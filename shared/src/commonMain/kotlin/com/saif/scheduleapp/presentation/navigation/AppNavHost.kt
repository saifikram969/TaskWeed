package com.saif.scheduleapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.LaunchedEffect
import com.saif.scheduleapp.presentation.addtask.AddTaskScreen
import com.saif.scheduleapp.presentation.addtask.AddTaskViewModel
import com.saif.scheduleapp.presentation.home.HomeScreen
import com.saif.scheduleapp.presentation.home.HomeViewModel
import org.koin.compose.koinInject

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier
    ) {
        composable<HomeRoute> {
            val viewModel: HomeViewModel = koinInject()
            val state by viewModel.uiState.collectAsState()
            HomeScreen(
                state = state,
                onToggleTask = viewModel::onToggleTaskDone,
                onNavigateToAddTask = {
                    navController.navigate(AddTaskRoute)
                }
            )
        }

        composable<AddTaskRoute> {
            val viewModel: AddTaskViewModel = koinInject()
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(state.saved) {
                if (state.saved) {
                    navController.popBackStack()
                }
            }

            AddTaskScreen(
                state = state,
                onTitleChange = viewModel::onTitleChange,
                onDetailsChange = viewModel::onDetailsChange,
                onStartChange = viewModel::onStartChange,
                onEndChange = viewModel::onEndChange,
                onCategoryChange = viewModel::onCategoryChange,
                onSaveClick = viewModel::onSaveClick,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
