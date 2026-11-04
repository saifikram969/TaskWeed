package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DecideReminderTest {

    private val decideReminder = DecideReminder()

    private val baseTask = ScheduleTask(
        id = 10L,
        title = "Team Standup",
        details = "Daily sync",
        startMinutes = 540,
        endMinutes = 570,
        category = Category.JOBS,
        repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY),
        voiceEnabled = true
    )

    @Test
    fun testSingleTaskWithRepeatDaysDecidesSchedule() {
        val decision = decideReminder(baseTask)
        assertIs<ReminderDecision.Schedule>(decision)
        assertEquals(baseTask, decision.task)
    }

    @Test
    fun testSingleTaskWithEmptyRepeatDaysDecidesCancel() {
        val noDaysTask = baseTask.copy(repeatDays = emptySet())
        val decision = decideReminder(noDaysTask)
        assertIs<ReminderDecision.Cancel>(decision)
        assertEquals(10L, decision.taskId)
    }

    @Test
    fun testNewTaskWithRepeatDaysDecidesSchedule() {
        val decision = decideReminder(oldTask = null, newTask = baseTask)
        assertIs<ReminderDecision.Schedule>(decision)
        assertEquals(baseTask, decision.task)
    }

    @Test
    fun testNewTaskWithEmptyRepeatDaysDecidesNone() {
        val noDaysTask = baseTask.copy(repeatDays = emptySet())
        val decision = decideReminder(oldTask = null, newTask = noDaysTask)
        assertIs<ReminderDecision.None>(decision)
    }

    @Test
    fun testDeletedTaskDecidesCancel() {
        val decision = decideReminder(oldTask = baseTask, newTask = null)
        assertIs<ReminderDecision.Cancel>(decision)
        assertEquals(10L, decision.taskId)
    }

    @Test
    fun testBothNullDecidesNone() {
        val decision = decideReminder(oldTask = null, newTask = null)
        assertIs<ReminderDecision.None>(decision)
    }

    @Test
    fun testUpdatedTaskWithEmptyRepeatDaysDecidesCancel() {
        val updated = baseTask.copy(repeatDays = emptySet())
        val decision = decideReminder(oldTask = baseTask, newTask = updated)
        assertIs<ReminderDecision.Cancel>(decision)
        assertEquals(10L, decision.taskId)
    }

    @Test
    fun testUpdatedTaskStartTimeChangeDecidesSchedule() {
        val updated = baseTask.copy(startMinutes = 600)
        val decision = decideReminder(oldTask = baseTask, newTask = updated)
        assertIs<ReminderDecision.Schedule>(decision)
        assertEquals(updated, decision.task)
    }

    @Test
    fun testUpdatedTaskRepeatDaysChangeDecidesSchedule() {
        val updated = baseTask.copy(repeatDays = setOf(DayOfWeek.FRIDAY))
        val decision = decideReminder(oldTask = baseTask, newTask = updated)
        assertIs<ReminderDecision.Schedule>(decision)
        assertEquals(updated, decision.task)
    }

    @Test
    fun testUpdatedTaskTitleChangeDecidesSchedule() {
        val updated = baseTask.copy(title = "Morning Architecture Sync")
        val decision = decideReminder(oldTask = baseTask, newTask = updated)
        assertIs<ReminderDecision.Schedule>(decision)
        assertEquals(updated, decision.task)
    }

    @Test
    fun testUpdatedTaskDetailsChangeDecidesSchedule() {
        val updated = baseTask.copy(details = "Discuss Compose preview bugs")
        val decision = decideReminder(oldTask = baseTask, newTask = updated)
        assertIs<ReminderDecision.Schedule>(decision)
        assertEquals(updated, decision.task)
    }

    @Test
    fun testUpdatedTaskVoiceEnabledChangeDecidesSchedule() {
        val updated = baseTask.copy(voiceEnabled = false)
        val decision = decideReminder(oldTask = baseTask, newTask = updated)
        assertIs<ReminderDecision.Schedule>(decision)
        assertEquals(updated, decision.task)
    }

    @Test
    fun testUpdatedTaskOnlyCategoryChangeDecidesNone() {
        val updated = baseTask.copy(category = Category.LEARNING)
        val decision = decideReminder(oldTask = baseTask, newTask = updated)
        assertIs<ReminderDecision.None>(decision)
    }

    @Test
    fun testUpdatedTaskOnlyEndMinutesChangeDecidesNone() {
        val updated = baseTask.copy(endMinutes = 580)
        val decision = decideReminder(oldTask = baseTask, newTask = updated)
        assertIs<ReminderDecision.None>(decision)
    }

    @Test
    fun testForDeleteDecidesCancel() {
        val decision = decideReminder.forDelete(99L)
        assertIs<ReminderDecision.Cancel>(decision)
        assertEquals(99L, decision.taskId)
    }

    @Test
    fun testDecideOnTriggerReturnsSkipWhenTaskIsNull() {
        val decision = decideReminder.decideOnTrigger(
            task = null,
            date = LocalDate(2026, 11, 2),
            isDoneToday = false
        )
        assertIs<TriggerDecision.Skip>(decision)
    }

    @Test
    fun testDecideOnTriggerReturnsSkipWhenTaskInactiveToday() {
        // baseTask repeats on MONDAY, TUESDAY, WEDNESDAY
        // 2026-11-05 is a Thursday
        val thursday = LocalDate(2026, 11, 5)
        val decision = decideReminder.decideOnTrigger(
            task = baseTask,
            date = thursday,
            isDoneToday = false
        )
        assertIs<TriggerDecision.Skip>(decision)
    }

    @Test
    fun testDecideOnTriggerReturnsSkipWhenTaskAlreadyCompletedToday() {
        // 2026-11-02 is Monday (active day), but already completed
        val monday = LocalDate(2026, 11, 2)
        val decision = decideReminder.decideOnTrigger(
            task = baseTask,
            date = monday,
            isDoneToday = true
        )
        assertIs<TriggerDecision.Skip>(decision)
    }

    @Test
    fun testDecideOnTriggerReturnsNotifyWithSpeakTrueWhenVoiceEnabled() {
        // 2026-11-02 is Monday, active, not completed, baseTask has voiceEnabled = true
        val monday = LocalDate(2026, 11, 2)
        val decision = decideReminder.decideOnTrigger(
            task = baseTask,
            date = monday,
            isDoneToday = false
        )
        assertIs<TriggerDecision.Notify>(decision)
        assertEquals(baseTask, decision.task)
        assertEquals(true, decision.speak)
    }

    @Test
    fun testDecideOnTriggerReturnsNotifyWithSpeakFalseWhenVoiceDisabled() {
        val silentTask = baseTask.copy(voiceEnabled = false)
        val monday = LocalDate(2026, 11, 2)
        val decision = decideReminder.decideOnTrigger(
            task = silentTask,
            date = monday,
            isDoneToday = false
        )
        assertIs<TriggerDecision.Notify>(decision)
        assertEquals(silentTask, decision.task)
        assertEquals(false, decision.speak)
    }
}

