package com.saif.scheduleapp.presentation.addtask

import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.repository.ScheduleRepository
import com.saif.scheduleapp.domain.usecase.AddTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AddTaskViewModelTest {

    private class FakeScheduleRepository : ScheduleRepository {
        val tasks = mutableListOf<ScheduleTask>()

        override fun observeTasks(date: kotlinx.datetime.LocalDate): Flow<List<ScheduleTask>> = flowOf(tasks)

        override suspend fun setDone(taskId: Long, date: kotlinx.datetime.LocalDate, done: Boolean) {
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

        override suspend fun clearAllTasks() {
            tasks.clear()
        }

        override suspend fun getAllTasks(): List<ScheduleTask> = tasks
    }

    private fun createViewModel(): Pair<AddTaskViewModel, FakeScheduleRepository> {
        val repo = FakeScheduleRepository()
        val addTask = AddTask(repo)
        return AddTaskViewModel(addTask = addTask, dispatcher = kotlinx.coroutines.Dispatchers.Unconfined) to repo
    }

    @Test
    fun testInitialStateMatchesSpec() {
        val (viewModel, _) = createViewModel()
        val state = viewModel.uiState.value

        assertEquals("", state.title)
        assertEquals("", state.details)
        assertEquals(540, state.startMinutes)
        assertEquals(600, state.endMinutes)
        assertEquals(Category.LEARNING, state.category)
        assertNull(state.error)
        assertFalse(state.saved)
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
    fun testSaveValidTaskSetsSavedTrue() {
        val (viewModel, repo) = createViewModel()
        viewModel.onTitleChange("Deep Work")
        viewModel.onDetailsChange("Focus session")
        viewModel.onStartChange(540)
        viewModel.onEndChange(660)
        viewModel.onCategoryChange(Category.JOBS)
        viewModel.onSaveClick()

        val state = viewModel.uiState.value
        assertTrue(state.saved)
        assertNull(state.error)
        assertEquals(1, repo.tasks.size)
        assertEquals("Deep Work", repo.tasks[0].title)
        assertEquals(Category.JOBS, repo.tasks[0].category)
    }
}
