package com.saif.scheduleapp.presentation.home

import com.saif.scheduleapp.domain.model.ScheduleTask

data class HomeUiState(
    val nowTask: ScheduleTask? = null,
    val upcoming: List<ScheduleTask> = emptyList(),
    val doneCount: Int = 0,
    val totalCount: Int = 0,
    val currentDate: String = ""
)
