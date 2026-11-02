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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class SqlDelightScheduleRepository(
    private val database: ScheduleDatabase
) : ScheduleRepository {

    private val queries = database.scheduleTaskQueries

    override fun observeTasks(date: LocalDate): Flow<List<ScheduleTask>> {
        val tasksFlow = queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)

        val completedIdsFlow = queries.selectCompletedTaskIdsForDate(date.toString())
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.toSet() }

        return tasksFlow.combine(completedIdsFlow) { tasks, completedIds ->
            tasks.map { dbTask ->
                dbTask.toDomain(isCompleted = dbTask.id in completedIds)
            }
        }
    }

    override suspend fun setDone(taskId: Long, date: LocalDate, done: Boolean) {
        withContext(Dispatchers.Default) {
            if (done) {
                queries.insertCompletion(taskId = taskId, date = date.toString())
            } else {
                queries.deleteCompletion(taskId = taskId, date = date.toString())
            }
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
                    isDone = 0L
                )
                queries.lastInsertRowId().executeAsOne()
            }
        }
    }

    override suspend fun clearAllTasks() {
        withContext(Dispatchers.Default) {
            queries.transaction {
                queries.clearAllCompletions()
                queries.clearAllTasks()
            }
        }
    }

    override suspend fun getAllTasks(): List<ScheduleTask> {
        return withContext(Dispatchers.Default) {
            queries.selectAll().executeAsList().map { it.toDomain(isCompleted = false) }
        }
    }
}
