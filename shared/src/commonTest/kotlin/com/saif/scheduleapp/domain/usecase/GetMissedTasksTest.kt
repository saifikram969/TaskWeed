package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.data.repository.InMemoryScheduleRepository
import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GetMissedTasksTest {

    private val testToday = LocalDate(2026, 11, 5) // Thursday

    @Test
    fun testTaskTodayWithExpiredEndTimeIsMissed() {
        val task = ScheduleTask(
            id = 1L,
            title = "Morning Run",
            details = "",
            startMinutes = 480, // 08:00 AM
            endMinutes = 540,   // 09:00 AM
            category = Category.REST,
            repeatDays = setOf(DayOfWeek.THURSDAY)
        )

        // At 09:01 AM (541 mins), end time has passed and task is not completed
        val missed = GetMissedTasks.computeMissedTasks(
            allTasks = listOf(task),
            completionsMap = emptyMap(),
            today = testToday,
            nowMinutes = 541,
            lookbackDays = 7
        )

        assertEquals(1, missed.size)
        assertEquals(1L, missed.first().task.id)
        assertEquals(testToday, missed.first().date)
    }

    @Test
    fun testTaskTodayBeforeEndTimeIsNotMissed() {
        val task = ScheduleTask(
            id = 1L,
            title = "Morning Run",
            details = "",
            startMinutes = 480, // 08:00 AM
            endMinutes = 540,   // 09:00 AM
            category = Category.REST,
            repeatDays = setOf(DayOfWeek.THURSDAY)
        )

        // At 08:30 AM (510 mins), task is in progress, end time not passed yet
        val missedDuring = GetMissedTasks.computeMissedTasks(
            allTasks = listOf(task),
            completionsMap = emptyMap(),
            today = testToday,
            nowMinutes = 510,
            lookbackDays = 7
        )
        assertTrue(missedDuring.isEmpty())

        // At 07:00 AM (420 mins), task is upcoming
        val missedBefore = GetMissedTasks.computeMissedTasks(
            allTasks = listOf(task),
            completionsMap = emptyMap(),
            today = testToday,
            nowMinutes = 420,
            lookbackDays = 7
        )
        assertTrue(missedBefore.isEmpty())
    }

    @Test
    fun testCompletedTaskTodayIsNotMissedEvenAfterEndTime() {
        val task = ScheduleTask(
            id = 1L,
            title = "Completed Task",
            details = "",
            startMinutes = 480,
            endMinutes = 540,
            category = Category.LEARNING,
            repeatDays = setOf(DayOfWeek.THURSDAY)
        )

        // Task was completed for today
        val completions = mapOf(testToday to setOf(1L))

        val missed = GetMissedTasks.computeMissedTasks(
            allTasks = listOf(task),
            completionsMap = completions,
            today = testToday,
            nowMinutes = 600,
            lookbackDays = 7
        )

        assertTrue(missed.isEmpty())
    }

    @Test
    fun testPastOccurrenceInLookbackWindowNotCompletedIsMissed() {
        val yesterday = testToday.minus(1, DateTimeUnit.DAY) // Wednesday

        val task = ScheduleTask(
            id = 2L,
            title = "Wednesday Study",
            details = "",
            startMinutes = 600,
            endMinutes = 660,
            category = Category.LEARNING,
            repeatDays = setOf(DayOfWeek.WEDNESDAY)
        )

        val missed = GetMissedTasks.computeMissedTasks(
            allTasks = listOf(task),
            completionsMap = emptyMap(),
            today = testToday,
            nowMinutes = 400,
            lookbackDays = 7
        )

        assertEquals(1, missed.size)
        assertEquals(2L, missed.first().task.id)
        assertEquals(yesterday, missed.first().date)
    }

    @Test
    fun testPastOccurrenceCompletedIsNotMissed() {
        val yesterday = testToday.minus(1, DateTimeUnit.DAY) // Wednesday

        val task = ScheduleTask(
            id = 2L,
            title = "Wednesday Study",
            details = "",
            startMinutes = 600,
            endMinutes = 660,
            category = Category.LEARNING,
            repeatDays = setOf(DayOfWeek.WEDNESDAY)
        )

        val completions = mapOf(yesterday to setOf(2L))

        val missed = GetMissedTasks.computeMissedTasks(
            allTasks = listOf(task),
            completionsMap = completions,
            today = testToday,
            nowMinutes = 400,
            lookbackDays = 7
        )

        assertTrue(missed.isEmpty())
    }

    @Test
    fun testRecurringTaskOccurrencesHandledIndependently() {
        // Task repeats Mon, Wed, Fri from 10:00 to 11:00 AM (600 to 660 mins)
        val task = ScheduleTask(
            id = 3L,
            title = "MWF Workout",
            details = "",
            startMinutes = 600,
            endMinutes = 660,
            category = Category.REST,
            repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        )

        // Scenario 1: On Wednesday morning at 09:00 AM (before Wednesday's 10:00 AM start):
        // Monday was missed, Wednesday has not passed yet, Friday is in the future.
        val testWednesday = LocalDate(2026, 11, 4)
        val monday = testWednesday.minus(2, DateTimeUnit.DAY)
        val previousFriday = testWednesday.minus(5, DateTimeUnit.DAY)

        val missedOnWedMorning = GetMissedTasks.computeMissedTasks(
            allTasks = listOf(task),
            completionsMap = mapOf(previousFriday to setOf(3L)),
            today = testWednesday,
            nowMinutes = 540, // 09:00 AM - before 11:00 AM end time
            lookbackDays = 7
        )

        // Only Monday is missed; Wednesday end time has not passed yet; Friday is future
        assertEquals(1, missedOnWedMorning.size)
        assertEquals(monday, missedOnWedMorning.first().date)
        assertEquals(3L, missedOnWedMorning.first().task.id)

        // Scenario 2: Completing Monday's missed occurrence does NOT complete subsequent occurrences
        val missedAfterMondayDone = GetMissedTasks.computeMissedTasks(
            allTasks = listOf(task),
            completionsMap = mapOf(previousFriday to setOf(3L), monday to setOf(3L)),
            today = testWednesday,
            nowMinutes = 540,
            lookbackDays = 7
        )
        assertTrue(missedAfterMondayDone.isEmpty())

        // Later on Wednesday at 12:00 PM (720 mins), after Wednesday's end time:
        // Now Wednesday becomes missed if incomplete, but Friday remains future.
        val missedOnWedAfternoon = GetMissedTasks.computeMissedTasks(
            allTasks = listOf(task),
            completionsMap = mapOf(previousFriday to setOf(3L), monday to setOf(3L)),
            today = testWednesday,
            nowMinutes = 720,
            lookbackDays = 7
        )
        assertEquals(1, missedOnWedAfternoon.size)
        assertEquals(testWednesday, missedOnWedAfternoon.first().date)
    }

    @Test
    fun testLateCompletionRemovesOccurrenceFromRepository() = runTest {
        val yesterday = testToday.minus(1, DateTimeUnit.DAY)
        val task = ScheduleTask(
            id = 10L,
            title = "Late Task",
            details = "",
            startMinutes = 500,
            endMinutes = 560,
            category = Category.JOBS,
            repeatDays = setOf(yesterday.dayOfWeek)
        )

        val repo = InMemoryScheduleRepository(listOf(task))
        val markTaskDone = MarkTaskDone(repo)
        val getMissedTasks = GetMissedTasks(repo)

        // Before completion: task is missed for yesterday
        val missedBefore = getMissedTasks(today = testToday, nowMinutes = 600).first()
        assertEquals(1, missedBefore.size)
        assertEquals(yesterday, missedBefore.first().date)

        // Complete it for yesterday
        markTaskDone(taskId = 10L, done = true, date = yesterday)

        // After completion: task is no longer missed
        val missedAfter = getMissedTasks(today = testToday, nowMinutes = 600).first()
        assertTrue(missedAfter.isEmpty())
    }

    @Test
    fun testStatsDoNotCountMissedTasksUntilCompleted() = runTest {
        val yesterday = testToday.minus(1, DateTimeUnit.DAY)
        val task = ScheduleTask(
            id = 20L,
            title = "Task for Stats",
            details = "",
            startMinutes = 500,
            endMinutes = 560,
            category = Category.JOBS,
            repeatDays = setOf(yesterday.dayOfWeek)
        )

        val repo = InMemoryScheduleRepository(listOf(task))
        val getStats = GetStats(repo)
        val markTaskDone = MarkTaskDone(repo)

        // Query stats before completion
        val statsBefore = getStats(today = testToday).first()
        val yesterdayStatsBefore = statsBefore.last7.first { it.date == yesterday }
        assertEquals(1, yesterdayStatsBefore.total)
        assertEquals(0, yesterdayStatsBefore.done, "Missed task must NOT count as completed in Stats")

        // Perform late completion for yesterday
        markTaskDone(taskId = 20L, done = true, date = yesterday)

        // Query stats after late completion
        val statsAfter = getStats(today = testToday).first()
        val yesterdayStatsAfter = statsAfter.last7.first { it.date == yesterday }
        assertEquals(1, yesterdayStatsAfter.total)
        assertEquals(1, yesterdayStatsAfter.done, "Late completed task must count as completed for that date")
    }
}
