package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.data.repository.InMemoryScheduleRepository
import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearAllTasksTest {

    private class RecordingReminderScheduler : ReminderScheduler {
        val scheduled = mutableListOf<ScheduleTask>()
        val cancelled = mutableListOf<Long>()

        override fun schedule(task: ScheduleTask) {
            scheduled.add(task)
        }

        override fun cancel(taskId: Long) {
            cancelled.add(taskId)
        }

        override fun snooze(task: ScheduleTask, minutes: Int) {}
    }

    @Test
    fun testClearAllTasksCancelsRemindersAndClearsDatabase() = runTest {
        val repository = InMemoryScheduleRepository()
        val scheduler = RecordingReminderScheduler()

        val id1 = repository.addTask(
            ScheduleTask(0, "Task 1", "", 600, 700, Category.JOBS, false)
        )
        val id2 = repository.addTask(
            ScheduleTask(0, "Task 2", "", 800, 900, Category.LEARNING, false)
        )

        assertEquals(2, repository.getAllTasks().size)

        val clearAllTasks = ClearAllTasks(repository, scheduler)
        clearAllTasks()

        assertEquals(0, repository.getAllTasks().size)
        assertTrue(scheduler.cancelled.contains(id1))
        assertTrue(scheduler.cancelled.contains(id2))
    }
}

