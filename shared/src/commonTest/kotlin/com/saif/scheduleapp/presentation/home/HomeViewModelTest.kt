package com.saif.scheduleapp.presentation.home

import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import com.saif.scheduleapp.domain.usecase.GetTodayTasks
import com.saif.scheduleapp.domain.usecase.MarkTaskDone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val sampleTasks = listOf(
        ScheduleTask(
            id = 1L,
            title = "Morning Task",
            details = "Details 1",
            startMinutes = 540,  // 09:00 AM
            endMinutes = 600,    // 10:00 AM
            category = Category.LEARNING,
            isDone = true
        ),
        ScheduleTask(
            id = 2L,
            title = "Current Midday Task",
            details = "Details 2",
            startMinutes = 600,  // 10:00 AM
            endMinutes = 660,    // 11:00 AM
            category = Category.JOBS,
            isDone = false
        ),
        ScheduleTask(
            id = 3L,
            title = "Upcoming Afternoon Task",
            details = "Details 3",
            startMinutes = 720,  // 12:00 PM
            endMinutes = 780,    // 01:00 PM
            category = Category.PRAYER,
            isDone = false
        )
    )

    @Test
    fun testNowTaskSelectedCorrectly() {
        // At 10:30 AM (630 mins), task 2 is current
        val state = HomeViewModel.calculateUiState(sampleTasks, nowMinutes = 630)

        assertEquals(2L, state.nowTask?.id)
        assertEquals("Current Midday Task", state.nowTask?.title)
        assertEquals(1, state.doneCount)
        assertEquals(3, state.totalCount)

        // Upcoming should NOT include nowTask (id 2)
        assertEquals(1, state.upcoming.size)
        assertEquals(3L, state.upcoming.first().id)
    }

    @Test
    fun testCompletedTaskNotSelectedAsNowTask() {
        // At 09:30 AM (570 mins), task 1 is done, so nowTask should be null
        val state = HomeViewModel.calculateUiState(sampleTasks, nowMinutes = 570)

        assertNull(state.nowTask)
        // Upcoming should have tasks 2 and 3
        assertEquals(2, state.upcoming.size)
        assertEquals(2L, state.upcoming[0].id)
        assertEquals(3L, state.upcoming[1].id)
    }

    @Test
    fun testNoUpcomingTasksWhenDayEnded() {
        // At 11:59 PM (1439 mins)
        val state = HomeViewModel.calculateUiState(sampleTasks, nowMinutes = 1439)

        assertNull(state.nowTask)
        assertEquals(0, state.upcoming.size)
        assertEquals(1, state.doneCount)
        assertEquals(3, state.totalCount)
    }

    @Test
    fun testEmptyTasksProducesEmptyState() {
        val state = HomeViewModel.calculateUiState(emptyList(), nowMinutes = 540)

        assertNull(state.nowTask)
        assertEquals(0, state.upcoming.size)
        assertEquals(0, state.doneCount)
        assertEquals(0, state.totalCount)
    }

    @Test
    fun testUpcomingTasksSortedByAscendingStartTime() {
        val unorderedTasks = listOf(
            ScheduleTask(id = 1L, title = "Task C", details = "", startMinutes = 800, endMinutes = 850, category = Category.LEARNING),
            ScheduleTask(id = 2L, title = "Task A", details = "", startMinutes = 600, endMinutes = 650, category = Category.JOBS),
            ScheduleTask(id = 3L, title = "Task B", details = "", startMinutes = 700, endMinutes = 750, category = Category.REST)
        )

        val state = HomeViewModel.calculateUiState(unorderedTasks, nowMinutes = 500)

        assertEquals(3, state.upcoming.size)
        assertEquals(listOf(600, 700, 800), state.upcoming.map { it.startMinutes })
        assertEquals(listOf("Task A", "Task B", "Task C"), state.upcoming.map { it.title })
    }

    @Test
    fun testNowTaskSelectionAtThreeDifferentTimesUsingFakeTimes() {
        // Time 1: 550 mins (09:10 AM) - Before task 2 (600..660), task 1 (540..600) is done
        val stateAtTime1 = HomeViewModel.calculateUiState(sampleTasks, nowMinutes = 550)
        assertNull(stateAtTime1.nowTask, "At 550 mins, no active task because task 1 is already done and task 2 starts at 600")
        assertEquals(2, stateAtTime1.upcoming.size)
        assertEquals(2L, stateAtTime1.upcoming[0].id)
        assertEquals(3L, stateAtTime1.upcoming[1].id)

        // Time 2: 630 mins (10:30 AM) - Inside task 2 (600..660)
        val stateAtTime2 = HomeViewModel.calculateUiState(sampleTasks, nowMinutes = 630)
        assertEquals(2L, stateAtTime2.nowTask?.id, "At 630 mins, task 2 should be active as nowTask")
        assertEquals("Current Midday Task", stateAtTime2.nowTask?.title)
        assertEquals(1, stateAtTime2.upcoming.size, "Upcoming should exclude nowTask")
        assertEquals(3L, stateAtTime2.upcoming[0].id)

        // Time 3: 750 mins (12:30 PM) - Task 2 ended at 660, inside task 3 (720..780)
        val stateAtTime3 = HomeViewModel.calculateUiState(sampleTasks, nowMinutes = 750)
        assertEquals(3L, stateAtTime3.nowTask?.id, "At 750 mins, task 3 should be active as nowTask")
        assertEquals("Upcoming Afternoon Task", stateAtTime3.nowTask?.title)
        assertEquals(0, stateAtTime3.upcoming.size, "No upcoming tasks remain after task 3")
    }

    @Test
    fun testFlowUpdatesNowTaskAsTimeChangesAcrossThreeFakeTimes() = kotlinx.coroutines.test.runTest {
        val fakeRepo = object : ScheduleRepository {
            private val _tasks = MutableStateFlow(sampleTasks)
            override fun observeTasks(date: kotlinx.datetime.LocalDate): Flow<List<ScheduleTask>> = _tasks
            override suspend fun setDone(taskId: Long, date: kotlinx.datetime.LocalDate, done: Boolean) {}
            override suspend fun addTask(task: ScheduleTask): Long = 1L
            override suspend fun clearAllTasks() {}
            override suspend fun getAllTasks(): List<ScheduleTask> = _tasks.value
        }

        val fakeTicker = MutableStateFlow(550)
        val states = mutableListOf<HomeUiState>()
        val job = backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            combine(GetTodayTasks(fakeRepo)(), fakeTicker) { tasks, nowMinutes ->
                HomeViewModel.calculateUiState(tasks, nowMinutes)
            }.collect { states.add(it) }
        }

        // Time 1: 550 minutes (09:10 AM) - Before task 2 starts
        assertEquals(1, states.size)
        assertNull(states.last().nowTask)
        assertEquals(2, states.last().upcoming.size)
        assertEquals(2L, states.last().upcoming[0].id)
        assertEquals(3L, states.last().upcoming[1].id)

        // Time 2: 630 minutes (10:30 AM) - During task 2
        fakeTicker.value = 630
        assertEquals(2, states.size)
        assertEquals(2L, states.last().nowTask?.id)
        assertEquals("Current Midday Task", states.last().nowTask?.title)
        assertEquals(1, states.last().upcoming.size)
        assertEquals(3L, states.last().upcoming[0].id)

        // Time 3: 750 minutes (12:30 PM) - During task 3 (after task 2 ended)
        fakeTicker.value = 750
        assertEquals(3, states.size)
        assertEquals(3L, states.last().nowTask?.id)
        assertEquals("Upcoming Afternoon Task", states.last().nowTask?.title)
        assertEquals(0, states.last().upcoming.size)

        job.cancel()
    }
}

