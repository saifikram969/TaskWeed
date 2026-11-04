package com.saif.scheduleapp.data.repository

import com.saif.scheduleapp.core.util.TimeUtils
import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

class InMemoryScheduleRepository(
    initialTasks: List<ScheduleTask> = emptyList()
) : ScheduleRepository {

    private val _tasks = MutableStateFlow(initialTasks.map { it.copy(isDone = false) })
    private val _completedTasks = MutableStateFlow<Map<LocalDate, Set<Long>>>(emptyMap())

    init {
        val initialDoneIds = initialTasks.filter { it.isDone }.map { it.id }.toSet()
        if (initialDoneIds.isNotEmpty()) {
            val today = TimeUtils.nowLocalDateTime().date
            _completedTasks.value = mapOf(today to initialDoneIds)
        }
    }

    override fun observeTasks(date: LocalDate): Flow<List<ScheduleTask>> {
        return combine(_tasks, _completedTasks) { tasks, completedMap ->
            val completedIds = completedMap[date] ?: emptySet()
            tasks.map { task ->
                task.copy(isDone = task.id in completedIds)
            }
        }
    }

    override suspend fun setDone(taskId: Long, date: LocalDate, done: Boolean) {
        _completedTasks.update { currentMap ->
            val currentSet = currentMap[date] ?: emptySet()
            val newSet = if (done) currentSet + taskId else currentSet - taskId
            currentMap + (date to newSet)
        }
    }

    override suspend fun addTask(task: ScheduleTask): Long {
        var generatedId: Long = 1L
        _tasks.update { currentTasks ->
            val nextId = (currentTasks.maxOfOrNull { it.id } ?: 0L) + 1L
            generatedId = nextId
            (currentTasks + task.copy(id = nextId, isDone = false)).sortedBy { it.startMinutes }
        }
        return generatedId
    }

    override suspend fun updateTask(task: ScheduleTask) {
        _tasks.update { currentTasks ->
            val index = currentTasks.indexOfFirst { it.id == task.id }
            if (index != -1) {
                val updated = currentTasks.toMutableList()
                updated[index] = task.copy(isDone = false)
                updated.sortedBy { it.startMinutes }
            } else {
                currentTasks
            }
        }
    }

    override suspend fun deleteTask(taskId: Long) {
        _tasks.update { currentTasks ->
            currentTasks.filterNot { it.id == taskId }
        }
        _completedTasks.update { currentMap ->
            currentMap.mapValues { (_, taskIds) -> taskIds - taskId }
        }
    }

    override suspend fun getTask(taskId: Long): ScheduleTask? {
        return _tasks.value.find { it.id == taskId }?.copy(isDone = false)
    }

    override suspend fun clearAllTasks() {
        _tasks.value = emptyList()
        _completedTasks.value = emptyMap()
    }

    override suspend fun getAllTasks(): List<ScheduleTask> {
        return _tasks.value.map { it.copy(isDone = false) }
    }

    companion object {
        val defaultInitialTasks: List<ScheduleTask> = listOf(
            ScheduleTask(
                id = 1L,
                title = "Fajr Prayer & Morning Reflection",
                details = "Dawn prayer followed by supplications and morning contemplation.",
                startMinutes = 330,  // 05:30 AM
                endMinutes = 390,    // 06:30 AM
                category = Category.PRAYER,
                isDone = true
            ),
            ScheduleTask(
                id = 2L,
                title = "Kotlin Multiplatform Study",
                details = "Deep dive into Compose Multiplatform rendering pipeline and Kotlin/Wasm.",
                startMinutes = 540,  // 09:00 AM
                endMinutes = 660,    // 11:00 AM
                category = Category.LEARNING,
                isDone = false
            ),
            ScheduleTask(
                id = 3L,
                title = "Dhuhr Prayer & Lunch Break",
                details = "Midday prayer, nutritious lunch, and a short walk.",
                startMinutes = 780,  // 01:00 PM
                endMinutes = 840,    // 02:00 PM
                category = Category.PRAYER,
                isDone = false
            ),
            ScheduleTask(
                id = 4L,
                title = "Client Architecture & Code Review",
                details = "Review pull requests, ensure Clean Architecture boundaries, and refine APIs.",
                startMinutes = 870,  // 02:30 PM
                endMinutes = 990,    // 04:30 PM
                category = Category.JOBS,
                isDone = false
            ),
            ScheduleTask(
                id = 5L,
                title = "Asr Prayer Break",
                details = "Afternoon prayer and brief mental recharge.",
                startMinutes = 1020, // 05:00 PM
                endMinutes = 1050, // 05:30 PM
                category = Category.PRAYER,
                isDone = false
            ),
            ScheduleTask(
                id = 6L,
                title = "Personal Project & Research",
                details = "Experiment with offline-first local cache synchronization techniques.",
                startMinutes = 1080, // 06:00 PM
                endMinutes = 1170, // 07:30 PM
                category = Category.LEARNING,
                isDone = false
            ),
            ScheduleTask(
                id = 7L,
                title = "Maghrib Prayer & Dinner",
                details = "Sunset prayer followed by dinner with family.",
                startMinutes = 1185, // 07:45 PM
                endMinutes = 1245, // 08:45 PM
                category = Category.PRAYER,
                isDone = false
            ),
            ScheduleTask(
                id = 8L,
                title = "Isha Prayer & Evening Reading",
                details = "Night prayer followed by reading literature and journaling.",
                startMinutes = 1260, // 09:00 PM
                endMinutes = 1320, // 10:00 PM
                category = Category.PRAYER,
                isDone = false
            ),
            ScheduleTask(
                id = 9L,
                title = "Night Rest & Sleep",
                details = "Wind down and sleep to prepare for the next productive day.",
                startMinutes = 1350, // 10:30 PM
                endMinutes = 300,  // 05:00 AM (next day)
                category = Category.REST,
                isDone = false
            )
        )
    }
}
