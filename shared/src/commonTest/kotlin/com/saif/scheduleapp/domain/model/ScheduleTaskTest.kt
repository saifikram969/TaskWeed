package com.saif.scheduleapp.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScheduleTaskTest {

    @Test
    fun testDefaultValues() {
        val task = ScheduleTask(
            id = 1L,
            title = "Daily Study",
            details = "Study Kotlin",
            startMinutes = 540,
            endMinutes = 600,
            category = Category.LEARNING
        )

        assertFalse(task.isDone)
        assertTrue(task.voiceEnabled)
        assertEquals(DayOfWeek.entries.toSet(), task.repeatDays)
        assertEquals(7, task.repeatDays.size)
    }

    @Test
    fun testIsActiveOn() {
        // Monday & Wednesday only
        val task = ScheduleTask(
            id = 2L,
            title = "Gym Session",
            details = "Workout",
            startMinutes = 1020,
            endMinutes = 1080,
            category = Category.REST,
            repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
        )

        // 2026-11-02 is Monday -> true
        val monday = LocalDate(2026, 11, 2)
        assertTrue(task.isActiveOn(monday))

        // 2026-11-03 is Tuesday -> false
        val tuesday = LocalDate(2026, 11, 3)
        assertFalse(task.isActiveOn(tuesday))

        // 2026-11-04 is Wednesday -> true
        val wednesday = LocalDate(2026, 11, 4)
        assertTrue(task.isActiveOn(wednesday))

        // 2026-11-05 is Thursday -> false
        val thursday = LocalDate(2026, 11, 5)
        assertFalse(task.isActiveOn(thursday))

        // 2026-10-31 is Saturday -> false
        val saturday = LocalDate(2026, 10, 31)
        assertFalse(task.isActiveOn(saturday))
    }
}

