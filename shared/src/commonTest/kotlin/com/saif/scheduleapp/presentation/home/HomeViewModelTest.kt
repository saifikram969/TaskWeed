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

    @Test
    fun testGreetingsByTimeOfDay() {
        assertEquals("Good morning", HomeViewModel.getGreeting(0))
        assertEquals("Good morning", HomeViewModel.getGreeting(500))
        assertEquals("Good morning", HomeViewModel.getGreeting(719))

        assertEquals("Good afternoon", HomeViewModel.getGreeting(720))
        assertEquals("Good afternoon", HomeViewModel.getGreeting(850))
        assertEquals("Good afternoon", HomeViewModel.getGreeting(1019))

        assertEquals("Good evening", HomeViewModel.getGreeting(1020))
        assertEquals("Good evening", HomeViewModel.getGreeting(1200))
        assertEquals("Good evening", HomeViewModel.getGreeting(1439))
    }

    @Test
    fun testNowProgressAndRemainingMinutesCalculated() {
        // Task 2: 600..660 (duration 60). At 630, progress is 30/60 = 0.5f, remaining is 30.
        val state = HomeViewModel.calculateUiState(sampleTasks, nowMinutes = 630)
        assertEquals(2L, state.nowTask?.id)
        assertEquals(0.5f, state.nowProgress)
        assertEquals(30, state.nowRemainingMinutes)
    }

    @Test
    fun testDoneTasksListSeparated() {
        val state = HomeViewModel.calculateUiState(sampleTasks, nowMinutes = 630)
        assertEquals(1, state.doneTasks.size)
        assertEquals(1L, state.doneTasks.first().id)
        assertEquals(true, state.doneTasks.first().isDone)
    }

    @Test
    fun testCurrentStreakPopulated() {
        val state = HomeViewModel.calculateUiState(sampleTasks, nowMinutes = 630, currentStreak = 7)
        assertEquals(7, state.currentStreak)
    }

    @Test
    fun testMissedTasksPopulatedInHomeUiState() {
        val missedTask = com.saif.scheduleapp.domain.model.MissedTask(
            task = sampleTasks[1],
            date = kotlinx.datetime.LocalDate(2026, 11, 5)
        )
        val state = HomeViewModel.calculateUiState(
            tasks = sampleTasks,
            nowMinutes = 630,
            missedTasks = listOf(missedTask)
        )
        assertEquals(1, state.missedTasks.size)
        assertEquals(2L, state.missedTasks.first().task.id)
    }

    @Test
    fun testHomeTabsAndTodayMissedClassification() {
        // At 700 minutes:
        // Task 1 (540..600, isDone=true) -> DONE
        // Task 2 (600..660, isDone=false) -> MISSED (ended at 660, current is 700)
        // Task 3 (720..780, isDone=false) -> UPCOMING (starts at 720)
        val state = HomeViewModel.calculateUiState(
            tasks = sampleTasks,
            nowMinutes = 700,
            selectedTab = HomeTab.UPCOMING
        )

        assertEquals(HomeTab.UPCOMING, state.selectedTab)
        assertNull(state.nowTask)
        assertEquals(1, state.upcoming.size)
        assertEquals(3L, state.upcoming.first().id)

        assertEquals(1, state.missed.size)
        assertEquals(2L, state.missed.first().id)

        assertEquals(1, state.doneTasks.size)
        assertEquals(1L, state.doneTasks.first().id)
    }

    @Test
    fun testSelectedTabCanBeChanged() {
        val stateMissed = HomeViewModel.calculateUiState(
            tasks = sampleTasks,
            nowMinutes = 700,
            selectedTab = HomeTab.MISSED
        )
        assertEquals(HomeTab.MISSED, stateMissed.selectedTab)

        val stateDone = HomeViewModel.calculateUiState(
            tasks = sampleTasks,
            nowMinutes = 700,
            selectedTab = HomeTab.DONE
        )
        assertEquals(HomeTab.DONE, stateDone.selectedTab)
    }

    @Test
    fun testAllTabsListsSortedByStartMinutes() {
        val mixedTasks = listOf(
            ScheduleTask(id = 1L, title = "Upcoming 2", details = "", startMinutes = 800, endMinutes = 850, category = Category.LEARNING, isDone = false),
            ScheduleTask(id = 2L, title = "Upcoming 1", details = "", startMinutes = 700, endMinutes = 750, category = Category.JOBS, isDone = false),
            ScheduleTask(id = 3L, title = "Missed 2", details = "", startMinutes = 400, endMinutes = 450, category = Category.REST, isDone = false),
            ScheduleTask(id = 4L, title = "Missed 1", details = "", startMinutes = 300, endMinutes = 350, category = Category.PRAYER, isDone = false),
            ScheduleTask(id = 5L, title = "Done 2", details = "", startMinutes = 200, endMinutes = 250, category = Category.PRAYER, isDone = true),
            ScheduleTask(id = 6L, title = "Done 1", details = "", startMinutes = 100, endMinutes = 150, category = Category.REST, isDone = true)
        )

        val state = HomeViewModel.calculateUiState(mixedTasks, nowMinutes = 600)

        assertEquals(listOf(700, 800), state.upcoming.map { it.startMinutes })
        assertEquals(listOf(300, 400), state.missed.map { it.startMinutes })
        assertEquals(listOf(100, 200), state.doneTasks.map { it.startMinutes })
    }
}
