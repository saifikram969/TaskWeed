package com.saif.scheduleapp.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.saif.scheduleapp.data.local.toBitmaskLong
import com.saif.scheduleapp.data.local.toCategory
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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class SqlDelightScheduleRepository(
    private val database: ScheduleDatabase
) : ScheduleRepository {

    private val queries = database.scheduleTaskQueries

    override fun observeAllTasks(): Flow<List<ScheduleTask>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { tasks -> tasks.map { it.toDomain(isCompleted = false) } }
    }

    override fun observeCompletions(from: LocalDate, to: LocalDate): Flow<Map<LocalDate, Set<Long>>> {
        return queries.selectCompletionsBetweenDates(fromDate = from.toString(), toDate = to.toString())
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list ->
                val result = mutableMapOf<LocalDate, MutableSet<Long>>()
                var d = from
                while (d <= to) {
                    result[d] = mutableSetOf()
                    d = d.plus(1, DateTimeUnit.DAY)
                }
                for (row in list) {
                    val date = LocalDate.parse(row.date)
                    result.getOrPut(date) { mutableSetOf() }.add(row.taskId)
                }
                result.mapValues { it.value.toSet() }
            }
    }

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
                    isDone = 0L,
                    repeatDays = task.repeatDays.toBitmaskLong(),
                    voiceEnabled = if (task.voiceEnabled) 1L else 0L
                )
                queries.lastInsertRowId().executeAsOne()
            }
        }
    }

    override suspend fun updateTask(task: ScheduleTask) {
        withContext(Dispatchers.Default) {
            queries.updateTask(
                id = task.id,
                title = task.title,
                details = task.details,
                startMinutes = task.startMinutes.toLong(),
                endMinutes = task.endMinutes.toLong(),
                category = task.category.toDbString(),
                repeatDays = task.repeatDays.toBitmaskLong(),
                voiceEnabled = if (task.voiceEnabled) 1L else 0L
            )
        }
    }

    override suspend fun deleteTask(taskId: Long) {
        withContext(Dispatchers.Default) {
            queries.transaction {
                queries.deleteCompletionsForTask(taskId)
                queries.deleteTask(taskId)
            }
        }
    }

    override suspend fun getTask(taskId: Long): ScheduleTask? {
        return withContext(Dispatchers.Default) {
            queries.selectById(taskId).executeAsOneOrNull()?.toDomain(isCompleted = false)
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
