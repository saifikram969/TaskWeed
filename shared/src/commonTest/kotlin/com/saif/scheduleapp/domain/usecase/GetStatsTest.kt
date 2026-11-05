package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.data.repository.InMemoryScheduleRepository
import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GetStatsTest {

    private val testDate = LocalDate(2026, 11, 4) // Wednesday

    private fun createTask(
        id: Long = 0L,
        title: String = "Task",
        startMinutes: Int = 540,
        endMinutes: Int = 600,
        category: Category = Category.LEARNING,
        repeatDays: Set<DayOfWeek> = DayOfWeek.entries.toSet()
    ) = ScheduleTask(
        id = id,
        title = title,
        details = "",
        startMinutes = startMinutes,
        endMinutes = endMinutes,
        category = category,
        repeatDays = repeatDays
    )

    @Test
    fun day_when_all_active_tasks_are_completed() = runTest {
        val repository = InMemoryScheduleRepository()
        val id1 = repository.addTask(createTask(title = "Task 1"))
        val id2 = repository.addTask(createTask(title = "Task 2"))

        repository.setDone(id1, testDate, true)
        repository.setDone(id2, testDate, true)

        val getStats = GetStats(repository = repository)
        val stats = getStats(testDate).first()

        assertEquals(2, stats.today.total)
        assertEquals(2, stats.today.done)
        assertTrue(stats.today.isComplete)
        assertEquals(1, stats.currentStreak)
    }

    @Test
    fun day_with_exactly_80_percent_completion() = runTest {
        val repository = InMemoryScheduleRepository()
        val ids = (1..5).map { i ->
            repository.addTask(createTask(title = "Task $i"))
        }

        // Complete 4 out of 5 = exactly 80%
        ids.take(4).forEach { id ->
            repository.setDone(id, testDate, true)
        }

        val getStats = GetStats(repository = repository)
        val stats = getStats(testDate).first()

        assertEquals(5, stats.today.total)
        assertEquals(4, stats.today.done)
        assertTrue(stats.today.isComplete)
        assertEquals(1, stats.currentStreak)
    }

    @Test
    fun deleted_tasks_completion_is_ignored() = runTest {
        val repository = InMemoryScheduleRepository()
        val id1 = repository.addTask(createTask(title = "Task 1"))
        val id2 = repository.addTask(createTask(title = "Task 2"))

        repository.setDone(id1, testDate, true)
        repository.setDone(id2, testDate, true)

        // Delete task 2
        repository.deleteTask(id2)

        val getStats = GetStats(repository = repository)
        val stats = getStats(testDate).first()

        // Only Task 1 exists now; Task 2's completion must be ignored
        assertEquals(1, stats.today.total)
        assertEquals(1, stats.today.done)
        assertTrue(stats.today.isComplete)
    }

    @Test
    fun task_inactive_on_given_weekday_is_not_counted() = runTest {
        val repository = InMemoryScheduleRepository()
        // 2026-11-04 is Wednesday
        assertEquals(DayOfWeek.WEDNESDAY, testDate.dayOfWeek)

        // Active only on Wednesday
        val idActive = repository.addTask(
            createTask(
                title = "Active Wednesday",
                repeatDays = setOf(DayOfWeek.WEDNESDAY)
            )
        )
        // Inactive on Wednesday (active only on Sunday)
        val idInactive = repository.addTask(
            createTask(
                title = "Sunday Only",
                repeatDays = setOf(DayOfWeek.SUNDAY)
            )
        )

        // Mark both done
        repository.setDone(idActive, testDate, true)
        repository.setDone(idInactive, testDate, true)

        val getStats = GetStats(repository = repository)
        val stats = getStats(testDate).first()

        // Only the Wednesday task should be counted in total and done
        assertEquals(1, stats.today.total)
        assertEquals(1, stats.today.done)
        assertTrue(stats.today.isComplete)
    }

    @Test
    fun last7_is_ordered_from_oldest_to_newest() = runTest {
        val repository = InMemoryScheduleRepository()
        val getStats = GetStats(repository = repository)
        val stats = getStats(testDate).first()

        assertEquals(7, stats.last7.size)
        // Oldest is testDate - 6 days
        assertEquals(LocalDate(2026, 10, 29), stats.last7.first().date)
        // Newest is testDate
        assertEquals(testDate, stats.last7.last().date)

        for (i in 0 until stats.last7.size - 1) {
            assertTrue(stats.last7[i].date < stats.last7[i + 1].date)
        }
    }
}

