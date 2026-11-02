package com.saif.scheduleapp.presentation.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReminderViewModel(
    private val repository: ScheduleRepository,
    private val scheduler: ReminderScheduler,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderUiState())
    val uiState: StateFlow<ReminderUiState> = _uiState.asStateFlow()

    fun loadTask(taskId: Long) {
        viewModelScope.launch(dispatcher) {
            val tasks = repository.getAllTasks()
            val matchedTask = tasks.find { it.id == taskId }
            if (matchedTask != null) {
                _uiState.update { it.copy(task = matchedTask) }
            }
        }
    }

    fun setTask(task: ScheduleTask) {
        _uiState.update { it.copy(task = task) }
    }

    fun setSpeaking(speaking: Boolean) {
        _uiState.update { it.copy(isSpeaking = speaking) }
    }

    fun onStartNow(onFinish: () -> Unit) {
        onFinish()
    }

    fun onSnooze(minutes: Int = 5, onFinish: () -> Unit) {
        val currentTask = _uiState.value.task
        if (currentTask != null) {
            scheduler.snooze(currentTask, minutes)
        }
        onFinish()
    }
}

