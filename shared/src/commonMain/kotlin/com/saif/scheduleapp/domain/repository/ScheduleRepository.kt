package com.saif.scheduleapp.domain.repository

import com.saif.scheduleapp.core.util.TimeUtils
import com.saif.scheduleapp.domain.model.ScheduleTask
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ScheduleRepository {
    fun observeTasks(date: LocalDate): Flow<List<ScheduleTask>>

    fun observeTasks(): Flow<List<ScheduleTask>> =
        observeTasks(TimeUtils.nowLocalDateTime().date)

    suspend fun setDone(
        taskId: Long,
        date: LocalDate,
        done: Boolean
    )

    suspend fun setDone(
        taskId: Long,
        done: Boolean
    ) = setDone(taskId, TimeUtils.nowLocalDateTime().date, done)

    suspend fun addTask(task: ScheduleTask): Long

    suspend fun clearAllTasks()

    suspend fun getAllTasks(): List<ScheduleTask>
}
