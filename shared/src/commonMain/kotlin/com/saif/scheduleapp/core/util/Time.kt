package com.saif.scheduleapp.core.util

import kotlin.time.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object TimeUtils {

    /**
     * Returns the current local date-time safely converting stdlib Clock to kotlinx-datetime.
     */
    fun nowLocalDateTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime {
        return Clock.System.now().toLocalDateTime(timeZone)
    }

    /**
     * Returns the current time as minutes since midnight (0..1439).
     */
    fun currentMinutesSinceMidnight(timeZone: TimeZone = TimeZone.currentSystemDefault()): Int {
        val now = nowLocalDateTime(timeZone)
        return now.hour * 60 + now.minute
    }

    /**
     * Formats minutes since midnight into a 12-hour formatted time string, e.g. "09:30 AM".
     */
    fun formatMinutesToTimeString(minutes: Int): String {
        val normalized = ((minutes % 1440) + 1440) % 1440
        val hour24 = normalized / 60
        val minute = normalized % 60
        val isPm = hour24 >= 12
        val hour12 = when (val h = hour24 % 12) {
            0 -> 12
            else -> h
        }
        val amPm = if (isPm) "PM" else "AM"
        val hourStr = if (hour12 < 10) "0$hour12" else "$hour12"
        val minuteStr = if (minute < 10) "0$minute" else "$minute"
        return "$hourStr:$minuteStr $amPm"
    }

    /**
     * Formats start and end minutes into a range string, e.g. "09:00 AM - 11:00 AM".
     */
    fun formatTimeRange(startMinutes: Int, endMinutes: Int): String {
        return "${formatMinutesToTimeString(startMinutes)} - ${formatMinutesToTimeString(endMinutes)}"
    }

    /**
     * Formats today's date into a display string, e.g. "Monday, Oct 12, 2026".
     */
    fun currentDateDisplay(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
        val today = nowLocalDateTime(timeZone).date
        val dayOfWeek = today.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        val month = today.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        @Suppress("DEPRECATION")
        val dayNum = today.dayOfMonth
        return "$dayOfWeek, $month $dayNum, ${today.year}"
    }

    /**
     * Determines if a task is currently active based on start-inclusive, end-exclusive rule.
     */
    fun isCurrent(startMinutes: Int, endMinutes: Int, currentMinutes: Int): Boolean {
        return startMinutes <= currentMinutes && currentMinutes < endMinutes
    }

    /**
     * Determines if a task is upcoming (strictly greater than current time).
     */
    fun isUpcoming(startMinutes: Int, currentMinutes: Int): Boolean {
        return startMinutes > currentMinutes
    }
}
