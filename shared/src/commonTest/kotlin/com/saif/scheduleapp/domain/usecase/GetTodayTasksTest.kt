package com.saif.scheduleapp.domain.usecase

import com.saif.scheduleapp.data.repository.InMemoryScheduleRepository
import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetTodayTasksTest {

    @Test
    fun testFiltersTasksUsingIsActiveOnDate() = runTest {
        val repository = InMemoryScheduleRepository(initialTasks = emptyList())

        // Monday-only task
        val mondayTask = ScheduleTask(
            id = 1L,
            title = "Monday Planning",
            details = "Sprint planning",
            startMinutes = 600,
            endMinutes = 660,
            category = Category.JOBS,
            repeatDays = setOf(DayOfWeek.MONDAY)
        )

        // Weekend-only task
        val weekendTask = ScheduleTask(
            id = 2L,
            title = "Weekend Workout",
            details = "Gym and recovery",
            startMinutes = 540,
            endMinutes = 600,
            category = Category.REST,
            repeatDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        )

        // Everyday task
        val everydayTask = ScheduleTask(
            id = 3L,
            title = "Daily Reading",
            details = "Study docs",
            startMinutes = 1200,
            endMinutes = 1260,
            category = Category.LEARNING,
            repeatDays = DayOfWeek.entries.toSet()
        )

        repository.addTask(mondayTask)
        repository.addTask(weekendTask)
        repository.addTask(everydayTask)

        // Monday: 2026-11-02 is Monday
        val mondayDate = LocalDate(2026, 11, 2)
        assertEquals(DayOfWeek.MONDAY, mondayDate.dayOfWeek)

        val getTodayTasks = GetTodayTasks(repository, dateFlow = flowOf(mondayDate))
        val mondayTasks = getTodayTasks().first()

        assertEquals(2, mondayTasks.size)
        assertEquals(1L, mondayTasks[0].id)
        assertEquals(3L, mondayTasks[1].id)

        // Saturday: 2026-11-07 is Saturday
        val saturdayDate = LocalDate(2026, 11, 7)
        assertEquals(DayOfWeek.SATURDAY, saturdayDate.dayOfWeek)

        val saturdayTasks = getTodayTasks(saturdayDate).first()

        assertEquals(2, saturdayTasks.size)
        assertEquals(2L, saturdayTasks[0].id)
        assertEquals(3L, saturdayTasks[1].id)

        // Tuesday: 2026-11-03 is Tuesday
        val tuesdayDate = LocalDate(2026, 11, 3)
        assertEquals(DayOfWeek.TUESDAY, tuesdayDate.dayOfWeek)

        val tuesdayTasks = getTodayTasks(tuesdayDate).first()

        assertEquals(1, tuesdayTasks.size)
        assertEquals(3L, tuesdayTasks[0].id)
    }

    @Test
    fun testActiveTasksSortedByAscendingStartTime() = runTest {
        val repository = InMemoryScheduleRepository(initialTasks = emptyList())

        val lateTask = ScheduleTask(
            id = 1L,
            title = "Evening Reflection",
            details = "",
            startMinutes = 1300,
            endMinutes = 1360,
            category = Category.REST,
            repeatDays = DayOfWeek.entries.toSet()
        )
        val earlyTask = ScheduleTask(
            id = 2L,
            title = "Morning Run",
            details = "",
            startMinutes = 420,
            endMinutes = 480,
            category = Category.REST,
            repeatDays = DayOfWeek.entries.toSet()
        )
        val midTask = ScheduleTask(
            id = 3L,
            title = "Lunch",
            details = "",
            startMinutes = 780,
            endMinutes = 840,
            category = Category.REST,
            repeatDays = DayOfWeek.entries.toSet()
        )

        repository.addTask(lateTask)
        repository.addTask(earlyTask)
        repository.addTask(midTask)

        val mondayDate = LocalDate(2026, 11, 2)
        val getTodayTasks = GetTodayTasks(repository, dateFlow = flowOf(mondayDate))
        val tasks = getTodayTasks().first()

        assertEquals(3, tasks.size)
        assertEquals(2L, tasks[0].id) // 420 min
        assertEquals(3L, tasks[1].id) // 780 min
        assertEquals(1L, tasks[2].id) // 1300 min
    }
}

