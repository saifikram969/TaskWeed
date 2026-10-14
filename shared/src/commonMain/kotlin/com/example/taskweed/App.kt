package com.example.taskweed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.saif.scheduleapp.core.designsystem.ScheduleAppTheme
import com.saif.scheduleapp.core.di.appModule
import com.saif.scheduleapp.presentation.home.HomeScreen
import com.saif.scheduleapp.presentation.home.HomeViewModel
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        ScheduleAppTheme {
            val viewModel: HomeViewModel = koinInject()
            val state by viewModel.uiState.collectAsState()
            HomeScreen(
                state = state,
                onToggleTask = viewModel::onToggleTaskDone
            )
        }
    }
}
