package com.saif.scheduleapp.data.repository

import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryScheduleRepository(
    initialTasks: List<ScheduleTask> = defaultInitialTasks
) : ScheduleRepository {

    private val _tasks = MutableStateFlow(initialTasks)

    override fun observeTasks(): Flow<List<ScheduleTask>> = _tasks.asStateFlow()

    override suspend fun setDone(taskId: Long, done: Boolean) {
        _tasks.update { currentTasks ->
            currentTasks.map { task ->
                if (task.id == taskId) {
                    task.copy(isDone = done)
                } else {
                    task
                }
            }
        }
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
                startMinutes = 1180, // 07:40 PM
                endMinutes = 1240, // 08:40 PM
                category = Category.PRAYER,
                isDone = false
            ),
            ScheduleTask(
                id = 8L,
                title = "Evening Wind-Down & Rest",
                details = "Reading, screen downtime, and preparing for restful sleep.",
                startMinutes = 1320, // 10:00 PM
                endMinutes = 1410, // 11:30 PM
                category = Category.REST,
                isDone = false
            )
        )
    }
}
