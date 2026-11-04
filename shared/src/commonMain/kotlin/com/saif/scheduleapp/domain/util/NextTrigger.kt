package com.saif.scheduleapp.domain.util

import com.saif.scheduleapp.domain.model.ScheduleTask
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant

/**
 * Calculates the next trigger timestamp in milliseconds for a scheduled task.
 *
 * Considers [repeatDays]:
 * - If today's day of week is in [repeatDays] and the scheduled time is strictly in the future, triggers today.
 * - Otherwise searches upcoming days (starting from tomorrow) for the earliest day of week in [repeatDays].
 *
 * If [repeatDays] is empty, throws [IllegalArgumentException].
 */
fun nextTriggerMillis(
    startMinutes: Int,
    repeatDays: Set<DayOfWeek>,
    now: LocalDateTime,
    zone: TimeZone
): Long {
    require(repeatDays.isNotEmpty()) { "Pick at least one day." }

    val hour = startMinutes / 60
    val minute = startMinutes % 60
    val targetTime = LocalTime(hour, minute)

    val todayScheduled = LocalDateTime(now.date, targetTime)

    val scheduledDate = if (now.date.dayOfWeek in repeatDays && todayScheduled > now) {
        now.date
    } else {
        var candidateDate = now.date.plus(1, DateTimeUnit.DAY)
        while (candidateDate.dayOfWeek !in repeatDays) {
            candidateDate = candidateDate.plus(1, DateTimeUnit.DAY)
        }
        candidateDate
    }

    val scheduledDateTime = LocalDateTime(scheduledDate, targetTime)
    return scheduledDateTime.toInstant(zone).toEpochMilliseconds()
}

/**
 * Overload accepting [startMinutes], [now], and [zone], defaulting to everyday repeat.
 */
fun nextTriggerMillis(
    startMinutes: Int,
    now: LocalDateTime,
    zone: TimeZone
): Long = nextTriggerMillis(
    startMinutes = startMinutes,
    repeatDays = DayOfWeek.entries.toSet(),
    now = now,
    zone = zone
)

/**
 * Convenience overload accepting [ScheduleTask], [now], and [zone].
 */
fun nextTriggerMillis(
    task: ScheduleTask,
    now: LocalDateTime,
    zone: TimeZone
): Long = nextTriggerMillis(
    startMinutes = task.startMinutes,
    repeatDays = task.repeatDays,
    now = now,
    zone = zone
)

/**
 * Pure function generating the speech text for task reminders.
 */
fun reminderSpeech(task: ScheduleTask): String {
    return "Time for ${task.title}. ${task.details}"
}
