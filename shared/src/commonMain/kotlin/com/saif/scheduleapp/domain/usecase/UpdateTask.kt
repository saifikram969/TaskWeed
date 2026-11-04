package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import com.saif.scheduleapp.domain.validation.validateTask

class UpdateTask(
    private val repository: ScheduleRepository,
    private val scheduler: ReminderScheduler? = null,
    private val decideReminder: DecideReminder = DecideReminder()
) {
    suspend operator fun invoke(task: ScheduleTask): Result<Unit> {
        return try {
            validateTask(task)
            val trimmedTitle = task.title.trim()
            val taskToPersist = task.copy(title = trimmedTitle)
            val oldTask = repository.getTask(taskToPersist.id)
            repository.updateTask(taskToPersist)

            when (val decision = decideReminder(oldTask, taskToPersist)) {
                is ReminderDecision.Schedule -> scheduler?.schedule(decision.task)
                is ReminderDecision.Cancel -> scheduler?.cancel(decision.taskId)
                is ReminderDecision.None -> Unit
            }

            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
