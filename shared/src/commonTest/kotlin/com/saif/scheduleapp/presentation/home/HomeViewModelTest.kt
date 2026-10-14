package com.saif.scheduleapp.presentation.home

import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
}
