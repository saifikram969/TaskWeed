package com.saif.scheduleapp.presentation.home

import com.saif.scheduleapp.domain.model.ScheduleTask

data class HomeUiState(
    val nowTask: ScheduleTask? = null,
    val nowProgress: Float = 0f,
    val nowRemainingMinutes: Int = 0,
    val upcoming: List<ScheduleTask> = emptyList(),
    val doneTasks: List<ScheduleTask> = emptyList(),
    val doneCount: Int = 0,
    val totalCount: Int = 0,
    val currentStreak: Int = 0,
    val greeting: String = "Good morning",
    val currentDate: String = "",
    val isPdfImportSupported: Boolean = false
)
