package com.saif.scheduleapp.presentation.addtask

import com.saif.scheduleapp.domain.model.Category

data class AddTaskUiState(
    val title: String = "",
    val details: String = "",
    val startMinutes: Int = 540,
    val endMinutes: Int = 600,
    val category: Category = Category.LEARNING,
    val error: String? = null,
    val saved: Boolean = false
)
