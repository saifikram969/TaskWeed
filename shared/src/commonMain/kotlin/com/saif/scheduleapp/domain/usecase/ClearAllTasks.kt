package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.domain.platform.ReminderScheduler
import com.saif.scheduleapp.domain.repository.ScheduleRepository

class ClearAllTasks(
    private val repository: ScheduleRepository,
    private val scheduler: ReminderScheduler? = null
) {
    suspend operator fun invoke() {
        val existingTasks = repository.getAllTasks()
        for (task in existingTasks) {
            scheduler?.cancel(task.id)
        }
        repository.clearAllTasks()
    }
}

