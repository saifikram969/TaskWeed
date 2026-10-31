package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import com.saif.scheduleapp.domain.repository.ScheduleRepository

class AddTask(
    private val repository: ScheduleRepository,
    private val scheduler: ReminderScheduler? = null
) {
    suspend operator fun invoke(task: ScheduleTask): Result<Long> {
        val trimmedTitle = task.title.trim()
        if (trimmedTitle.isBlank()) {
            return Result.failure(IllegalArgumentException("Task title cannot be blank."))
        }

        if (task.endMinutes <= task.startMinutes) {
            return Result.failure(IllegalArgumentException("End time must be after start time."))
        }

        if (task.startMinutes !in 0..1439 || task.endMinutes !in 0..1439) {
            return Result.failure(IllegalArgumentException("Start time and end time must be within 0..1439 minutes."))
        }

        val taskToPersist = task.copy(title = trimmedTitle)
        return try {
            val newId = repository.addTask(taskToPersist)
            scheduler?.schedule(taskToPersist.copy(id = newId))
            Result.success(newId)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
