package com.saif.scheduleapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saif.scheduleapp.core.util.TimeUtils
import com.saif.scheduleapp.core.util.minuteTicker
import com.saif.scheduleapp.domain.model.MissedTask
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.model.TaskState
import com.saif.scheduleapp.domain.model.classifyTask
import com.saif.scheduleapp.domain.platform.PdfReader
import com.saif.scheduleapp.domain.progress.remainingMinutes
import com.saif.scheduleapp.domain.progress.taskProgress
import com.saif.scheduleapp.domain.usecase.GetMissedTasks
import com.saif.scheduleapp.domain.usecase.GetStats
import com.saif.scheduleapp.domain.usecase.GetTodayTasks
import com.saif.scheduleapp.domain.usecase.MarkTaskDone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getTodayTasks: GetTodayTasks,
    private val markTaskDone: MarkTaskDone,
    private val getStats: GetStats,
    private val getMissedTasks: GetMissedTasks? = null,
    private val pdfReader: PdfReader? = null,
    minuteTicker: Flow<Int> = minuteTicker()
) : ViewModel() {

    private val isPdfSupported: Boolean = pdfReader?.isSupported ?: false

    private val _selectedTab = MutableStateFlow(HomeTab.UPCOMING)

    val uiState: StateFlow<HomeUiState> = combine(
        getTodayTasks(),
        minuteTicker,
        getStats(),
        _selectedTab
    ) { tasks, nowMinutes, stats, selectedTab ->
        calculateUiState(
            tasks = tasks,
            nowMinutes = nowMinutes,
            currentStreak = stats.currentStreak,
            selectedTab = selectedTab,
            isPdfImportSupported = isPdfSupported
        )
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HomeUiState(
            currentDate = TimeUtils.currentDateDisplay(),
            isPdfImportSupported = isPdfSupported
        )
    )

    fun onTabSelected(tab: HomeTab) {
        _selectedTab.value = tab
    }

    fun onToggleTaskDone(task: ScheduleTask) {
        viewModelScope.launch(Dispatchers.Default) {
            markTaskDone(taskId = task.id, done = !task.isDone)
        }
    }

    fun onSetTaskDone(taskId: Long, isDone: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            markTaskDone(taskId = taskId, done = isDone)
        }
    }

    fun onMarkTaskDone(taskId: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            markTaskDone(taskId = taskId, done = true)
        }
    }

    fun onCompleteMissedTask(task: ScheduleTask) {
        viewModelScope.launch(Dispatchers.Default) {
            markTaskDone(taskId = task.id, done = true)
        }
    }

    fun onCompleteMissedTask(missedTask: MissedTask) {
        viewModelScope.launch(Dispatchers.Default) {
            markTaskDone(
                taskId = missedTask.task.id,
                done = true,
                date = missedTask.date
            )
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
            selectedTab: HomeTab = HomeTab.UPCOMING,
            missedTasks: List<MissedTask> = emptyList(),
            isPdfImportSupported: Boolean = false
        ): HomeUiState {
            val totalCount = tasks.size
            val doneCount = tasks.count { it.isDone }

            // nowTask is the first not-done task whose state is NOW
            val nowTask = tasks.firstOrNull { task ->
                classifyTask(task, nowMinutes) == TaskState.NOW
            }

            val nowProgress = nowTask?.let { taskProgress(it, nowMinutes) } ?: 0f
            val nowRemainingMinutes = nowTask?.let { remainingMinutes(it, nowMinutes) } ?: 0

            // upcoming contains not-done tasks whose state is UPCOMING (sorted by startMinutes)
            val upcoming = tasks.filter { task ->
                classifyTask(task, nowMinutes) == TaskState.UPCOMING
            }.sortedBy { it.startMinutes }

            // missed contains today's not-done tasks whose state is MISSED (sorted by startMinutes)
            val missed = tasks.filter { task ->
                classifyTask(task, nowMinutes) == TaskState.MISSED
            }.sortedBy { it.startMinutes }

            // doneTasks contains tasks whose state is DONE (sorted by startMinutes)
            val doneTasks = tasks.filter { task ->
                classifyTask(task, nowMinutes) == TaskState.DONE
            }.sortedBy { it.startMinutes }

            return HomeUiState(
                nowTask = nowTask,
                nowProgress = nowProgress,
                nowRemainingMinutes = nowRemainingMinutes,
                upcoming = upcoming,
                missed = missed,
                doneTasks = doneTasks,
                selectedTab = selectedTab,
                missedTasks = missedTasks,
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
