package com.saif.scheduleapp.presentation.importschedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saif.scheduleapp.core.designsystem.AppAccent
import com.saif.scheduleapp.core.designsystem.AppBackground
import com.saif.scheduleapp.core.designsystem.AppCardBackground
import com.saif.scheduleapp.core.designsystem.AppCardBorder
import com.saif.scheduleapp.core.designsystem.AppPrimary
import com.saif.scheduleapp.core.designsystem.AppPrimaryContainer
import com.saif.scheduleapp.core.designsystem.AppTextMain
import com.saif.scheduleapp.core.designsystem.AppTextMuted
import com.saif.scheduleapp.core.designsystem.CategoryJobs
import com.saif.scheduleapp.core.designsystem.CategoryLearning
import com.saif.scheduleapp.core.designsystem.CategoryPrayer
import com.saif.scheduleapp.core.designsystem.CategoryRest
import com.saif.scheduleapp.core.util.formatTime
import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.presentation.taskform.TimePickerModalDialog
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch

@Composable
fun ImportScreen(
    state: ImportUiState,
    onPdfPicked: (ByteArray) -> Unit,
    onRowToggle: (Long) -> Unit,
    onRowEdit: (id: Long, title: String, start: Int, end: Int, category: Category) -> Unit,
    onReplaceToggle: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val filePickerLauncher = rememberFilePickerLauncher(
        type = FileKitType.File("pdf"),
        mode = FileKitMode.Single
    ) { platformFile ->
        if (platformFile != null) {
            coroutineScope.launch {
                val bytes = platformFile.readBytes()
                onPdfPicked(bytes)
            }
        }
    }

    var editingRowIdForTime by remember { mutableStateOf<Long?>(null) }
    var isEditingStartTime by remember { mutableStateOf(true) }

    val activeEditingRow = state.rows.find { it.id == editingRowIdForTime }
    if (activeEditingRow != null) {
        val initialMinutes = if (isEditingStartTime) activeEditingRow.startMinutes else activeEditingRow.endMinutes
        val dialogTitle = if (isEditingStartTime) "Edit start time" else "Edit end time"

        TimePickerModalDialog(
            title = dialogTitle,
            initialMinutes = initialMinutes,
            onConfirm = { newMinutes ->
                val newStart = if (isEditingStartTime) newMinutes else activeEditingRow.startMinutes
                val newEnd = if (!isEditingStartTime) newMinutes else activeEditingRow.endMinutes
                onRowEdit(activeEditingRow.id, activeEditingRow.title, newStart, newEnd, activeEditingRow.category)
                editingRowIdForTime = null
            },
            onDismiss = { editingRowIdForTime = null }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppBackground,
        bottomBar = {
            if (state.stage == ImportStage.Preview || state.stage == ImportStage.Done) {
                Surface(
                    color = AppBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val selectedCount = state.rows.count { it.selected }
                        Button(
                            onClick = onSaveClick,
                            enabled = selectedCount > 0 && state.stage != ImportStage.Saving,
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 720.dp)
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppPrimary,
                                contentColor = Color.White,
                                disabledContainerColor = AppCardBackground,
                                disabledContentColor = AppTextMuted
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Save selected ($selectedCount)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            val contentMaxWidth = 720.dp
            val horizontalPadding = if (maxWidth > 600.dp) 32.dp else 16.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = contentMaxWidth)
                    .padding(horizontal = horizontalPadding, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Top Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AppCardBackground)
                            .border(1.dp, AppCardBorder, CircleShape)
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "←",
                            color = AppTextMain,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Import Schedule",
                        color = AppTextMain,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 2. Stage-dependent Content
                when (state.stage) {
                    ImportStage.Idle -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "📄",
                                    fontSize = 48.sp
                                )
                                Text(
                                    text = "Import tasks from PDF",
                                    color = AppTextMain,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Select a PDF schedule file to preview and import tasks.",
                                    color = AppTextMuted,
                                    fontSize = 14.sp
                                )
                                Button(
                                    onClick = { filePickerLauncher.launch() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppPrimary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Select PDF File")
                                }
                            }
                        }
                    }

                    ImportStage.Parsing -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(color = AppPrimary)
                                Text(
                                    text = "Reading & parsing PDF...",
                                    color = AppTextMuted,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    ImportStage.Saving -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(color = AppPrimary)
                                Text(
                                    text = "Saving selected tasks...",
                                    color = AppTextMuted,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    ImportStage.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(AppCardBackground)
                                    .border(1.dp, AppCardBorder, RoundedCornerShape(16.dp))
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "⚠️",
                                    fontSize = 36.sp
                                )
                                Text(
                                    text = state.message ?: "An error occurred during import.",
                                    color = AppTextMain,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Button(
                                    onClick = { filePickerLauncher.launch() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppPrimary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Choose Another PDF")
                                }
                            }
                        }
                    }

                    ImportStage.Preview, ImportStage.Done -> {
                        // Options: Replace existing switch & file re-pick
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(AppCardBackground)
                                .border(1.dp, AppCardBorder, RoundedCornerShape(14.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Replace existing tasks",
                                    color = AppTextMain,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (state.replaceExisting) "Existing schedule will be cleared" else "New tasks will be appended",
                                    color = AppTextMuted,
                                    fontSize = 12.sp
                                )
                            }
                            Switch(
                                checked = state.replaceExisting,
                                onCheckedChange = onReplaceToggle,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AppPrimary,
                                    uncheckedThumbColor = AppTextMuted,
                                    uncheckedTrackColor = AppBackground
                                )
                            )
                        }

                        // Tasks list
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            items(items = state.rows, key = { it.id }) { row ->
                                ImportRowCard(
                                    row = row,
                                    onToggle = { onRowToggle(row.id) },
                                    onTitleEdit = { newTitle ->
                                        onRowEdit(row.id, newTitle, row.startMinutes, row.endMinutes, row.category)
                                    },
                                    onCategoryEdit = { newCat ->
                                        onRowEdit(row.id, row.title, row.startMinutes, row.endMinutes, newCat)
                                    },
                                    onStartTimeClick = {
                                        editingRowIdForTime = row.id
                                        isEditingStartTime = true
                                    },
                                    onEndTimeClick = {
                                        editingRowIdForTime = row.id
                                        isEditingStartTime = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportRowCard(
    row: ImportRow,
    onToggle: () -> Unit,
    onTitleEdit: (String) -> Unit,
    onCategoryEdit: (Category) -> Unit,
    onStartTimeClick: () -> Unit,
    onEndTimeClick: () -> Unit
) {
    val isCrossingMidnight = row.crossesMidnight || row.endMinutes <= row.startMinutes

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppCardBackground)
            .border(
                width = 1.dp,
                color = if (row.selected) AppPrimary.copy(alpha = 0.5f) else AppCardBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: Checkbox + Editable Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = row.selected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = AppPrimary,
                    uncheckedColor = AppTextMuted,
                    checkmarkColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            OutlinedTextField(
                value = row.title,
                onValueChange = onTitleEdit,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppPrimary,
                    unfocusedBorderColor = AppCardBorder,
                    focusedTextColor = AppTextMain,
                    unfocusedTextColor = AppTextMain,
                    cursorColor = AppPrimary
                )
            )
        }

        // Row 2: Time chips & Midnight notice
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Start time chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppPrimaryContainer)
                    .clickable(onClick = onStartTimeClick)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Start: ${formatTime(row.startMinutes)}",
                    color = AppPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // End time chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppPrimaryContainer)
                    .clickable(onClick = onEndTimeClick)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "End: ${formatTime(row.endMinutes)}",
                    color = AppPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (isCrossingMidnight) {
                Text(
                    text = "ends at 11:59 PM",
                    color = AppAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Row 3: Category chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(Category.entries.toTypedArray()) { cat ->
                val isSelected = (row.category == cat)
                val catColor = when (cat) {
                    Category.PRAYER -> CategoryPrayer
                    Category.REST -> CategoryRest
                    Category.JOBS -> CategoryJobs
                    Category.LEARNING -> CategoryLearning
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) catColor.copy(alpha = 0.25f) else AppBackground)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) catColor else AppCardBorder,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onCategoryEdit(cat) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = cat.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = if (isSelected) catColor else AppTextMuted,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

