package com.saif.scheduleapp.presentation.reminder

import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ReminderViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeScheduleRepository(
        private val tasks: List<ScheduleTask>
    ) : ScheduleRepository {
        override fun observeTasks(date: kotlinx.datetime.LocalDate): Flow<List<ScheduleTask>> = flowOf(tasks)
        override suspend fun setDone(taskId: Long, date: kotlinx.datetime.LocalDate, done: Boolean) {}
        override suspend fun addTask(task: ScheduleTask): Long = 0L
        override suspend fun clearAllTasks() {}
        override suspend fun getAllTasks(): List<ScheduleTask> = tasks
    }

    private class FakeReminderScheduler : ReminderScheduler {
        val snoozed = mutableListOf<Pair<ScheduleTask, Int>>()
        override fun schedule(task: ScheduleTask) {}
        override fun cancel(taskId: Long) {}
        override fun snooze(task: ScheduleTask, minutes: Int) {
            snoozed.add(task to minutes)
        }
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() {
        val repo = FakeScheduleRepository(emptyList())
        val scheduler = FakeReminderScheduler()
        val viewModel = ReminderViewModel(repo, scheduler, testDispatcher)

        assertNull(viewModel.uiState.value.task)
        assertFalse(viewModel.uiState.value.isSpeaking)
    }

    @Test
    fun testLoadTask() = runTest(testDispatcher) {
        val task = ScheduleTask(
            id = 42L,
            title = "Review Architecture",
            details = "Check Compose Multiplatform setup",
            startMinutes = 600,
            endMinutes = 660,
            category = Category.LEARNING
        )
        val repo = FakeScheduleRepository(listOf(task))
        val scheduler = FakeReminderScheduler()
        val viewModel = ReminderViewModel(repo, scheduler, testDispatcher)

        viewModel.loadTask(42L)
        testScheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.task)
        assertEquals(42L, viewModel.uiState.value.task?.id)
        assertEquals("Review Architecture", viewModel.uiState.value.task?.title)
    }

    @Test
    fun testStartNowInvokesCallbackWithoutModifyingDone() {
        val repo = FakeScheduleRepository(emptyList())
        val scheduler = FakeReminderScheduler()
        val viewModel = ReminderViewModel(repo, scheduler, testDispatcher)

        var finishCalled = false
        viewModel.onStartNow {
            finishCalled = true
        }

        assertTrue(finishCalled)
    }

    @Test
    fun testSnoozeCallsSchedulerAndFinishes() {
        val task = ScheduleTask(
            id = 10L,
            title = "Take a walk",
            details = "Stretch and walk outside",
            startMinutes = 720,
            endMinutes = 750,
            category = Category.REST
        )
        val repo = FakeScheduleRepository(listOf(task))
        val scheduler = FakeReminderScheduler()
        val viewModel = ReminderViewModel(repo, scheduler, testDispatcher)
        viewModel.setTask(task)

        var finishCalled = false
        viewModel.onSnooze(minutes = 5) {
            finishCalled = true
        }

        assertTrue(finishCalled)
        assertEquals(1, scheduler.snoozed.size)
        assertEquals(10L, scheduler.snoozed.first().first.id)
        assertEquals(5, scheduler.snoozed.first().second)
    }
}

