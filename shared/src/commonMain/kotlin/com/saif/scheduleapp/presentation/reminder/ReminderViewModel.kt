package com.saif.scheduleapp.presentation.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReminderViewModel(
    private val repository: ScheduleRepository,
    private val scheduler: ReminderScheduler,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderUiState())
    val uiState: StateFlow<ReminderUiState> = _uiState.asStateFlow()

    private var speakingJob: Job? = null

    fun loadTask(taskId: Long) {
        viewModelScope.launch(dispatcher) {
            val tasks = repository.getAllTasks()
            val matchedTask = tasks.find { it.id == taskId }
            if (matchedTask != null) {
                _uiState.update { it.copy(task = matchedTask) }
                triggerSpeakingCountdown(matchedTask.voiceEnabled)
            }
        }
    }

    fun setTask(task: ScheduleTask) {
        _uiState.update { it.copy(task = task) }
        triggerSpeakingCountdown(task.voiceEnabled)
    }

    fun setSpeaking(speaking: Boolean) {
        speakingJob?.cancel()
        _uiState.update { it.copy(isSpeaking = speaking) }
    }

    fun triggerSpeakingCountdown(voiceEnabled: Boolean) {
        speakingJob?.cancel()
        if (voiceEnabled) {
            speakingJob = viewModelScope.launch(dispatcher) {
                _uiState.update { it.copy(isSpeaking = true) }
                delay(8000L)
                _uiState.update { it.copy(isSpeaking = false) }
            }
        } else {
            _uiState.update { it.copy(isSpeaking = false) }
        }
    }

    fun onStartNow(onFinish: () -> Unit) {
        speakingJob?.cancel()
        onFinish()
    }

    fun onSnooze(minutes: Int = 5, onFinish: () -> Unit) {
        speakingJob?.cancel()
        val currentTask = _uiState.value.task
        if (currentTask != null) {
            scheduler.snooze(currentTask, minutes)
        }
        onFinish()
    }

    override fun onCleared() {
        speakingJob?.cancel()
        super.onCleared()
    }
}

