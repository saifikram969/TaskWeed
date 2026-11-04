package com.saif.scheduleapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saif.scheduleapp.core.util.TimeUtils
import com.saif.scheduleapp.core.util.minuteTicker
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.platform.PdfReader
import com.saif.scheduleapp.domain.progress.remainingMinutes
import com.saif.scheduleapp.domain.progress.taskProgress
import com.saif.scheduleapp.domain.usecase.GetStats
import com.saif.scheduleapp.domain.usecase.GetTodayTasks
import com.saif.scheduleapp.domain.usecase.MarkTaskDone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getTodayTasks: GetTodayTasks,
    private val markTaskDone: MarkTaskDone,
    private val getStats: GetStats,
    private val pdfReader: PdfReader? = null,
    minuteTicker: Flow<Int> = minuteTicker()
) : ViewModel() {

    private val isPdfSupported: Boolean = pdfReader?.isSupported ?: false

    val uiState: StateFlow<HomeUiState> = combine(
        getTodayTasks(),
        minuteTicker,
        getStats()
    ) { tasks, nowMinutes, stats ->
        calculateUiState(
            tasks = tasks,
            nowMinutes = nowMinutes,
            currentStreak = stats.currentStreak,
            isPdfImportSupported = isPdfSupported
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HomeUiState(
            currentDate = TimeUtils.currentDateDisplay(),
            isPdfImportSupported = isPdfSupported
        )
    )

    fun onToggleTaskDone(task: ScheduleTask) {
        viewModelScope.launch {
            markTaskDone(taskId = task.id, done = !task.isDone)
        }
    }

    fun onSetTaskDone(taskId: Long, isDone: Boolean) {
        viewModelScope.launch {
            markTaskDone(taskId = taskId, done = isDone)
        }
    }

    companion object {
        fun getGreeting(nowMinutes: Int): String {
            return when {
                nowMinutes in 0..719 -> "Good morning"
                nowMinutes in 720..1019 -> "Good afternoon"
                else -> "Good evening"
            }
        }

        fun calculateUiState(
            tasks: List<ScheduleTask>,
            nowMinutes: Int,
            currentStreak: Int = 0,
            isPdfImportSupported: Boolean = false
        ): HomeUiState {
            val totalCount = tasks.size
            val doneCount = tasks.count { it.isDone }

            // nowTask is the first not-done task whose time range contains the current time
            // Interval: startMinutes <= nowMinutes && nowMinutes < endMinutes
            val nowTask = tasks.firstOrNull { task ->
                !task.isDone && TimeUtils.isCurrent(task.startMinutes, task.endMinutes, nowMinutes)
            }

            val nowProgress = nowTask?.let { taskProgress(it, nowMinutes) } ?: 0f
            val nowRemainingMinutes = nowTask?.let { remainingMinutes(it, nowMinutes) } ?: 0

            // upcoming contains not-done tasks whose startMinutes are strictly greater than current time
            // Excludes current task
            val upcoming = tasks.filter { task ->
                !task.isDone &&
                task.id != nowTask?.id &&
                TimeUtils.isUpcoming(task.startMinutes, nowMinutes)
            }.sortedBy { it.startMinutes }

            val doneTasks = tasks.filter { it.isDone }.sortedBy { it.startMinutes }

            return HomeUiState(
                nowTask = nowTask,
                nowProgress = nowProgress,
                nowRemainingMinutes = nowRemainingMinutes,
                upcoming = upcoming,
                doneTasks = doneTasks,
                doneCount = doneCount,
                totalCount = totalCount,
                currentStreak = currentStreak,
                greeting = getGreeting(nowMinutes),
                currentDate = TimeUtils.currentDateDisplay(),
                isPdfImportSupported = isPdfImportSupported
            )
        }
    }
}
