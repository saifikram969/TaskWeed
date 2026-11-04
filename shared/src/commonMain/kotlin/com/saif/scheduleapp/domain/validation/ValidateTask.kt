package com.saif.scheduleapp.domain.validation

import com.saif.scheduleapp.domain.model.ScheduleTask

/**
 * Pure domain validation for a [ScheduleTask].
 *
 * Rules:
 * - Title cannot be blank after trimming.
 * - End time must be strictly after start time.
 * - Start time and end time must be within [0, 1439] minutes of the day.
 * - Repeat days must not be empty ("Pick at least one day.").
 *
 * Throws [IllegalArgumentException] if validation fails.
 */
fun validateTask(task: ScheduleTask) {
    if (task.title.trim().isBlank()) {
        throw IllegalArgumentException("Task title cannot be blank.")
    }
    if (task.endMinutes <= task.startMinutes) {
        throw IllegalArgumentException("End time must be after start time.")
    }
    if (task.startMinutes !in 0..1439 || task.endMinutes !in 0..1439) {
        throw IllegalArgumentException("Start time and end time must be within 0..1439 minutes.")
    }
    if (task.repeatDays.isEmpty()) {
        throw IllegalArgumentException("Pick at least one day.")
    }
}

