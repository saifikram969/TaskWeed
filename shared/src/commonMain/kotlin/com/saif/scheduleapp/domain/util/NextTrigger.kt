package com.saif.scheduleapp.domain.util

import com.saif.scheduleapp.domain.model.ScheduleTask
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant

/**
 * Calculates the next trigger timestamp in milliseconds for a scheduled task.
 *
 * If today's scheduled time is now or earlier, returns the same time tomorrow.
 */
fun nextTriggerMillis(
    startMinutes: Int,
    now: LocalDateTime,
    zone: TimeZone
): Long {
    val hour = startMinutes / 60
    val minute = startMinutes % 60
    val targetTime = LocalTime(hour, minute)
    val todayScheduled = LocalDateTime(now.date, targetTime)

    val scheduledDate = if (todayScheduled <= now) {
        now.date.plus(1, DateTimeUnit.DAY)
    } else {
        now.date
    }

    val scheduledDateTime = LocalDateTime(scheduledDate, targetTime)
    return scheduledDateTime.toInstant(zone).toEpochMilliseconds()
}

/**
 * Pure function generating the speech text for task reminders.
 */
fun reminderSpeech(task: ScheduleTask): String {
    return "Time for ${task.title}. ${task.details}"
}
