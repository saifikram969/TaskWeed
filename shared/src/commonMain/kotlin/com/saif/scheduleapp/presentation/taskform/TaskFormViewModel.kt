package com.saif.scheduleapp.presentation.taskform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.usecase.AddTask
import com.saif.scheduleapp.domain.usecase.DeleteTask
import com.saif.scheduleapp.domain.usecase.GetTask
import com.saif.scheduleapp.domain.usecase.UpdateTask
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek

class TaskFormViewModel(
    private val getTask: GetTask,
    private val addTask: AddTask,
    private val updateTask: UpdateTask,
    private val deleteTask: DeleteTask,
    taskId: Long? = null,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskFormUiState(taskId = taskId))
    val uiState: StateFlow<TaskFormUiState> = _uiState.asStateFlow()

    init {
        if (taskId != null) {
            loadTask(taskId)
        }
    }

    fun loadTask(id: Long): Job {
        return viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(isLoading = true, taskId = id) }
            val existing = getTask(id)
            if (existing != null) {
                _uiState.update {
                    it.copy(
                        taskId = existing.id,
                        title = existing.title,
                        details = existing.details,
                        startMinutes = existing.startMinutes,
                        endMinutes = existing.endMinutes,
                        category = existing.category,
                        repeatDays = existing.repeatDays,
                        voiceEnabled = existing.voiceEnabled,
                        isLoading = false,
                        error = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Task not found"
                    )
                }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle, error = null) }
    }

    fun onDetailsChange(newDetails: String) {
        _uiState.update { it.copy(details = newDetails) }
    }

    fun onStartChange(newStartMinutes: Int) {
        _uiState.update { it.copy(startMinutes = newStartMinutes, error = null) }
    }

    fun onEndChange(newEndMinutes: Int) {
        _uiState.update { it.copy(endMinutes = newEndMinutes, error = null) }
    }

    fun onCategoryChange(newCategory: Category) {
        _uiState.update { it.copy(category = newCategory) }
    }

    fun onDayToggle(day: DayOfWeek) {
        _uiState.update { current ->
            val updatedDays = if (day in current.repeatDays) {
                current.repeatDays - day
            } else {
                current.repeatDays + day
            }
            current.copy(repeatDays = updatedDays, error = null)
        }
    }

    fun onSetRepeatDays(days: Set<DayOfWeek>) {
        _uiState.update { it.copy(repeatDays = days, error = null) }
    }

    fun onVoiceEnabledChange(enabled: Boolean) {
        _uiState.update { it.copy(voiceEnabled = enabled) }
    }

    fun onSaveClick(): Job {
        val current = _uiState.value
        val task = ScheduleTask(
            id = current.taskId ?: 0L,
            title = current.title,
            details = current.details,
            startMinutes = current.startMinutes,
            endMinutes = current.endMinutes,
            category = current.category,
            isDone = false,
            repeatDays = current.repeatDays,
            voiceEnabled = current.voiceEnabled
        )

        return viewModelScope.launch(dispatcher) {
            if (current.isEditMode) {
                val result = updateTask(task)
                result.fold(
                    onSuccess = {
                        _uiState.update { it.copy(saved = true, error = null) }
                    },
                    onFailure = { throwable ->
                        _uiState.update { it.copy(error = throwable.message ?: "Failed to update task") }
                    }
                )
            } else {
                val result = addTask(task)
                result.fold(
                    onSuccess = {
                        _uiState.update { it.copy(saved = true, error = null) }
                    },
                    onFailure = { throwable ->
                        _uiState.update { it.copy(error = throwable.message ?: "Failed to add task") }
                    }
                )
            }
        }
    }

    fun onDeleteClick(): Job {
        val currentId = _uiState.value.taskId ?: return viewModelScope.launch {}
        return viewModelScope.launch(dispatcher) {
            try {
                deleteTask(currentId)
                _uiState.update { it.copy(deleted = true, error = null) }
            } catch (e: Throwable) {
                _uiState.update { it.copy(error = e.message ?: "Failed to delete task") }
            }
        }
    }
}

