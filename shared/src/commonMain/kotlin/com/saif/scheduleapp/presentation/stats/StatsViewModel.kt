package com.saif.scheduleapp.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saif.scheduleapp.domain.usecase.GetStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class StatsViewModel(
    getStats: GetStats
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = getStats()
        .map { stats ->
            val hasTasks = stats.last7.any { it.total > 0 } ||
                stats.currentStreak > 0 ||
                stats.longestStreak > 0 ||
                stats.averageLast30 > 0f

            StatsUiState(
                currentStreak = stats.currentStreak,
                longestStreak = stats.longestStreak,
                averageLast30 = stats.averageLast30,
                last7 = stats.last7,
                isEmpty = !hasTasks,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = StatsUiState(isLoading = true)
        )
}

