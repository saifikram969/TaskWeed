package com.saif.scheduleapp.presentation.addtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.usecase.AddTask
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddTaskViewModel(
    private val addTask: AddTask,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTaskUiState())
    val uiState: StateFlow<AddTaskUiState> = _uiState.asStateFlow()

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

    fun onSaveClick(): Job {
        val current = _uiState.value
        val task = ScheduleTask(
            id = 0L,
            title = current.title,
            details = current.details,
            startMinutes = current.startMinutes,
            endMinutes = current.endMinutes,
            category = current.category,
            isDone = false
        )

        return viewModelScope.launch(dispatcher) {
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
