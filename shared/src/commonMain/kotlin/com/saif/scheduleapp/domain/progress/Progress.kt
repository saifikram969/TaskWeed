package com.saif.scheduleapp.domain.progress

import com.saif.scheduleapp.domain.model.ScheduleTask
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

const val STREAK_THRESHOLD: Float = 0.8f

data class DayStats(
    val date: LocalDate,
    val total: Int,
    val done: Int
) {
    val isComplete: Boolean
        get() = total > 0 && (done.toFloat() / total.toFloat()) >= STREAK_THRESHOLD
}

data class Stats(
    val today: DayStats,
    val last7: List<DayStats>,
    val currentStreak: Int,
    val longestStreak: Int,
    val completedLast30: Int,
    val averageLast30: Float
)

fun taskProgress(task: ScheduleTask, nowMinutes: Int): Float {
    val duration = task.endMinutes - task.startMinutes
    if (duration <= 0) {
        return if (nowMinutes >= task.startMinutes) 1f else 0f
    }
    val progress = (nowMinutes - task.startMinutes).toFloat() / duration.toFloat()
    return progress.coerceIn(0f, 1f)
}

fun remainingMinutes(task: ScheduleTask, nowMinutes: Int): Int {
    val duration = task.endMinutes - task.startMinutes
    if (duration <= 0) return 0
    if (nowMinutes <= task.startMinutes) return duration
    if (nowMinutes >= task.endMinutes) return 0
    return maxOf(0, task.endMinutes - nowMinutes)
}

fun currentStreak(days: List<DayStats>, today: LocalDate): Int {
    if (days.isEmpty()) return 0
    val dayMap = days.groupBy { it.date }
        .mapValues { (date, list) ->
            DayStats(date, list.sumOf { it.total }, list.sumOf { it.done })
        }
    val minDate = dayMap.keys.minOrNull() ?: return 0
    if (minDate > today) return 0

    val todayStats = dayMap[today]
    val todayIsComplete = todayStats?.isComplete == true

    var streak = 0
    var currentDate = if (todayIsComplete) today else today.minus(1, DateTimeUnit.DAY)

    while (currentDate >= minDate) {
        val stats = dayMap[currentDate]
        if (stats == null || stats.total == 0) {
            // Days with zero tasks are skipped
            currentDate = currentDate.minus(1, DateTimeUnit.DAY)
            continue
        }

        if (stats.isComplete) {
            streak++
            currentDate = currentDate.minus(1, DateTimeUnit.DAY)
        } else {
            // Stop the streak at the first incomplete day
            break
        }
    }

    return streak
}

fun longestStreak(days: List<DayStats>): Int {
    if (days.isEmpty()) return 0
    val dayMap = days.groupBy { it.date }
        .mapValues { (date, list) ->
            DayStats(date, list.sumOf { it.total }, list.sumOf { it.done })
        }
    val minDate = dayMap.keys.minOrNull() ?: return 0
    val maxDate = dayMap.keys.maxOrNull() ?: return 0

    var longest = 0
    var current = 0
    var currentDate = minDate

    while (currentDate <= maxDate) {
        val stats = dayMap[currentDate]
        if (stats == null || stats.total == 0) {
            // Days with zero tasks are skipped
            currentDate = currentDate.plus(1, DateTimeUnit.DAY)
            continue
        }

        if (stats.isComplete) {
            current++
            if (current > longest) {
                longest = current
            }
        } else {
            // Stop the streak at the first incomplete day
            current = 0
        }
        currentDate = currentDate.plus(1, DateTimeUnit.DAY)
    }

    return longest
}

