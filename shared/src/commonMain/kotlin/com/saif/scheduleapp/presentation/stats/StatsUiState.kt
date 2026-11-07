package com.saif.scheduleapp.presentation.stats

import com.saif.scheduleapp.domain.progress.DayStats

data class StatsUiState(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val averageLast30: Float = 0f,
    val last7: List<DayStats> = emptyList(),
    val isEmpty: Boolean = true,
    val isLoading: Boolean = false
)

