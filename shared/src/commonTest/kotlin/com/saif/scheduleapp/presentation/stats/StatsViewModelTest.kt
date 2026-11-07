package com.saif.scheduleapp.presentation.stats

import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.progress.DayStats
import com.saif.scheduleapp.domain.progress.Stats
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import com.saif.scheduleapp.domain.usecase.GetStats
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    private fun createGetStats(
        allTasks: List<ScheduleTask>,
        completionsMap: Map<LocalDate, Set<Long>>,
        today: LocalDate
    ): GetStats {
        val repo = object : ScheduleRepository {
            override fun observeAllTasks(): Flow<List<ScheduleTask>> = flowOf(allTasks)
            override fun observeCompletions(from: LocalDate, to: LocalDate): Flow<Map<LocalDate, Set<Long>>> = flowOf(completionsMap)
            override fun observeTasks(date: LocalDate): Flow<List<ScheduleTask>> = flowOf(emptyList())
            override suspend fun setDone(taskId: Long, date: LocalDate, done: Boolean) {}
            override suspend fun addTask(task: ScheduleTask): Long = 1L
            override suspend fun clearAllTasks() {}
            override suspend fun getAllTasks(): List<ScheduleTask> = allTasks
        }
        return GetStats(repository = repo, dateFlow = flowOf(today))
    }

    @Test
    fun testEmptyStatsProducesEmptyState() = runTest {
        val getStats = createGetStats(
            allTasks = emptyList(),
            completionsMap = emptyMap(),
            today = LocalDate(2026, 11, 5)
        )
        val viewModel = StatsViewModel(getStats)
        val state = viewModel.uiState.first { !it.isLoading }

        assertTrue(state.isEmpty)
        assertEquals(0, state.currentStreak)
        assertEquals(0, state.longestStreak)
        assertEquals(0f, state.averageLast30)
    }

    @Test
    fun testStatsWithTasksProducesPopulatedState() = runTest {
        val today = LocalDate(2026, 11, 5)
        val task1 = ScheduleTask(
            id = 1L,
            title = "Task 1",
            details = "",
            startMinutes = 600,
            endMinutes = 660,
            category = Category.LEARNING
        )
        val getStats = createGetStats(
            allTasks = listOf(task1),
            completionsMap = mapOf(today to setOf(1L)),
            today = today
        )
        val viewModel = StatsViewModel(getStats)
        val state = viewModel.uiState.first { !it.isLoading }

        assertFalse(state.isEmpty)
        assertEquals(1, state.currentStreak)
        assertEquals(1, state.longestStreak)
        assertEquals(7, state.last7.size)
    }
}
