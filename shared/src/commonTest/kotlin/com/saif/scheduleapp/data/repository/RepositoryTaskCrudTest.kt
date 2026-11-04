package com.saif.scheduleapp.data.repository

import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RepositoryTaskCrudTest {

    @Test
    fun testAddTaskAndGetTask() = runTest {
        val repository = InMemoryScheduleRepository()

        val task = ScheduleTask(
            id = 0L,
            title = "Algorithm Practice",
            details = "Dynamic programming",
            startMinutes = 540,
            endMinutes = 600,
            category = Category.LEARNING,
            repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
            voiceEnabled = false
        )

        val id = repository.addTask(task)
        val retrieved = repository.getTask(id)

        assertNotNull(retrieved)
        assertEquals(id, retrieved.id)
        assertEquals("Algorithm Practice", retrieved.title)
        assertEquals("Dynamic programming", retrieved.details)
        assertEquals(540, retrieved.startMinutes)
        assertEquals(600, retrieved.endMinutes)
        assertEquals(Category.LEARNING, retrieved.category)
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), retrieved.repeatDays)
        assertEquals(false, retrieved.voiceEnabled)
    }

    @Test
    fun testUpdateTask() = runTest {
        val repository = InMemoryScheduleRepository()

        val original = ScheduleTask(
            id = 0L,
            title = "Initial Title",
            details = "Initial Details",
            startMinutes = 600,
            endMinutes = 660,
            category = Category.JOBS,
            repeatDays = DayOfWeek.entries.toSet(),
            voiceEnabled = true
        )

        val id = repository.addTask(original)

        val updated = ScheduleTask(
            id = id,
            title = "Updated Title",
            details = "Updated Details",
            startMinutes = 630,
            endMinutes = 700,
            category = Category.REST,
            repeatDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
            voiceEnabled = false
        )

        repository.updateTask(updated)

        val retrieved = repository.getTask(id)
        assertNotNull(retrieved)
        assertEquals("Updated Title", retrieved.title)
        assertEquals("Updated Details", retrieved.details)
        assertEquals(630, retrieved.startMinutes)
        assertEquals(700, retrieved.endMinutes)
        assertEquals(Category.REST, retrieved.category)
        assertEquals(setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY), retrieved.repeatDays)
        assertEquals(false, retrieved.voiceEnabled)
    }

    @Test
    fun testDeleteTask() = runTest {
        val repository = InMemoryScheduleRepository()

        val task = ScheduleTask(
            id = 0L,
            title = "To be deleted",
            details = "",
            startMinutes = 600,
            endMinutes = 660,
            category = Category.REST
        )

        val id = repository.addTask(task)
        assertNotNull(repository.getTask(id))

        repository.deleteTask(id)
        assertNull(repository.getTask(id))
    }

    @Test
    fun testDeleteTaskDeletesCompletionRecords() = runTest {
        val repository = InMemoryScheduleRepository()
        val testDate = LocalDate(2026, 11, 2)

        val task = ScheduleTask(
            id = 0L,
            title = "Task with completions",
            details = "Will be marked done then deleted",
            startMinutes = 600,
            endMinutes = 660,
            category = Category.LEARNING
        )

        val id = repository.addTask(task)
        repository.setDone(taskId = id, date = testDate, done = true)

        val tasksBefore = repository.observeTasks(testDate).first()
        assertEquals(1, tasksBefore.size)
        assertTrue(tasksBefore.first().isDone)

        // Delete the task
        repository.deleteTask(id)

        // Task should no longer appear in observeTasks, and completion should be cleared
        val tasksAfter = repository.observeTasks(testDate).first()
        assertTrue(tasksAfter.isEmpty())
        assertNull(repository.getTask(id))
    }
}

