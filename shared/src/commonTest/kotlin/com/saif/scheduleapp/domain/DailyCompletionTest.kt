package com.saif.scheduleapp.domain

import com.saif.scheduleapp.core.util.dateTicker
import com.saif.scheduleapp.data.repository.InMemoryScheduleRepository
import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.usecase.GetTodayTasks
import com.saif.scheduleapp.domain.usecase.MarkTaskDone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DailyCompletionTest {

    private class FakeClock(var currentInstant: Instant) : Clock {
        override fun now(): Instant = currentInstant
    }

    private val sampleTask = ScheduleTask(
        id = 1L,
        title = "Study Multiplatform",
        details = "Read specs",
        startMinutes = 540,
        endMinutes = 600,
        category = Category.LEARNING,
        isDone = false
    )

    @Test
    fun testTaskMarkedDoneTodayIsNotDoneTomorrow() = runTest {
        val repository = InMemoryScheduleRepository(listOf(sampleTask))
        val today = LocalDate(2026, 11, 1)
        val tomorrow = LocalDate(2026, 11, 2)

        // Initial state: not done on today or tomorrow
        val initialTodayTasks = repository.observeTasks(today).first()
        assertFalse(initialTodayTasks.first { it.id == 1L }.isDone)
        val initialTomorrowTasks = repository.observeTasks(tomorrow).first()
        assertFalse(initialTomorrowTasks.first { it.id == 1L }.isDone)

        // Mark done today
        repository.setDone(taskId = 1L, date = today, done = true)

        // Verify done today
        val todayTasksAfter = repository.observeTasks(today).first()
        assertTrue(todayTasksAfter.first { it.id == 1L }.isDone)

        // Verify still NOT done tomorrow
        val tomorrowTasksAfter = repository.observeTasks(tomorrow).first()
        assertFalse(tomorrowTasksAfter.first { it.id == 1L }.isDone)
    }

    @Test
    fun testGetTodayTasksResetsWhenDateChanges() = runTest {
        val repository = InMemoryScheduleRepository(listOf(sampleTask))
        val markTaskDone = MarkTaskDone(repository)

        val today = LocalDate(2026, 11, 1)
        val tomorrow = LocalDate(2026, 11, 2)
        val dateFlow = MutableStateFlow(today)

        val getTodayTasks = GetTodayTasks(repository = repository, dateFlow = dateFlow)

        val observedStates = mutableListOf<List<ScheduleTask>>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            getTodayTasks().collect { observedStates.add(it) }
        }

        // Initially not done
        assertFalse(observedStates.last().first { it.id == 1L }.isDone)

        // Mark done today
        markTaskDone(taskId = 1L, done = true, date = today)
        assertTrue(observedStates.last().first { it.id == 1L }.isDone)

        // Advance date to tomorrow
        dateFlow.value = tomorrow

        // Automatically uncompleted for tomorrow!
        assertFalse(observedStates.last().first { it.id == 1L }.isDone)

        job.cancel()
    }

    @Test
    fun testDateTickerEmitsAgainWhenDateChangesWithFakeClock() = runTest {
        // Start 10 seconds before midnight UTC
        val startInstant = Instant.parse("2026-11-01T23:59:50Z")
        val fakeClock = FakeClock(startInstant)

        val emissions = mutableListOf<LocalDate>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            dateTicker(timeZone = TimeZone.UTC, clock = fakeClock).collect {
                emissions.add(it)
            }
        }

        // Immediately emits today's date
        assertEquals(1, emissions.size)
        assertEquals(LocalDate(2026, 11, 1), emissions[0])

        // Advance clock across midnight
        fakeClock.currentInstant = startInstant + 20.seconds
        testScheduler.advanceTimeBy(15_000L) // advance past the minute boundary delay

        // Emits tomorrow's date
        assertEquals(2, emissions.size)
        assertEquals(LocalDate(2026, 11, 2), emissions[1])

        job.cancel()
    }
}

