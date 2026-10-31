package com.saif.scheduleapp.reminder

import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.platform.ReminderScheduler

/**
 * Web no-op implementation of [ReminderScheduler].
 * Logs when scheduling, cancelling, or snoozing is requested.
 */
class WebReminderScheduler : ReminderScheduler {

    override fun schedule(task: ScheduleTask) {
        println("[WebReminderScheduler] Scheduled reminder for task '${task.title}' (ID: ${task.id}) at ${task.startMinutes} mins")
    }

    override fun cancel(taskId: Long) {
        println("[WebReminderScheduler] Canceled reminder for task ID $taskId")
    }

    override fun snooze(task: ScheduleTask, minutes: Int) {
        println("[WebReminderScheduler] Snoozed reminder for task '${task.title}' (ID: ${task.id}) by $minutes min")
    }
}

