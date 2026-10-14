package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.domain.repository.ScheduleRepository

class MarkTaskDone(
    private val repository: ScheduleRepository
) {
    suspend operator fun invoke(taskId: Long, done: Boolean) {
        repository.setDone(taskId = taskId, done = done)
    }
}
