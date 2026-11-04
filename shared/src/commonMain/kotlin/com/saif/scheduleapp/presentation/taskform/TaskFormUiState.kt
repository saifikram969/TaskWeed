package com.saif.scheduleapp.presentation.taskform

import com.saif.scheduleapp.domain.model.Category
import kotlinx.datetime.DayOfWeek

data class TaskFormUiState(
    val taskId: Long? = null,
    val title: String = "",
    val details: String = "",
    val startMinutes: Int = 540,
    val endMinutes: Int = 600,
    val category: Category = Category.LEARNING,
    val repeatDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    val voiceEnabled: Boolean = true,
    val error: String? = null,
    val saved: Boolean = false,
    val deleted: Boolean = false,
    val isLoading: Boolean = false
) {
    val isEditMode: Boolean get() = taskId != null
}

