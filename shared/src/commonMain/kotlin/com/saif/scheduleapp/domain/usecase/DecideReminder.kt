package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.domain.model.ScheduleTask
import kotlinx.datetime.LocalDate

sealed interface ReminderDecision {
    data class Schedule(val task: ScheduleTask) : ReminderDecision
    data class Cancel(val taskId: Long) : ReminderDecision
    data object None : ReminderDecision
}

sealed interface TriggerDecision {
    data class Notify(val task: ScheduleTask, val speak: Boolean) : TriggerDecision
    data object Skip : TriggerDecision
}

class DecideReminder {

    /**
     * Decides reminder action for a single task.
     *
     * If the task has repeat days, schedules it; if empty, cancels it.
     */
    operator fun invoke(task: ScheduleTask): ReminderDecision {
        return if (task.repeatDays.isNotEmpty()) {
            ReminderDecision.Schedule(task)
        } else {
            ReminderDecision.Cancel(task.id)
        }
    }

    /**
     * Decides reminder action when transitioning from [oldTask] to [newTask].
     *
     * - If [newTask] is null (task deleted): Cancel reminder for [oldTask].
     * - If [newTask] has empty repeatDays: Cancel reminder for [oldTask].
     * - If [oldTask] is null: Schedule reminder for [newTask].
     * - If [oldTask] and [newTask] are identical in reminder-relevant fields
     *   (startMinutes, repeatDays, title, details, voiceEnabled):
     *   None (no need to reschedule).
     * - Otherwise: Schedule updated reminder for [newTask].
     */
    operator fun invoke(oldTask: ScheduleTask?, newTask: ScheduleTask?): ReminderDecision {
        if (newTask == null) {
            return if (oldTask != null) {
                ReminderDecision.Cancel(oldTask.id)
            } else {
                ReminderDecision.None
            }
        }

        if (newTask.repeatDays.isEmpty()) {
            return if (oldTask != null) {
                ReminderDecision.Cancel(oldTask.id)
            } else {
                ReminderDecision.None
            }
        }

        if (oldTask == null) {
            return ReminderDecision.Schedule(newTask)
        }

        val hasReminderRelevantChanges =
            oldTask.startMinutes != newTask.startMinutes ||
            oldTask.repeatDays != newTask.repeatDays ||
            oldTask.title != newTask.title ||
            oldTask.details != newTask.details ||
            oldTask.voiceEnabled != newTask.voiceEnabled

        return if (hasReminderRelevantChanges) {
            ReminderDecision.Schedule(newTask)
        } else {
            ReminderDecision.None
        }
    }

    fun forDelete(taskId: Long): ReminderDecision {
        return ReminderDecision.Cancel(taskId)
    }

    /**
     * Decides whether to notify when a scheduled alarm triggers.
     *
     * - Returns [TriggerDecision.Skip] if task is missing, inactive today, or already completed.
     * - Returns [TriggerDecision.Notify] if active today and not completed, with [Notify.speak] reflecting [voiceEnabled].
     */
    fun decideOnTrigger(
        task: ScheduleTask?,
        date: LocalDate,
        isDoneToday: Boolean
    ): TriggerDecision {
        if (task == null) return TriggerDecision.Skip
        if (!task.isActiveOn(date)) return TriggerDecision.Skip
        if (isDoneToday) return TriggerDecision.Skip

        return TriggerDecision.Notify(
            task = task,
            speak = task.voiceEnabled
        )
    }
}


