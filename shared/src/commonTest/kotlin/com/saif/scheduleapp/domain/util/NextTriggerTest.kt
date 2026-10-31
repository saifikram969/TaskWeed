package com.saif.scheduleapp.domain.util

import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals

class NextTriggerTest {

    private val timeZone = TimeZone.UTC

    @Test
    fun testTimeLaterToday() {
        // Now is 08:30 on 2026-10-31, task is at 10:00 (startMinutes = 600)
        val now = LocalDateTime(2026, 10, 31, 8, 30, 0, 0)
        val startMinutes = 600

        val triggerMillis = nextTriggerMillis(startMinutes, now, timeZone)

        val expected = LocalDateTime(2026, 10, 31, 10, 0, 0, 0)
            .toInstant(timeZone)
            .toEpochMilliseconds()
        assertEquals(expected, triggerMillis)
    }

    @Test
    fun testTimeAlreadyPassedToday() {
        // Now is 14:00 on 2026-10-31, task is at 10:00 (startMinutes = 600)
        val now = LocalDateTime(2026, 10, 31, 14, 0, 0, 0)
        val startMinutes = 600

        val triggerMillis = nextTriggerMillis(startMinutes, now, timeZone)

        // Should return tomorrow: 2026-11-01 10:00
        val expected = LocalDateTime(2026, 11, 1, 10, 0, 0, 0)
            .toInstant(timeZone)
            .toEpochMilliseconds()
        assertEquals(expected, triggerMillis)
    }

    @Test
    fun testExactlyNow() {
        // Now is exactly 10:00:00 on 2026-10-31, task is at 10:00 (startMinutes = 600)
        val now = LocalDateTime(2026, 10, 31, 10, 0, 0, 0)
        val startMinutes = 600

        val triggerMillis = nextTriggerMillis(startMinutes, now, timeZone)

        // If today's scheduled time is now or earlier, returns the same time tomorrow
        val expected = LocalDateTime(2026, 11, 1, 10, 0, 0, 0)
            .toInstant(timeZone)
            .toEpochMilliseconds()
        assertEquals(expected, triggerMillis)
    }

    @Test
    fun testMidnightRollover() {
        // Now is 23:45 on 2026-12-31, task is at 00:00 (startMinutes = 0)
        val now = LocalDateTime(2026, 12, 31, 23, 45, 0, 0)
        val startMinutes = 0

        val triggerMillis = nextTriggerMillis(startMinutes, now, timeZone)

        // Should roll over across year boundary to 2027-01-01 00:00
        val expected = LocalDateTime(2027, 1, 1, 0, 0, 0, 0)
            .toInstant(timeZone)
            .toEpochMilliseconds()
        assertEquals(expected, triggerMillis)
    }

    @Test
    fun testReminderSpeech() {
        val task = ScheduleTask(
            id = 1L,
            title = "Morning Standup",
            details = "Share team updates",
            startMinutes = 540,
            endMinutes = 570,
            category = Category.JOBS
        )

        val speech = reminderSpeech(task)
        assertEquals("Time for Morning Standup. Share team updates", speech)
    }
}

