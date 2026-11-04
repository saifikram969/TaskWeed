package com.saif.scheduleapp.data.local

import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import kotlinx.datetime.DayOfWeek
import com.saif.scheduleapp.db.ScheduleTask as ScheduleTaskEntity

fun ScheduleTaskEntity.toDomain(isCompleted: Boolean = false): ScheduleTask {
    return ScheduleTask(
        id = id,
        title = title,
        details = details,
        startMinutes = startMinutes.toInt(),
        endMinutes = endMinutes.toInt(),
        category = category.toCategory(),
        isDone = isCompleted,
        repeatDays = repeatDays.toDayOfWeekSet(),
        voiceEnabled = voiceEnabled != 0L
    )
}

fun String.toCategory(): Category {
    return try {
        Category.valueOf(this)
    } catch (e: IllegalArgumentException) {
        Category.LEARNING
    }
}

fun Category.toDbString(): String = this.name

/**
 * Maps a Set of [DayOfWeek] to an integer bitmask.
 *
 * Monday = bit 0 (1), Tuesday = bit 1 (2), ..., Sunday = bit 6 (64).
 * All 7 days = 127.
 */
fun Set<DayOfWeek>.toBitmask(): Int {
    var mask = 0
    for (day in this) {
        mask = mask or (1 shl day.ordinal)
    }
    return mask
}

/**
 * Maps an integer bitmask to a Set of [DayOfWeek].
 */
fun Int.toDayOfWeekSet(): Set<DayOfWeek> {
    val set = mutableSetOf<DayOfWeek>()
    for (day in DayOfWeek.entries) {
        if ((this and (1 shl day.ordinal)) != 0) {
            set.add(day)
        }
    }
    return set
}

fun Long.toDayOfWeekSet(): Set<DayOfWeek> = this.toInt().toDayOfWeekSet()

fun Set<DayOfWeek>.toBitmaskLong(): Long = this.toBitmask().toLong()
