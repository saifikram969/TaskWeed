package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import com.saif.scheduleapp.domain.validation.validateTask

class AddTask(
    private val repository: ScheduleRepository,
    private val scheduler: ReminderScheduler? = null
) {
    suspend operator fun invoke(task: ScheduleTask): Result<Long> {
        return try {
            validateTask(task)
            val trimmedTitle = task.title.trim()
            val taskToPersist = task.copy(title = trimmedTitle)
            val newId = repository.addTask(taskToPersist)
            scheduler?.schedule(taskToPersist.copy(id = newId))
            Result.success(newId)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
