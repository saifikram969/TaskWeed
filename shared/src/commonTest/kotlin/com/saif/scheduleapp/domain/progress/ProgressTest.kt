package com.saif.scheduleapp.domain.progress

import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProgressTest {

    private fun createTask(
        id: Long = 1L,
        startMinutes: Int = 540, // 09:00
        endMinutes: Int = 600    // 10:00 (duration: 60)
    ) = ScheduleTask(
        id = id,
        title = "Test Task",
        details = "",
        startMinutes = startMinutes,
        endMinutes = endMinutes,
        category = Category.LEARNING
    )

    // --- taskProgress tests ---

    @Test
    fun taskProgress_before_task_returns_zero() {
        val task = createTask()
        assertEquals(0f, taskProgress(task, nowMinutes = 510)) // 08:30
        assertEquals(0f, taskProgress(task, nowMinutes = 0))   // 00:00
        assertEquals(0f, taskProgress(task, nowMinutes = 539)) // 08:59
    }

    @Test
    fun taskProgress_at_start_returns_zero() {
        val task = createTask()
        assertEquals(0f, taskProgress(task, nowMinutes = 540)) // 09:00
    }

    @Test
    fun taskProgress_during_task_returns_clamped_fraction() {
        val task = createTask()
        assertEquals(0.25f, taskProgress(task, nowMinutes = 555)) // 09:15
        assertEquals(0.50f, taskProgress(task, nowMinutes = 570)) // 09:30
        assertEquals(0.75f, taskProgress(task, nowMinutes = 585)) // 09:45
    }

    @Test
    fun taskProgress_at_end_returns_one() {
        val task = createTask()
        assertEquals(1f, taskProgress(task, nowMinutes = 600)) // 10:00
    }

    @Test
    fun taskProgress_after_task_clamps_to_one() {
        val task = createTask()
        assertEquals(1f, taskProgress(task, nowMinutes = 615)) // 10:15
        assertEquals(1f, taskProgress(task, nowMinutes = 1439)) // 23:59
    }

    @Test
    fun taskProgress_zero_duration_clamps_correctly() {
        val zeroDurationTask = createTask(startMinutes = 500, endMinutes = 500)
        assertEquals(0f, taskProgress(zeroDurationTask, nowMinutes = 400))
        assertEquals(1f, taskProgress(zeroDurationTask, nowMinutes = 500))
        assertEquals(1f, taskProgress(zeroDurationTask, nowMinutes = 600))
    }

    // --- remainingMinutes tests ---

    @Test
    fun remainingMinutes_before_task_returns_full_duration() {
        val task = createTask() // duration = 60
        assertEquals(60, remainingMinutes(task, nowMinutes = 510)) // 08:30
        assertEquals(60, remainingMinutes(task, nowMinutes = 540)) // 09:00
    }

    @Test
    fun remainingMinutes_during_task_returns_exact_remaining() {
        val task = createTask()
        assertEquals(45, remainingMinutes(task, nowMinutes = 555)) // 09:15
        assertEquals(30, remainingMinutes(task, nowMinutes = 570)) // 09:30
        assertEquals(1, remainingMinutes(task, nowMinutes = 599))  // 09:59
    }

    @Test
    fun remainingMinutes_at_and_after_task_never_negative() {
        val task = createTask()
        assertEquals(0, remainingMinutes(task, nowMinutes = 600)) // 10:00
        assertEquals(0, remainingMinutes(task, nowMinutes = 615)) // 10:15
        assertEquals(0, remainingMinutes(task, nowMinutes = 1000))
    }

    @Test
    fun remainingMinutes_zero_duration_returns_zero() {
        val zeroDurationTask = createTask(startMinutes = 500, endMinutes = 500)
        assertEquals(0, remainingMinutes(zeroDurationTask, nowMinutes = 400))
        assertEquals(0, remainingMinutes(zeroDurationTask, nowMinutes = 500))
        assertEquals(0, remainingMinutes(zeroDurationTask, nowMinutes = 600))
    }

    // --- Boundary Conditions & DayStats.isComplete ---

    @Test
    fun dayStats_boundary_conditions_including_exact_80_percent() {
        val date = LocalDate(2026, 11, 4)

        // Exactly 80%
        assertTrue(DayStats(date, total = 5, done = 4).isComplete)
        assertTrue(DayStats(date, total = 10, done = 8).isComplete)
        assertTrue(DayStats(date, total = 100, done = 80).isComplete)

        // Above 80%
        assertTrue(DayStats(date, total = 5, done = 5).isComplete)
        assertTrue(DayStats(date, total = 4, done = 5).isComplete) // > 100%

        // Below 80%
        assertFalse(DayStats(date, total = 4, done = 3).isComplete) // 75%
        assertFalse(DayStats(date, total = 9, done = 7).isComplete) // ~77.7%
        assertFalse(DayStats(date, total = 100, done = 79).isComplete) // 79%
        assertFalse(DayStats(date, total = 5, done = 0).isComplete) // 0%

        // Zero tasks: total greater than zero requirement
        assertFalse(DayStats(date, total = 0, done = 0).isComplete)
    }

    // --- Streak Tests ---

    @Test
    fun streak_empty_list_returns_zero() {
        val today = LocalDate(2026, 11, 4)
        assertEquals(0, currentStreak(emptyList(), today))
        assertEquals(0, longestStreak(emptyList()))
    }

    @Test
    fun streak_today_only_complete_returns_one() {
        val today = LocalDate(2026, 11, 4)
        val days = listOf(DayStats(today, total = 5, done = 4)) // 80%
        assertEquals(1, currentStreak(days, today))
        assertEquals(1, longestStreak(days))
    }

    @Test
    fun streak_today_only_incomplete_returns_zero() {
        val today = LocalDate(2026, 11, 4)
        val days = listOf(DayStats(today, total = 5, done = 2)) // 40%
        assertEquals(0, currentStreak(days, today))
        assertEquals(0, longestStreak(days))
    }

    @Test
    fun streak_today_only_zero_tasks_returns_zero() {
        val today = LocalDate(2026, 11, 4)
        val days = listOf(DayStats(today, total = 0, done = 0))
        assertEquals(0, currentStreak(days, today))
        assertEquals(0, longestStreak(days))
    }

    @Test
    fun streak_starts_from_yesterday_when_today_is_not_complete_yet() {
        val today = LocalDate(2026, 11, 4)
        val yesterday = today.minus(1, DateTimeUnit.DAY)

        // Today is ongoing (only 1 of 5 done)
        val days = listOf(
            DayStats(yesterday, total = 5, done = 5),
            DayStats(today, total = 5, done = 1)
        )
        // Does not break streak because today is not complete yet
        assertEquals(1, currentStreak(days, today))
        assertEquals(1, longestStreak(days))
    }

    @Test
    fun streak_starts_from_yesterday_when_today_has_zero_tasks() {
        val today = LocalDate(2026, 11, 4)
        val day1 = today.minus(2, DateTimeUnit.DAY)
        val day2 = today.minus(1, DateTimeUnit.DAY)

        val days = listOf(
            DayStats(day1, total = 5, done = 5),
            DayStats(day2, total = 5, done = 4),
            DayStats(today, total = 0, done = 0)
        )
        assertEquals(2, currentStreak(days, today))
        assertEquals(2, longestStreak(days))
    }

    @Test
    fun streak_skips_days_with_zero_tasks() {
        val today = LocalDate(2026, 11, 4)
        val day1 = today.minus(4, DateTimeUnit.DAY)
        val day2Zero = today.minus(3, DateTimeUnit.DAY)
        val day3 = today.minus(2, DateTimeUnit.DAY)
        val day4Zero = today.minus(1, DateTimeUnit.DAY)

        val days = listOf(
            DayStats(day1, total = 5, done = 5),     // Complete
            DayStats(day2Zero, total = 0, done = 0), // Skipped
            DayStats(day3, total = 5, done = 4),     // Complete (80%)
            DayStats(day4Zero, total = 0, done = 0), // Skipped
            DayStats(today, total = 5, done = 5)     // Complete
        )
        // 3 completed days, 2 skipped zero-task days
        assertEquals(3, currentStreak(days, today))
        assertEquals(3, longestStreak(days))
    }

    @Test
    fun streak_skips_zero_tasks_when_today_not_complete_yet() {
        val today = LocalDate(2026, 11, 4)
        val day1 = today.minus(3, DateTimeUnit.DAY)
        val day2Zero = today.minus(2, DateTimeUnit.DAY)
        val day3 = today.minus(1, DateTimeUnit.DAY)

        val days = listOf(
            DayStats(day1, total = 5, done = 5),
            DayStats(day2Zero, total = 0, done = 0), // Skipped
            DayStats(day3, total = 5, done = 5),     // Yesterday complete
            DayStats(today, total = 5, done = 2)     // Today not complete yet
        )
        assertEquals(2, currentStreak(days, today))
        assertEquals(2, longestStreak(days))
    }

    @Test
    fun streak_stops_at_first_incomplete_day() {
        val today = LocalDate(2026, 11, 4)
        val day1 = today.minus(4, DateTimeUnit.DAY)
        val day2 = today.minus(3, DateTimeUnit.DAY)
        val day3Incomplete = today.minus(2, DateTimeUnit.DAY)
        val day4 = today.minus(1, DateTimeUnit.DAY)

        val days = listOf(
            DayStats(day1, total = 5, done = 5),
            DayStats(day2, total = 5, done = 5),
            DayStats(day3Incomplete, total = 5, done = 1), // Incomplete breaks here
            DayStats(day4, total = 5, done = 5),
            DayStats(today, total = 5, done = 5)
        )
        assertEquals(2, currentStreak(days, today)) // Days 4 & today
        assertEquals(2, longestStreak(days))
    }

    @Test
    fun longest_streak_tracks_maximum_past_streak() {
        val today = LocalDate(2026, 11, 7)
        val d1 = LocalDate(2026, 11, 1)
        val d2 = LocalDate(2026, 11, 2)
        val d3 = LocalDate(2026, 11, 3)
        val d4 = LocalDate(2026, 11, 4)
        val d5Incomplete = LocalDate(2026, 11, 5)
        val d6 = LocalDate(2026, 11, 6)
        val d7 = LocalDate(2026, 11, 7)

        val days = listOf(
            DayStats(d1, total = 5, done = 5),
            DayStats(d2, total = 5, done = 5),
            DayStats(d3, total = 5, done = 5),
            DayStats(d4, total = 5, done = 4), // 4-day streak here
            DayStats(d5Incomplete, total = 5, done = 0), // Breaks streak
            DayStats(d6, total = 5, done = 5),
            DayStats(d7, total = 5, done = 5)  // Current streak = 2
        )
        assertEquals(2, currentStreak(days, today = d7))
        assertEquals(4, longestStreak(days))
    }

    @Test
    fun streak_returns_zero_when_yesterday_incomplete_and_today_incomplete() {
        val today = LocalDate(2026, 11, 4)
        val yesterday = today.minus(1, DateTimeUnit.DAY)

        val days = listOf(
            DayStats(today.minus(2, DateTimeUnit.DAY), total = 5, done = 5),
            DayStats(yesterday, total = 5, done = 1), // Incomplete
            DayStats(today, total = 5, done = 2)     // Incomplete
        )
        assertEquals(0, currentStreak(days, today))
        assertEquals(1, longestStreak(days))
    }
}

