package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.data.repository.InMemoryScheduleRepository
import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UpdateTaskTest {

    private class FakeReminderScheduler : ReminderScheduler {
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
    fun testUpdateTaskSuccessfullyUpdatesAndReschedules() = runTest {
        val repository = InMemoryScheduleRepository(initialTasks = emptyList())
        val scheduler = FakeReminderScheduler()
        val updateTask = UpdateTask(repository, scheduler)

        val original = ScheduleTask(
            id = 1L,
            title = "Original Title",
            details = "Original Details",
            startMinutes = 540,
            endMinutes = 600,
            category = Category.LEARNING,
            repeatDays = setOf(DayOfWeek.MONDAY)
        )
        repository.addTask(original)

        val updated = original.copy(
            title = "Updated Title",
            startMinutes = 600,
            endMinutes = 660
        )

        val result = updateTask(updated)
        assertTrue(result.isSuccess)

        val inDb = repository.getTask(1L)
        assertNotNull(inDb)
        assertEquals("Updated Title", inDb.title)
        assertEquals(600, inDb.startMinutes)
        assertEquals(660, inDb.endMinutes)

        assertEquals(1, scheduler.scheduled.size)
        assertEquals(updated, scheduler.scheduled.first())
    }

    @Test
    fun testUpdateTaskFailsOnInvalidTask() = runTest {
        val repository = InMemoryScheduleRepository(initialTasks = emptyList())
        val scheduler = FakeReminderScheduler()
        val updateTask = UpdateTask(repository, scheduler)

        // Invalid: empty repeatDays
        val invalidTask = ScheduleTask(
            id = 1L,
            title = "Valid Title",
            details = "Details",
            startMinutes = 540,
            endMinutes = 600,
            category = Category.LEARNING,
            repeatDays = emptySet()
        )

        val result = updateTask(invalidTask)
        assertTrue(result.isFailure)
        assertEquals("Pick at least one day.", result.exceptionOrNull()?.message)
        assertEquals(0, scheduler.scheduled.size)
    }

    @Test
    fun testUpdateTaskFailsOnInvalidTimeRange() = runTest {
        val repository = InMemoryScheduleRepository(initialTasks = emptyList())
        val scheduler = FakeReminderScheduler()
        val updateTask = UpdateTask(repository, scheduler)

        val invalidTask = ScheduleTask(
            id = 1L,
            title = "Valid Title",
            details = "Details",
            startMinutes = 600,
            endMinutes = 500, // end <= start
            category = Category.LEARNING,
            repeatDays = setOf(DayOfWeek.MONDAY)
        )

        val result = updateTask(invalidTask)
        assertTrue(result.isFailure)
        assertEquals(0, scheduler.scheduled.size)
    }
}

