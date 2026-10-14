package com.saif.scheduleapp.domain.repository

import com.saif.scheduleapp.domain.model.ScheduleTask
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    fun observeTasks(): Flow<List<ScheduleTask>>

    suspend fun setDone(
        taskId: Long,
        done: Boolean
    )
}
