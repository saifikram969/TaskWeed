package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetTodayTasks(
    private val repository: ScheduleRepository
) {
    operator fun invoke(): Flow<List<ScheduleTask>> {
        return repository.observeTasks().map { tasks ->
            tasks.sortedBy { it.startMinutes }
        }
    }
}
