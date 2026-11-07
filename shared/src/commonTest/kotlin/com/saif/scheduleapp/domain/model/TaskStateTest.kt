package com.saif.scheduleapp.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class TaskStateTest {

    private val startMinutes = 540 // 09:00 AM
    private val endMinutes = 600   // 10:00 AM

    // 1. UPCOMING tests
    @Test
    fun testUpcomingWellBeforeStartTime() {
        val state = classifyTask(
            startMinutes = startMinutes,
            endMinutes = endMinutes,
            isDone = false,
            nowMinutes = 480 // 08:00 AM
        )
        assertEquals(TaskState.UPCOMING, state)
    }

    @Test
    fun testUpcomingImmediatelyBeforeStartTime() {
        val state = classifyTask(
            startMinutes = startMinutes,
            endMinutes = endMinutes,
            isDone = false,
            nowMinutes = 539 // 08:59 AM
        )
        assertEquals(TaskState.UPCOMING, state)
    }

    // 2. NOW tests (boundary: starts exactly at current time)
    @Test
    fun testNowExactlyAtStartTime() {
        val state = classifyTask(
            startMinutes = startMinutes,
            endMinutes = endMinutes,
            isDone = false,
            nowMinutes = 540 // 09:00 AM (boundary condition)
        )
        assertEquals(TaskState.NOW, state)
    }

    @Test
    fun testNowMidwayDuringTask() {
        val state = classifyTask(
            startMinutes = startMinutes,
            endMinutes = endMinutes,
            isDone = false,
            nowMinutes = 570 // 09:30 AM
        )
        assertEquals(TaskState.NOW, state)
    }

    @Test
    fun testNowImmediatelyBeforeEndTime() {
        val state = classifyTask(
            startMinutes = startMinutes,
            endMinutes = endMinutes,
            isDone = false,
            nowMinutes = 599 // 09:59 AM (boundary condition)
        )
        assertEquals(TaskState.NOW, state)
    }

    // 3. MISSED tests (boundary: ends exactly at current time)
    @Test
    fun testMissedExactlyAtEndTime() {
        val state = classifyTask(
            startMinutes = startMinutes,
            endMinutes = endMinutes,
            isDone = false,
            nowMinutes = 600 // 10:00 AM (boundary condition)
        )
        assertEquals(TaskState.MISSED, state)
    }

    @Test
    fun testMissedImmediatelyAfterEndTime() {
        val state = classifyTask(
            startMinutes = startMinutes,
            endMinutes = endMinutes,
            isDone = false,
            nowMinutes = 601 // 10:01 AM
        )
        assertEquals(TaskState.MISSED, state)
    }

    @Test
    fun testMissedWellAfterEndTime() {
        val state = classifyTask(
            startMinutes = startMinutes,
            endMinutes = endMinutes,
            isDone = false,
            nowMinutes = 720 // 12:00 PM
        )
        assertEquals(TaskState.MISSED, state)
    }

    // 4. DONE tests (isDone = true takes priority over all time states)
    @Test
    fun testDoneBeforeStartTime() {
        val state = classifyTask(
            startMinutes = startMinutes,
            endMinutes = endMinutes,
            isDone = true,
            nowMinutes = 480
        )
        assertEquals(TaskState.DONE, state)
    }

    @Test
    fun testDoneAtStartTime() {
        val state = classifyTask(
            startMinutes = startMinutes,
            endMinutes = endMinutes,
            isDone = true,
            nowMinutes = 540
        )
        assertEquals(TaskState.DONE, state)
    }

    @Test
    fun testDoneDuringTask() {
        val state = classifyTask(
            startMinutes = startMinutes,
            endMinutes = endMinutes,
            isDone = true,
            nowMinutes = 570
        )
        assertEquals(TaskState.DONE, state)
    }

    @Test
    fun testDoneAtEndTime() {
        val state = classifyTask(
            startMinutes = startMinutes,
            endMinutes = endMinutes,
            isDone = true,
            nowMinutes = 600
        )
        assertEquals(TaskState.DONE, state)
    }

    @Test
    fun testDoneAfterEndTime() {
        val state = classifyTask(
            startMinutes = startMinutes,
            endMinutes = endMinutes,
            isDone = true,
            nowMinutes = 720
        )
        assertEquals(TaskState.DONE, state)
    }

    // 5. ScheduleTask overload test
    @Test
    fun testScheduleTaskOverloadClassification() {
        val task = ScheduleTask(
            id = 1L,
            title = "Test Task",
            details = "Details",
            startMinutes = 600,
            endMinutes = 660,
            category = Category.LEARNING,
            isDone = false
        )

        assertEquals(TaskState.UPCOMING, classifyTask(task, 599))
        assertEquals(TaskState.NOW, classifyTask(task, 600))
        assertEquals(TaskState.NOW, classifyTask(task, 630))
        assertEquals(TaskState.NOW, classifyTask(task, 659))
        assertEquals(TaskState.MISSED, classifyTask(task, 660))
        assertEquals(TaskState.MISSED, classifyTask(task, 700))

        val doneTask = task.copy(isDone = true)
        assertEquals(TaskState.DONE, classifyTask(doneTask, 500))
        assertEquals(TaskState.DONE, classifyTask(doneTask, 630))
        assertEquals(TaskState.DONE, classifyTask(doneTask, 700))
    }
}

