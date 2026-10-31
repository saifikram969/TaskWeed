package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RescheduleAllRemindersTest {

    private class FakeScheduleRepository(
        private val tasks: List<ScheduleTask>
    ) : ScheduleRepository {
        override fun observeTasks(): Flow<List<ScheduleTask>> = flowOf(tasks)
        override suspend fun setDone(taskId: Long, done: Boolean) {}
        override suspend fun addTask(task: ScheduleTask): Long = 0L
    }

    private class FakeReminderScheduler : ReminderScheduler {
        val scheduledTasks = mutableListOf<ScheduleTask>()
        val canceledTaskIds = mutableListOf<Long>()
        val snoozedTasks = mutableListOf<Pair<ScheduleTask, Int>>()

        override fun schedule(task: ScheduleTask) {
            scheduledTasks.add(task)
        }

        override fun cancel(taskId: Long) {
            canceledTaskIds.add(taskId)
        }

        override fun snooze(task: ScheduleTask, minutes: Int) {
            snoozedTasks.add(task to minutes)
        }
    }

    @Test
    fun testRescheduleSchedulesOnlyNotDoneTasks() = runTest {
        val task1 = ScheduleTask(
            id = 1L,
            title = "Task 1",
            details = "Active task",
            startMinutes = 540,
            endMinutes = 600,
            category = Category.LEARNING,
            isDone = false
        )
        val task2 = ScheduleTask(
            id = 2L,
            title = "Task 2",
            details = "Completed task",
            startMinutes = 600,
            endMinutes = 660,
            category = Category.JOBS,
            isDone = true
        )
        val task3 = ScheduleTask(
            id = 3L,
            title = "Task 3",
            details = "Another active task",
            startMinutes = 700,
            endMinutes = 750,
            category = Category.REST,
            isDone = false
        )

        val repository = FakeScheduleRepository(listOf(task1, task2, task3))
        val scheduler = FakeReminderScheduler()
        val rescheduleAllReminders = RescheduleAllReminders(repository, scheduler)

        rescheduleAllReminders()

        assertEquals(2, scheduler.scheduledTasks.size)
        assertTrue(scheduler.scheduledTasks.any { it.id == 1L })
        assertTrue(scheduler.scheduledTasks.any { it.id == 3L })
        assertTrue(scheduler.scheduledTasks.none { it.id == 2L })
    }
}

