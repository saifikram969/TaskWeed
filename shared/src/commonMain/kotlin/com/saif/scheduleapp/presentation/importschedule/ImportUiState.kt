package com.saif.scheduleapp.presentation.importschedule

import com.saif.scheduleapp.domain.model.Category

enum class ImportStage {
    Idle,
    Parsing,
    Preview,
    Saving,
    Done,
    Error
}

data class ImportRow(
    val id: Long,
    val title: String,
    val startMinutes: Int,
    val endMinutes: Int,
    val category: Category,
    val selected: Boolean = true,
    val crossesMidnight: Boolean = false
)

data class ImportUiState(
    val stage: ImportStage = ImportStage.Idle,
    val rows: List<ImportRow> = emptyList(),
    val replaceExisting: Boolean = false,
    val message: String? = null
)

