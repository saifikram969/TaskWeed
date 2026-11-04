package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AddTaskTest {

    private class FakeScheduleRepository : ScheduleRepository {
        val tasks = mutableListOf<ScheduleTask>()

        override fun observeTasks(date: kotlinx.datetime.LocalDate): Flow<List<ScheduleTask>> = flowOf(tasks)

        override suspend fun setDone(taskId: Long, date: kotlinx.datetime.LocalDate, done: Boolean) {
            val index = tasks.indexOfFirst { it.id == taskId }
            if (index != -1) {
                tasks[index] = tasks[index].copy(isDone = done)
            }
        }

        override suspend fun addTask(task: ScheduleTask): Long {
            val nextId = (tasks.maxOfOrNull { it.id } ?: 0L) + 1L
            tasks.add(task.copy(id = nextId))
            return nextId
        }

        override suspend fun clearAllTasks() {
            tasks.clear()
        }

        override suspend fun getAllTasks(): List<ScheduleTask> = tasks
    }

    @Test
    fun testBlankTitle() = kotlinx.coroutines.test.runTest {
        val fakeRepo = FakeScheduleRepository()
        val addTask = AddTask(fakeRepo)
        val task = ScheduleTask(
            id = 0L,
            title = "   ",
            details = "Some details",
            startMinutes = 540,
            endMinutes = 600,
            category = Category.LEARNING
        )

        val result = addTask(task)

        assertTrue(result.isFailure)
        assertEquals("Task title cannot be blank.", result.exceptionOrNull()?.message)
        assertTrue(fakeRepo.tasks.isEmpty())
    }

    @Test
    fun testEndBeforeStart() = kotlinx.coroutines.test.runTest {
        val fakeRepo = FakeScheduleRepository()
        val addTask = AddTask(fakeRepo)
        val task = ScheduleTask(
            id = 0L,
            title = "Valid Title",
            details = "Details",
            startMinutes = 600,
            endMinutes = 540,
            category = Category.JOBS
        )

        val result = addTask(task)

        assertTrue(result.isFailure)
        assertEquals("End time must be after start time.", result.exceptionOrNull()?.message)
        assertTrue(fakeRepo.tasks.isEmpty())
    }

    @Test
    fun testEndEqualToStart() = kotlinx.coroutines.test.runTest {
        val fakeRepo = FakeScheduleRepository()
        val addTask = AddTask(fakeRepo)
        val task = ScheduleTask(
            id = 0L,
            title = "Valid Title",
            details = "Details",
            startMinutes = 600,
            endMinutes = 600,
            category = Category.JOBS
        )

        val result = addTask(task)

        assertTrue(result.isFailure)
        assertEquals("End time must be after start time.", result.exceptionOrNull()?.message)
        assertTrue(fakeRepo.tasks.isEmpty())
    }

    @Test
    fun testOutOfRangeTime() = kotlinx.coroutines.test.runTest {
        val fakeRepo = FakeScheduleRepository()
        val addTask = AddTask(fakeRepo)

        // Negative start time
        val taskNegative = ScheduleTask(
            id = 0L,
            title = "Valid Title",
            details = "Details",
            startMinutes = -10,
            endMinutes = 600,
            category = Category.PRAYER
        )
        val resultNegative = addTask(taskNegative)
        assertTrue(resultNegative.isFailure)
        assertEquals("Start time and end time must be within 0..1439 minutes.", resultNegative.exceptionOrNull()?.message)

        // Beyond 1439 end time
        val taskOver = ScheduleTask(
            id = 0L,
            title = "Valid Title",
            details = "Details",
            startMinutes = 1400,
            endMinutes = 1440,
            category = Category.PRAYER
        )
        val resultOver = addTask(taskOver)
        assertTrue(resultOver.isFailure)
        assertEquals("Start time and end time must be within 0..1439 minutes.", resultOver.exceptionOrNull()?.message)

        assertTrue(fakeRepo.tasks.isEmpty())
    }

    @Test
    fun testEmptyRepeatDays() = kotlinx.coroutines.test.runTest {
        val fakeRepo = FakeScheduleRepository()
        val addTask = AddTask(fakeRepo)
        val task = ScheduleTask(
            id = 0L,
            title = "Valid Title",
            details = "Details",
            startMinutes = 540,
            endMinutes = 600,
            category = Category.LEARNING,
            repeatDays = emptySet()
        )

        val result = addTask(task)

        assertTrue(result.isFailure)
        assertEquals("Pick at least one day.", result.exceptionOrNull()?.message)
        assertTrue(fakeRepo.tasks.isEmpty())
    }

    @Test
    fun testValidTask() = kotlinx.coroutines.test.runTest {
        val fakeRepo = FakeScheduleRepository()
        val addTask = AddTask(fakeRepo)
        val task = ScheduleTask(
            id = 0L,
            title = "  Clean Up Desk  ",
            details = "Organize workspace",
            startMinutes = 540,
            endMinutes = 600,
            category = Category.REST
        )

        val result = addTask(task)

        assertTrue(result.isSuccess)
        val createdId = result.getOrNull()
        assertEquals(1L, createdId)

        assertEquals(1, fakeRepo.tasks.size)
        val savedTask = fakeRepo.tasks.first()
        assertEquals("Clean Up Desk", savedTask.title)
        assertEquals(1L, savedTask.id)
        assertEquals(540, savedTask.startMinutes)
        assertEquals(600, savedTask.endMinutes)
        assertEquals(Category.REST, savedTask.category)
    }

    private class FakeReminderScheduler : ReminderScheduler {
        val scheduled = mutableListOf<ScheduleTask>()
        override fun schedule(task: ScheduleTask) {
            scheduled.add(task)
        }
        override fun cancel(taskId: Long) {}
        override fun snooze(task: ScheduleTask, minutes: Int) {}
    }

    @Test
    fun testValidTaskSchedulesReminder() = kotlinx.coroutines.test.runTest {
        val fakeRepo = FakeScheduleRepository()
        val fakeScheduler = FakeReminderScheduler()
        val addTask = AddTask(repository = fakeRepo, scheduler = fakeScheduler)

        val task = ScheduleTask(
            id = 0L,
            title = "Drink Water",
            details = "Hydration is key",
            startMinutes = 600,
            endMinutes = 630,
            category = Category.REST
        )

        val result = addTask(task)

        assertTrue(result.isSuccess)
        assertEquals(1, fakeScheduler.scheduled.size)
        val scheduledTask = fakeScheduler.scheduled.first()
        assertEquals(1L, scheduledTask.id)
        assertEquals("Drink Water", scheduledTask.title)
    }
}
