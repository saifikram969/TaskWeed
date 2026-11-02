package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.core.util.TimeUtils
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import kotlinx.datetime.LocalDate

class MarkTaskDone(
    private val repository: ScheduleRepository
) {
    suspend operator fun invoke(
        taskId: Long,
        done: Boolean,
        date: LocalDate = TimeUtils.nowLocalDateTime().date
    ) {
        repository.setDone(taskId = taskId, date = date, done = done)
    }
}
