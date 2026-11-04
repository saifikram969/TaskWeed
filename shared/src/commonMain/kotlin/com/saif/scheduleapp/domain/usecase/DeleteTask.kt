package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.domain.platform.ReminderScheduler
import com.saif.scheduleapp.domain.repository.ScheduleRepository

class DeleteTask(
    private val repository: ScheduleRepository,
    private val scheduler: ReminderScheduler? = null,
    private val decideReminder: DecideReminder = DecideReminder()
) {
    suspend operator fun invoke(taskId: Long) {
        val decision = decideReminder.forDelete(taskId)
        if (decision is ReminderDecision.Cancel) {
            scheduler?.cancel(decision.taskId)
        }
        repository.deleteTask(taskId)
    }
}
