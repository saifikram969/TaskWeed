package com.saif.scheduleapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saif.scheduleapp.core.util.TimeUtils
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.usecase.GetTodayTasks
import com.saif.scheduleapp.domain.usecase.MarkTaskDone
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getTodayTasks: GetTodayTasks,
    private val markTaskDone: MarkTaskDone
) : ViewModel() {

    private val _currentMinutes = MutableStateFlow(TimeUtils.currentMinutesSinceMidnight())

    init {
        viewModelScope.launch {
            while (isActive) {
                _currentMinutes.value = TimeUtils.currentMinutesSinceMidnight()
                delay(10_000L)
            }
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        getTodayTasks(),
        _currentMinutes
    ) { tasks, nowMinutes ->
        calculateUiState(tasks, nowMinutes)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HomeUiState(currentDate = TimeUtils.currentDateDisplay())
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
        fun calculateUiState(tasks: List<ScheduleTask>, nowMinutes: Int): HomeUiState {
            val totalCount = tasks.size
            val doneCount = tasks.count { it.isDone }

            // nowTask is the first not-done task whose time range contains the current time
            // Interval: startMinutes <= nowMinutes && nowMinutes < endMinutes
            val nowTask = tasks.firstOrNull { task ->
                !task.isDone && TimeUtils.isCurrent(task.startMinutes, task.endMinutes, nowMinutes)
            }

            // upcoming contains not-done tasks whose startMinutes are strictly greater than current time
            // Excludes current task
            val upcoming = tasks.filter { task ->
                !task.isDone &&
                task.id != nowTask?.id &&
                TimeUtils.isUpcoming(task.startMinutes, nowMinutes)
            }

            return HomeUiState(
                nowTask = nowTask,
                upcoming = upcoming,
                doneCount = doneCount,
                totalCount = totalCount,
                currentDate = TimeUtils.currentDateDisplay()
            )
        }
    }
}
