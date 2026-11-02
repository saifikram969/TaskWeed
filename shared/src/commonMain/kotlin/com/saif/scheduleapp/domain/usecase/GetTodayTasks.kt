package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.core.util.dateTicker
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class GetTodayTasks(
    private val repository: ScheduleRepository,
    private val dateFlow: Flow<LocalDate> = dateTicker()
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<ScheduleTask>> {
        return dateFlow.flatMapLatest { date ->
            repository.observeTasks(date).map { tasks ->
                tasks.sortedBy { it.startMinutes }
            }
        }
    }
}
