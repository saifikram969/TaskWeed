package com.saif.scheduleapp.presentation.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.saif.scheduleapp.core.designsystem.ScheduleTheme
import com.saif.scheduleapp.presentation.home.HomeScreen
import com.saif.scheduleapp.presentation.home.HomeViewModel
import com.saif.scheduleapp.presentation.importschedule.ImportScreen
import com.saif.scheduleapp.presentation.importschedule.ImportStage
import com.saif.scheduleapp.presentation.importschedule.ImportViewModel
import com.saif.scheduleapp.presentation.stats.StatsScreen
import com.saif.scheduleapp.presentation.stats.StatsViewModel
import com.saif.scheduleapp.presentation.taskform.TaskFormScreen
import com.saif.scheduleapp.presentation.taskform.TaskFormViewModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import taskweed.shared.generated.resources.Res
import taskweed.shared.generated.resources.ic_nav_stats
import taskweed.shared.generated.resources.ic_nav_today

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isToday = currentDestination?.hierarchy?.any { it.hasRoute(HomeRoute::class) } == true
    val isStats = currentDestination?.hierarchy?.any { it.hasRoute(StatsRoute::class) } == true
    val showBottomBar = isToday || isStats

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ScheduleTheme.colors.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = ScheduleTheme.colors.surface,
                    contentColor = ScheduleTheme.colors.text,
                    tonalElevation = 0.dp,
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = ScheduleTheme.colors.surfaceVariant
                    )
                ) {
                    NavigationBarItem(
                        selected = isToday,
                        onClick = {
                            if (!isToday) {
                                navController.navigate(HomeRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_nav_today),
                                contentDescription = "Today"
                            )
                        },
                        label = { Text("Today") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ScheduleTheme.colors.primary,
                            selectedTextColor = ScheduleTheme.colors.primary,
                            indicatorColor = ScheduleTheme.colors.primaryMuted,
                            unselectedIconColor = ScheduleTheme.colors.textMuted,
                            unselectedTextColor = ScheduleTheme.colors.textMuted
                        )
                    )

                    NavigationBarItem(
                        selected = isStats,
                        onClick = {
                            if (!isStats) {
                                navController.navigate(StatsRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_nav_stats),
                                contentDescription = "Stats"
                            )
                        },
                        label = { Text("Stats") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ScheduleTheme.colors.primary,
                            selectedTextColor = ScheduleTheme.colors.primary,
                            indicatorColor = ScheduleTheme.colors.primaryMuted,
                            unselectedIconColor = ScheduleTheme.colors.textMuted,
                            unselectedTextColor = ScheduleTheme.colors.textMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable<HomeRoute> {
                val viewModel: HomeViewModel = koinInject()
                val state by viewModel.uiState.collectAsState()
                HomeScreen(
                    state = state,
                    onToggleTask = viewModel::onToggleTaskDone,
                    onNavigateToAddTask = {
                        navController.navigate(TaskFormRoute())
                    },
                    onNavigateToEditTask = { taskId ->
                        navController.navigate(TaskFormRoute(taskId = taskId))
                    },
                    onNavigateToImport = {
                        navController.navigate(ImportRoute)
                    }
                )
            }

            composable<StatsRoute> {
                val viewModel: StatsViewModel = koinInject()
                val state by viewModel.uiState.collectAsState()
                StatsScreen(
                    state = state
                )
            }

            composable<TaskFormRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<TaskFormRoute>()
                val viewModel: TaskFormViewModel = koinInject { parametersOf(route.taskId) }
                val state by viewModel.uiState.collectAsState()

                LaunchedEffect(state.saved, state.deleted) {
                    if (state.saved || state.deleted) {
                        navController.popBackStack()
                    }
                }

                TaskFormScreen(
                    state = state,
                    onTitleChange = viewModel::onTitleChange,
                    onDetailsChange = viewModel::onDetailsChange,
                    onStartChange = viewModel::onStartChange,
                    onEndChange = viewModel::onEndChange,
                    onCategoryChange = viewModel::onCategoryChange,
                    onDayToggle = viewModel::onDayToggle,
                    onSetRepeatDays = viewModel::onSetRepeatDays,
                    onVoiceEnabledChange = viewModel::onVoiceEnabledChange,
                    onSaveClick = viewModel::onSaveClick,
                    onDeleteClick = viewModel::onDeleteClick,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable<ImportRoute> {
                val viewModel: ImportViewModel = koinInject()
                val state by viewModel.uiState.collectAsState()

                LaunchedEffect(state.stage) {
                    if (state.stage == ImportStage.Done) {
                        navController.popBackStack()
                    }
                }

                ImportScreen(
                    state = state,
                    onPdfPicked = viewModel::onPdfPicked,
                    onRowToggle = viewModel::onRowToggle,
                    onRowEdit = viewModel::onRowEdit,
                    onReplaceToggle = viewModel::onReplaceToggle,
                    onSaveClick = viewModel::onSaveClick,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
