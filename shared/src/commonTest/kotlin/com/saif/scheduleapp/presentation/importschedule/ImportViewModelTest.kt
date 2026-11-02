package com.saif.scheduleapp.presentation.importschedule

import com.saif.scheduleapp.data.repository.InMemoryScheduleRepository
import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.platform.PdfReader
import com.saif.scheduleapp.domain.usecase.AddTask
import com.saif.scheduleapp.domain.usecase.ClearAllTasks
import com.saif.scheduleapp.domain.usecase.ImportTasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ImportViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = InMemoryScheduleRepository()
    private val addTask = AddTask(repository)
    private val clearAllTasks = ClearAllTasks(repository)
    private val importTasks = ImportTasks(addTask, clearAllTasks)

    private class FakePdfReader(
        override val isSupported: Boolean = true,
        var textToReturn: String = ""
    ) : PdfReader {
        override suspend fun extractText(bytes: ByteArray): String = textToReturn
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
        val pdfReader = FakePdfReader()
        val viewModel = ImportViewModel(pdfReader, importTasks, testDispatcher)
        assertEquals(ImportStage.Idle, viewModel.uiState.value.stage)
        assertFalse(viewModel.uiState.value.replaceExisting)
        assertTrue(viewModel.uiState.value.rows.isEmpty())
    }

    @Test
    fun testUnsupportedPlatformError() = runTest(testDispatcher) {
        val pdfReader = FakePdfReader(isSupported = false)
        val viewModel = ImportViewModel(pdfReader, importTasks, testDispatcher)

        viewModel.onPdfPicked(byteArrayOf(1, 2, 3))
        advanceUntilIdle()

        assertEquals(ImportStage.Error, viewModel.uiState.value.stage)
        assertEquals("PDF import is not available on web yet.", viewModel.uiState.value.message)
    }

    @Test
    fun testBlankTextReturnsOcrError() = runTest(testDispatcher) {
        val pdfReader = FakePdfReader(isSupported = true, textToReturn = "   \n  ")
        val viewModel = ImportViewModel(pdfReader, importTasks, testDispatcher)

        viewModel.onPdfPicked(byteArrayOf(1, 2, 3))
        advanceUntilIdle()

        assertEquals(ImportStage.Error, viewModel.uiState.value.stage)
        assertEquals(
            "No text found. Scanned PDFs need OCR, which is not supported yet.",
            viewModel.uiState.value.message
        )
    }

    @Test
    fun testNoTimeRangesFoundError() = runTest(testDispatcher) {
        val pdfReader = FakePdfReader(
            isSupported = true,
            textToReturn = "Welcome to the Schedule\nThis is just an introductory document."
        )
        val viewModel = ImportViewModel(pdfReader, importTasks, testDispatcher)

        viewModel.onPdfPicked(byteArrayOf(1, 2, 3))
        advanceUntilIdle()

        assertEquals(ImportStage.Error, viewModel.uiState.value.stage)
        assertEquals(
            "No time ranges found. Expected lines like 6:15 - 8:15 DSA practice.",
            viewModel.uiState.value.message
        )
    }

    @Test
    fun testValidScheduleParsingAndEditing() = runTest(testDispatcher) {
        val scheduleText = """
            Daily Routine
            6:15 - 8:15 AM DSA practice
            8:15 - 9:00 AM Breakfast
            11:00 PM - 5:00 AM Sleep
        """.trimIndent()

        val pdfReader = FakePdfReader(isSupported = true, textToReturn = scheduleText)
        val viewModel = ImportViewModel(pdfReader, importTasks, testDispatcher)

        viewModel.onPdfPicked(byteArrayOf(1, 2, 3))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(ImportStage.Preview, state.stage)
        assertEquals(3, state.rows.size)

        // Check rows
        assertEquals("DSA practice", state.rows[0].title)
        assertTrue(state.rows[0].selected)
        assertFalse(state.rows[0].crossesMidnight)

        assertEquals("Sleep", state.rows[2].title)
        assertTrue(state.rows[2].crossesMidnight)

        // Toggle row 0
        viewModel.onRowToggle(state.rows[0].id)
        assertFalse(viewModel.uiState.value.rows[0].selected)

        // Edit row 1
        viewModel.onRowEdit(state.rows[1].id, "Healthy Breakfast", 500, 540, Category.REST)
        val editedRow = viewModel.uiState.value.rows[1]
        assertEquals("Healthy Breakfast", editedRow.title)
        assertEquals(500, editedRow.startMinutes)
        assertEquals(540, editedRow.endMinutes)
        assertEquals(Category.REST, editedRow.category)

        // Toggle replace existing
        viewModel.onReplaceToggle(true)
        assertTrue(viewModel.uiState.value.replaceExisting)

        // Save selected (rows 1 and 2 are selected, row 0 was unchecked)
        viewModel.onSaveClick()
        advanceUntilIdle()

        assertEquals(ImportStage.Done, viewModel.uiState.value.stage)
        val allTasks = repository.getAllTasks()
        assertEquals(2, allTasks.size)
        assertEquals("Healthy Breakfast", allTasks[0].title)
        assertEquals("Sleep", allTasks[1].title)
        // Midnight crossing capped at 1439
        assertEquals(1439, allTasks[1].endMinutes)
    }
}

