package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.data.repository.InMemoryScheduleRepository
import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GetTaskTest {

    @Test
    fun testGetTaskReturnsTaskIfExists() = runTest {
        val repository = InMemoryScheduleRepository(initialTasks = emptyList())
        val getTask = GetTask(repository)

        val task = ScheduleTask(
            id = 12L,
            title = "Sample Task",
            details = "Sample Details",
            startMinutes = 540,
            endMinutes = 600,
            category = Category.LEARNING,
            repeatDays = setOf(DayOfWeek.MONDAY),
            voiceEnabled = false
        )
        val assignedId = repository.addTask(task)

        val retrieved = getTask(assignedId)
        assertNotNull(retrieved)
        assertEquals(assignedId, retrieved.id)
        assertEquals("Sample Task", retrieved.title)
        assertEquals(false, retrieved.voiceEnabled)

        val notFound = getTask(999L)
        assertNull(notFound)
    }
}

