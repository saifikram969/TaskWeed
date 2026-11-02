package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.data.repository.InMemoryScheduleRepository
import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImportTasksTest {

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
    fun testImportTasksAppendsWhenReplaceIsFalse() = runTest {
        val repository = InMemoryScheduleRepository()
        val scheduler = RecordingReminderScheduler()
        val addTask = AddTask(repository, scheduler)
        val clearAllTasks = ClearAllTasks(repository, scheduler)
        val importTasks = ImportTasks(addTask, clearAllTasks)

        repository.addTask(ScheduleTask(0, "Existing Task", "", 400, 500, Category.REST, false))

        val toImport = listOf(
            ScheduleTask(0, "Imported 1", "", 600, 700, Category.JOBS, false),
            ScheduleTask(0, "Imported 2", "", 800, 900, Category.LEARNING, false)
        )

        val result = importTasks(toImport, replaceExisting = false)
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull())

        val all = repository.getAllTasks()
        assertEquals(3, all.size)
        assertEquals(2, scheduler.scheduled.size)
    }

    @Test
    fun testImportTasksClearsWhenReplaceIsTrue() = runTest {
        val repository = InMemoryScheduleRepository()
        val scheduler = RecordingReminderScheduler()
        val addTask = AddTask(repository, scheduler)
        val clearAllTasks = ClearAllTasks(repository, scheduler)
        val importTasks = ImportTasks(addTask, clearAllTasks)

        val existingId = repository.addTask(ScheduleTask(0, "Existing Task", "", 400, 500, Category.REST, false))

        val toImport = listOf(
            ScheduleTask(0, "Imported 1", "", 600, 700, Category.JOBS, false)
        )

        val result = importTasks(toImport, replaceExisting = true)
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull())

        val all = repository.getAllTasks()
        assertEquals(1, all.size)
        assertEquals("Imported 1", all[0].title)
        assertTrue(scheduler.cancelled.contains(existingId))
    }
}

