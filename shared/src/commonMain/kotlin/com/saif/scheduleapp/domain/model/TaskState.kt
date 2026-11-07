package com.saif.scheduleapp.domain.model

enum class TaskState {
    UPCOMING,
    NOW,
    MISSED,
    DONE
}

/**
 * Classifies a task occurrence into one of the four TaskState values:
 * - DONE: If isDone is true (regardless of time)
 * - UPCOMING: If nowMinutes < startMinutes (strictly before start time)
 * - NOW: If startMinutes <= nowMinutes < endMinutes (start time inclusive, end time exclusive)
 * - MISSED: If nowMinutes >= endMinutes (end time inclusive, after scheduled time has passed)
 */
fun classifyTask(
    startMinutes: Int,
    endMinutes: Int,
    isDone: Boolean,
    nowMinutes: Int
): TaskState {
    return when {
        isDone -> TaskState.DONE
        nowMinutes < startMinutes -> TaskState.UPCOMING
        nowMinutes < endMinutes -> TaskState.NOW
        else -> TaskState.MISSED
    }
}

/**
 * Overload of classifyTask for ScheduleTask.
 */
fun classifyTask(task: ScheduleTask, nowMinutes: Int): TaskState {
    return classifyTask(
        startMinutes = task.startMinutes,
        endMinutes = task.endMinutes,
        isDone = task.isDone,
        nowMinutes = nowMinutes
    )
}

