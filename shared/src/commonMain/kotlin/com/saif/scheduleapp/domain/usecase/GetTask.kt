package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.repository.ScheduleRepository

class GetTask(
    private val repository: ScheduleRepository
) {
    suspend operator fun invoke(taskId: Long): ScheduleTask? {
        return repository.getTask(taskId)
    }
}

