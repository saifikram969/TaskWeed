package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.data.repository.InMemoryScheduleRepository
import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DeleteTaskTest {

    private class FakeReminderScheduler : ReminderScheduler {
        val cancelled = mutableListOf<Long>()

        override fun schedule(task: ScheduleTask) {}
        override fun cancel(taskId: Long) {
            cancelled.add(taskId)
        }
        override fun snooze(task: ScheduleTask, minutes: Int) {}
    }

    @Test
    fun testDeleteTaskCancelsReminderAndRemovesFromRepository() = runTest {
        val repository = InMemoryScheduleRepository(initialTasks = emptyList())
        val scheduler = FakeReminderScheduler()
        val deleteTask = DeleteTask(repository, scheduler)

        val task = ScheduleTask(
            id = 55L,
            title = "Task To Delete",
            details = "Details",
            startMinutes = 540,
            endMinutes = 600,
            category = Category.LEARNING,
            repeatDays = setOf(DayOfWeek.MONDAY)
        )
        repository.addTask(task)

        deleteTask(55L)

        assertEquals(1, scheduler.cancelled.size)
        assertEquals(55L, scheduler.cancelled.first())

        val inDb = repository.getTask(55L)
        assertNull(inDb)
    }
}

