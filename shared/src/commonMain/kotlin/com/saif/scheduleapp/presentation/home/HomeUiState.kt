package com.saif.scheduleapp.presentation.home

import com.saif.scheduleapp.domain.model.MissedTask
import com.saif.scheduleapp.domain.model.ScheduleTask

enum class HomeTab {
    UPCOMING,
    MISSED,
    DONE
}

data class HomeUiState(
    val nowTask: ScheduleTask? = null,
    val nowProgress: Float = 0f,
    val nowRemainingMinutes: Int = 0,
    val upcoming: List<ScheduleTask> = emptyList(),
    val missed: List<ScheduleTask> = emptyList(),
    val doneTasks: List<ScheduleTask> = emptyList(),
    val selectedTab: HomeTab = HomeTab.UPCOMING,
    val missedTasks: List<MissedTask> = emptyList(),
    val doneCount: Int = 0,
    val totalCount: Int = 0,
    val currentStreak: Int = 0,
    val greeting: String = "Good morning",
    val currentDate: String = "",
    val isPdfImportSupported: Boolean = false
)
