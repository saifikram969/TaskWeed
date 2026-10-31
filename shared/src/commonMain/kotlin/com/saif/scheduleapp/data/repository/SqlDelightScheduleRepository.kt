package com.saif.scheduleapp.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.saif.scheduleapp.data.local.toDbString
import com.saif.scheduleapp.data.local.toDomain
import com.saif.scheduleapp.db.ScheduleDatabase
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightScheduleRepository(
    private val database: ScheduleDatabase
) : ScheduleRepository {

    private val queries = database.scheduleTaskQueries

    override fun observeTasks(): Flow<List<ScheduleTask>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun setDone(taskId: Long, done: Boolean) {
        withContext(Dispatchers.Default) {
            queries.setDone(
                isDone = if (done) 1L else 0L,
                id = taskId
            )
        }
    }

    override suspend fun addTask(task: ScheduleTask): Long {
        return withContext(Dispatchers.Default) {
            queries.transactionWithResult {
                queries.insertTask(
                    title = task.title,
                    details = task.details,
                    startMinutes = task.startMinutes.toLong(),
                    endMinutes = task.endMinutes.toLong(),
                    category = task.category.toDbString(),
                    isDone = if (task.isDone) 1L else 0L
                )
                queries.lastInsertRowId().executeAsOne()
            }
        }
    }
}
