package com.saif.scheduleapp.presentation.importschedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask
import com.saif.scheduleapp.domain.parser.parseSchedule
import com.saif.scheduleapp.domain.platform.PdfReader
import com.saif.scheduleapp.domain.usecase.ImportTasks
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ImportViewModel(
    private val pdfReader: PdfReader,
    private val importTasks: ImportTasks,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    fun onPdfPicked(bytes: ByteArray) {
        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(stage = ImportStage.Parsing, message = null) }

            if (!pdfReader.isSupported) {
                _uiState.update {
                    it.copy(
                        stage = ImportStage.Error,
                        message = "PDF import is not available on web yet."
                    )
                }
                return@launch
            }

            val text = try {
                pdfReader.extractText(bytes)
            } catch (e: Exception) {
                ""
            }

            if (text.isBlank()) {
                _uiState.update {
                    it.copy(
                        stage = ImportStage.Error,
                        message = "No text found. Scanned PDFs need OCR, which is not supported yet."
                    )
                }
                return@launch
            }

            val parsedTasks = parseSchedule(text)
            if (parsedTasks.isEmpty()) {
                _uiState.update {
                    it.copy(
                        stage = ImportStage.Error,
                        message = "No time ranges found. Expected lines like 6:15 - 8:15 DSA practice."
                    )
                }
                return@launch
            }

            val rows = parsedTasks.mapIndexed { index, task ->
                ImportRow(
                    id = (index + 1).toLong(),
                    title = task.title,
                    startMinutes = task.startMinutes,
                    endMinutes = task.endMinutes,
                    category = task.category,
                    selected = true,
                    crossesMidnight = task.crossesMidnight
                )
            }

            _uiState.update {
                it.copy(
                    stage = ImportStage.Preview,
                    rows = rows,
                    message = null
                )
            }
        }
    }

    fun onRowToggle(id: Long) {
        _uiState.update { state ->
            val updatedRows = state.rows.map { row ->
                if (row.id == id) row.copy(selected = !row.selected) else row
            }
            state.copy(rows = updatedRows)
        }
    }

    fun onRowEdit(id: Long, title: String, start: Int, end: Int, category: Category) {
        _uiState.update { state ->
            val updatedRows = state.rows.map { row ->
                if (row.id == id) {
                    val crossesMidnight = end <= start
                    row.copy(
                        title = title,
                        startMinutes = start,
                        endMinutes = end,
                        category = category,
                        crossesMidnight = crossesMidnight
                    )
                } else row
            }
            state.copy(rows = updatedRows)
        }
    }

    fun onReplaceToggle(enabled: Boolean? = null) {
        _uiState.update { state ->
            state.copy(replaceExisting = enabled ?: !state.replaceExisting)
        }
    }

    fun onSaveClick() {
        val state = _uiState.value
        val selectedRows = state.rows.filter { it.selected }
        if (selectedRows.isEmpty()) return

        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(stage = ImportStage.Saving) }

            val tasksToSave = selectedRows.map { row ->
                val endMin = if (row.crossesMidnight || row.endMinutes <= row.startMinutes) 1439 else row.endMinutes
                ScheduleTask(
                    id = 0L,
                    title = row.title,
                    details = "",
                    startMinutes = row.startMinutes,
                    endMinutes = endMin,
                    category = row.category,
                    isDone = false
                )
            }

            importTasks(tasksToSave, state.replaceExisting)
            _uiState.update { it.copy(stage = ImportStage.Done) }
        }
    }
}

