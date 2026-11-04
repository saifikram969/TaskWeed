package com.saif.scheduleapp.presentation.taskform

import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.platform.ReminderScheduler
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import com.saif.scheduleapp.domain.usecase.AddTask
import com.saif.scheduleapp.domain.usecase.DecideReminder
import com.saif.scheduleapp.domain.usecase.DeleteTask
import com.saif.scheduleapp.domain.usecase.GetTask
import com.saif.scheduleapp.domain.usecase.UpdateTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TaskFormViewModelTest {

    private class FakeScheduleRepository : ScheduleRepository {
        val tasks = mutableListOf<ScheduleTask>()

        override fun observeTasks(date: LocalDate): Flow<List<ScheduleTask>> = flowOf(tasks)

        override suspend fun setDone(taskId: Long, date: LocalDate, done: Boolean) {
            val index = tasks.indexOfFirst { it.id == taskId }
            if (index != -1) {
                tasks[index] = tasks[index].copy(isDone = done)
            }
        }

        override suspend fun addTask(task: ScheduleTask): Long {
            val nextId = (tasks.maxOfOrNull { it.id } ?: 0L) + 1L
            tasks.add(task.copy(id = nextId))
            return nextId
        }

        override suspend fun updateTask(task: ScheduleTask) {
            val index = tasks.indexOfFirst { it.id == task.id }
            if (index != -1) {
                tasks[index] = task
            }
        }

        override suspend fun deleteTask(taskId: Long) {
            tasks.removeAll { it.id == taskId }
        }

        override suspend fun getTask(taskId: Long): ScheduleTask? {
            return tasks.find { it.id == taskId }
        }

        override suspend fun clearAllTasks() {
            tasks.clear()
        }

        override suspend fun getAllTasks(): List<ScheduleTask> = tasks
    }

    private class FakeReminderScheduler : ReminderScheduler {
        val scheduled = mutableListOf<ScheduleTask>()
        val cancelled = mutableListOf<Long>()

        override fun schedule(task: ScheduleTask) {
            scheduled.add(task)
        }

        override fun cancel(taskId: Long) {
            cancelled.add(taskId)
        }

        override fun snooze(task: ScheduleTask, minutes: Int) {}
    }

    private fun createViewModel(
        repo: FakeScheduleRepository = FakeScheduleRepository(),
        taskId: Long? = null
    ): Pair<TaskFormViewModel, FakeScheduleRepository> {
        val scheduler = FakeReminderScheduler()
        val decideReminder = DecideReminder()
        val getTask = GetTask(repo)
        val addTask = AddTask(repo, scheduler)
        val updateTask = UpdateTask(repo, scheduler, decideReminder)
        val deleteTask = DeleteTask(repo, scheduler, decideReminder)

        val vm = TaskFormViewModel(
            getTask = getTask,
            addTask = addTask,
            updateTask = updateTask,
            deleteTask = deleteTask,
            taskId = taskId,
            dispatcher = Dispatchers.Unconfined
        )
        return vm to repo
    }

    @Test
    fun testInitialAddModeStateMatchesSpec() {
        val (viewModel, _) = createViewModel()
        val state = viewModel.uiState.value

        assertNull(state.taskId)
        assertFalse(state.isEditMode)
        assertEquals("", state.title)
        assertEquals("", state.details)
        assertEquals(540, state.startMinutes)
        assertEquals(600, state.endMinutes)
        assertEquals(Category.LEARNING, state.category)
        assertEquals(DayOfWeek.entries.toSet(), state.repeatDays)
        assertTrue(state.voiceEnabled)
        assertNull(state.error)
        assertFalse(state.saved)
        assertFalse(state.deleted)
    }

    @Test
    fun testFieldChangesUpdateState() {
        val (viewModel, _) = createViewModel()

        viewModel.onTitleChange("Morning Study")
        assertEquals("Morning Study", viewModel.uiState.value.title)

        viewModel.onDetailsChange("Chapter 1")
        assertEquals("Chapter 1", viewModel.uiState.value.details)

        viewModel.onStartChange(600)
        assertEquals(600, viewModel.uiState.value.startMinutes)

        viewModel.onEndChange(720)
        assertEquals(720, viewModel.uiState.value.endMinutes)

        viewModel.onCategoryChange(Category.PRAYER)
        assertEquals(Category.PRAYER, viewModel.uiState.value.category)
    }

    @Test
    fun testRepeatDaysToggleAndShortcuts() {
        val (viewModel, _) = createViewModel()

        // Toggle Monday off
        viewModel.onDayToggle(DayOfWeek.MONDAY)
        assertFalse(DayOfWeek.MONDAY in viewModel.uiState.value.repeatDays)

        // Toggle Monday back on
        viewModel.onDayToggle(DayOfWeek.MONDAY)
        assertTrue(DayOfWeek.MONDAY in viewModel.uiState.value.repeatDays)

        // Weekdays shortcut
        val weekdays = setOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        )
        viewModel.onSetRepeatDays(weekdays)
        assertEquals(weekdays, viewModel.uiState.value.repeatDays)

        // Every day shortcut
        viewModel.onSetRepeatDays(DayOfWeek.entries.toSet())
        assertEquals(DayOfWeek.entries.toSet(), viewModel.uiState.value.repeatDays)
    }

    @Test
    fun testVoiceEnabledToggle() {
        val (viewModel, _) = createViewModel()
        assertTrue(viewModel.uiState.value.voiceEnabled)

        viewModel.onVoiceEnabledChange(false)
        assertFalse(viewModel.uiState.value.voiceEnabled)

        viewModel.onVoiceEnabledChange(true)
        assertTrue(viewModel.uiState.value.voiceEnabled)
    }

    @Test
    fun testLoadExistingTaskInEditMode() {
        val repo = FakeScheduleRepository()
        repo.tasks.add(
            ScheduleTask(
                id = 42L,
                title = "Existing Task",
                details = "Existing Details",
                startMinutes = 660,
                endMinutes = 720,
                category = Category.JOBS,
                repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                voiceEnabled = false
            )
        )

        val (viewModel, _) = createViewModel(repo = repo, taskId = 42L)
        val state = viewModel.uiState.value

        assertEquals(42L, state.taskId)
        assertTrue(state.isEditMode)
        assertEquals("Existing Task", state.title)
        assertEquals("Existing Details", state.details)
        assertEquals(660, state.startMinutes)
        assertEquals(720, state.endMinutes)
        assertEquals(Category.JOBS, state.category)
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), state.repeatDays)
        assertFalse(state.voiceEnabled)
    }

    @Test
    fun testSaveInAddModeAddsTask() {
        val (viewModel, repo) = createViewModel()
        viewModel.onTitleChange("New Task")
        viewModel.onDetailsChange("Task details")
        viewModel.onStartChange(540)
        viewModel.onEndChange(660)
        viewModel.onCategoryChange(Category.JOBS)
        viewModel.onSaveClick()

        val state = viewModel.uiState.value
        assertTrue(state.saved)
        assertNull(state.error)
        assertEquals(1, repo.tasks.size)
        assertEquals("New Task", repo.tasks[0].title)
    }

    @Test
    fun testSaveInEditModeUpdatesTask() {
        val repo = FakeScheduleRepository()
        repo.tasks.add(
            ScheduleTask(
                id = 15L,
                title = "Original Title",
                details = "Original Details",
                startMinutes = 540,
                endMinutes = 600,
                category = Category.LEARNING,
                repeatDays = DayOfWeek.entries.toSet(),
                voiceEnabled = true
            )
        )

        val (viewModel, _) = createViewModel(repo = repo, taskId = 15L)
        viewModel.onTitleChange("Updated Title")
        viewModel.onStartChange(600)
        viewModel.onEndChange(700)
        viewModel.onSaveClick()

        val state = viewModel.uiState.value
        assertTrue(state.saved)
        assertNull(state.error)
        assertEquals(1, repo.tasks.size)
        assertEquals("Updated Title", repo.tasks[0].title)
        assertEquals(600, repo.tasks[0].startMinutes)
        assertEquals(700, repo.tasks[0].endMinutes)
    }

    @Test
    fun testDeleteInEditModeRemovesTask() {
        val repo = FakeScheduleRepository()
        repo.tasks.add(
            ScheduleTask(
                id = 77L,
                title = "To Delete",
                details = "",
                startMinutes = 540,
                endMinutes = 600,
                category = Category.REST
            )
        )

        val (viewModel, _) = createViewModel(repo = repo, taskId = 77L)
        viewModel.onDeleteClick()

        val state = viewModel.uiState.value
        assertTrue(state.deleted)
        assertEquals(0, repo.tasks.size)
    }

    @Test
    fun testSaveBlankTitleSetsError() {
        val (viewModel, _) = createViewModel()
        viewModel.onTitleChange("   ")
        viewModel.onSaveClick()

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertFalse(state.saved)
    }

    @Test
    fun testSaveInvalidTimesSetsError() {
        val (viewModel, _) = createViewModel()
        viewModel.onTitleChange("Valid Title")
        viewModel.onStartChange(700)
        viewModel.onEndChange(600) // End before start
        viewModel.onSaveClick()

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertFalse(state.saved)
    }

    @Test
    fun testSaveEmptyRepeatDaysSetsError() {
        val (viewModel, _) = createViewModel()
        viewModel.onTitleChange("Valid Title")
        viewModel.onSetRepeatDays(emptySet())
        viewModel.onSaveClick()

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertEquals("Pick at least one day.", state.error)
        assertFalse(state.saved)
    }
}

