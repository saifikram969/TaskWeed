package com.saif.scheduleapp.domain.validation

import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import kotlinx.datetime.DayOfWeek
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ValidateTaskTest {

    @Test
    fun testBlankTitleThrows() {
        val task = ScheduleTask(
            id = 1L,
            title = "   ",
            details = "",
            startMinutes = 540,
            endMinutes = 600,
            category = Category.LEARNING
        )
        val ex = assertFailsWith<IllegalArgumentException> {
            validateTask(task)
        }
        assertEquals("Task title cannot be blank.", ex.message)
    }

    @Test
    fun testEndBeforeStartThrows() {
        val task = ScheduleTask(
            id = 1L,
            title = "Valid Title",
            details = "",
            startMinutes = 600,
            endMinutes = 540,
            category = Category.LEARNING
        )
        val ex = assertFailsWith<IllegalArgumentException> {
            validateTask(task)
        }
        assertEquals("End time must be after start time.", ex.message)
    }

    @Test
    fun testEndEqualToStartThrows() {
        val task = ScheduleTask(
            id = 1L,
            title = "Valid Title",
            details = "",
            startMinutes = 600,
            endMinutes = 600,
            category = Category.LEARNING
        )
        val ex = assertFailsWith<IllegalArgumentException> {
            validateTask(task)
        }
        assertEquals("End time must be after start time.", ex.message)
    }

    @Test
    fun testOutOfRangeTimesThrows() {
        // Negative start
        val taskNeg = ScheduleTask(
            id = 1L,
            title = "Valid Title",
            details = "",
            startMinutes = -1,
            endMinutes = 600,
            category = Category.LEARNING
        )
        val ex1 = assertFailsWith<IllegalArgumentException> {
            validateTask(taskNeg)
        }
        assertEquals("Start time and end time must be within 0..1439 minutes.", ex1.message)

        // End >= 1440
        val taskOver = ScheduleTask(
            id = 1L,
            title = "Valid Title",
            details = "",
            startMinutes = 1400,
            endMinutes = 1440,
            category = Category.LEARNING
        )
        val ex2 = assertFailsWith<IllegalArgumentException> {
            validateTask(taskOver)
        }
        assertEquals("Start time and end time must be within 0..1439 minutes.", ex2.message)
    }

    @Test
    fun testEmptyRepeatDaysThrows() {
        val task = ScheduleTask(
            id = 1L,
            title = "Valid Title",
            details = "",
            startMinutes = 540,
            endMinutes = 600,
            category = Category.LEARNING,
            repeatDays = emptySet()
        )
        val ex = assertFailsWith<IllegalArgumentException> {
            validateTask(task)
        }
        assertEquals("Pick at least one day.", ex.message)
    }

    @Test
    fun testValidTaskPasses() {
        val task = ScheduleTask(
            id = 1L,
            title = "DSA Practice",
            details = "LeetCode graphs",
            startMinutes = 360,
            endMinutes = 480,
            category = Category.LEARNING,
            repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            voiceEnabled = true
        )
        // Should not throw
        validateTask(task)
    }
}

