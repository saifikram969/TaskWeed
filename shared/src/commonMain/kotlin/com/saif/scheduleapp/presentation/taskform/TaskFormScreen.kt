package com.saif.scheduleapp.presentation.taskform

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saif.scheduleapp.core.designsystem.AppBackground
import com.saif.scheduleapp.core.designsystem.AppCardBackground
import com.saif.scheduleapp.core.designsystem.AppCardBorder
import com.saif.scheduleapp.core.designsystem.AppDanger
import com.saif.scheduleapp.core.designsystem.AppDangerContainer
import com.saif.scheduleapp.core.designsystem.categoryColor
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
import kotlinx.datetime.DayOfWeek

private val WEEKDAYS = setOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormScreen(
    state: TaskFormUiState,
    onTitleChange: (String) -> Unit,
    onDetailsChange: (String) -> Unit,
    onStartChange: (Int) -> Unit,
    onEndChange: (Int) -> Unit,
    onCategoryChange: (Category) -> Unit,
    onDayToggle: (DayOfWeek) -> Unit,
    onSetRepeatDays: (Set<DayOfWeek>) -> Unit,
    onVoiceEnabledChange: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = AppBackground
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val contentMaxWidth = 720.dp
            val horizontalPadding = if (maxWidth > 600.dp) 32.dp else 20.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = contentMaxWidth)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Top App Bar / Header
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
                        text = if (state.isEditMode) "Edit task" else "Add task",
                        color = AppTextMain,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Title Input
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Task Title",
                        color = AppTextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedTextField(
                        value = state.title,
                        onValueChange = onTitleChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "e.g. Study Mathematics",
                                color = AppTextMuted.copy(alpha = 0.6f)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppPrimary,
                            unfocusedBorderColor = AppCardBorder,
                            focusedTextColor = AppTextMain,
                            unfocusedTextColor = AppTextMain,
                            cursorColor = AppPrimary,
                            focusedContainerColor = AppCardBackground,
                            unfocusedContainerColor = AppCardBackground
                        )
                    )
                }

                // Details Input
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Details (optional)",
                        color = AppTextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedTextField(
                        value = state.details,
                        onValueChange = onDetailsChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "e.g. Review chapter 4 problems 1 to 10",
                                color = AppTextMuted.copy(alpha = 0.6f)
                            )
                        },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppPrimary,
                            unfocusedBorderColor = AppCardBorder,
                            focusedTextColor = AppTextMain,
                            unfocusedTextColor = AppTextMain,
                            cursorColor = AppPrimary,
                            focusedContainerColor = AppCardBackground,
                            unfocusedContainerColor = AppCardBackground
                        )
                    )
                }

                // Time Pickers (Start & End Time)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Schedule Time",
                        color = AppTextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Start Time Selector
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(AppCardBackground)
                                .border(1.dp, AppCardBorder, RoundedCornerShape(16.dp))
                                .clickable { showStartTimePicker = true }
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Start Time",
                                    color = AppTextMuted,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = formatTime(state.startMinutes),
                                    color = AppTextMain,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // End Time Selector
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(AppCardBackground)
                                .border(1.dp, AppCardBorder, RoundedCornerShape(16.dp))
                                .clickable { showEndTimePicker = true }
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "End Time",
                                    color = AppTextMuted,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = formatTime(state.endMinutes),
                                    color = AppTextMain,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Repeat Days Section (Monday first + Shortcuts)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Repeat Days",
                            color = AppTextMuted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // Shortcuts: Every day & Weekdays
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppCardBackground)
                                    .border(1.dp, AppCardBorder, RoundedCornerShape(8.dp))
                                    .clickable { onSetRepeatDays(DayOfWeek.entries.toSet()) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Every day",
                                    color = AppPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppCardBackground)
                                    .border(1.dp, AppCardBorder, RoundedCornerShape(8.dp))
                                    .clickable { onSetRepeatDays(WEEKDAYS) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Weekdays",
                                    color = AppPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Seven weekday chips, Monday first
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DayOfWeek.entries.forEach { day ->
                            val isSelected = day in state.repeatDays
                            val shortName = day.name.take(3).lowercase().replaceFirstChar { it.uppercase() }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) AppPrimaryContainer else AppCardBackground
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) AppPrimary else AppCardBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onDayToggle(day) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = shortName,
                                    color = if (isSelected) AppPrimary else AppTextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Category Chips
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Category",
                        color = AppTextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Category.entries.forEach { category ->
                            val isSelected = state.category == category
                            val categoryColor = when (category) {
                                Category.LEARNING -> CategoryLearning
                                Category.JOBS -> CategoryJobs
                                Category.PRAYER -> CategoryPrayer
                                Category.REST -> CategoryRest
                            }
                            val displayName = category.name.lowercase().replaceFirstChar { it.uppercase() }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) categoryColor.copy(alpha = 0.2f) else AppCardBackground
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) categoryColor else AppCardBorder,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { onCategoryChange(category) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = displayName,
                                    color = if (isSelected) categoryColor else AppTextMuted,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Speak this reminder switch
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppCardBackground)
                        .border(1.dp, AppCardBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Speak this reminder",
                                color = AppTextMain,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Voice readout when reminder triggers",
                                color = AppTextMuted,
                                fontSize = 12.sp
                            )
                        }

                        Switch(
                            checked = state.voiceEnabled,
                            onCheckedChange = onVoiceEnabledChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AppPrimary,
                                uncheckedThumbColor = AppTextMuted,
                                uncheckedTrackColor = AppCardBackground,
                                uncheckedBorderColor = AppCardBorder
                            )
                        )
                    }
                }

                // Error Message Display
                if (state.error != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AppDangerContainer)
                            .border(1.dp, AppDanger, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = state.error,
                            color = AppDanger,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Save Button
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (state.isEditMode) "Save Changes" else "Save Task",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Delete Button (Shown only in edit mode)
                if (state.isEditMode) {
                    Button(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppDangerContainer,
                            contentColor = AppDanger
                        )
                    ) {
                        Text(
                            text = "Delete Task",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppDanger
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Delete Task",
                    color = AppTextMain,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this task? This action cannot be undone.",
                    color = AppTextMuted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteClick()
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = AppDanger,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(
                        text = "Cancel",
                        color = AppTextMuted
                    )
                }
            },
            containerColor = AppCardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Start TimePickerDialog
    if (showStartTimePicker) {
        TimePickerModalDialog(
            title = "Select Start Time",
            initialMinutes = state.startMinutes,
            onConfirm = { minutes ->
                onStartChange(minutes)
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false }
        )
    }

    // End TimePickerDialog
    if (showEndTimePicker) {
        TimePickerModalDialog(
            title = "Select End Time",
            initialMinutes = state.endMinutes,
            onConfirm = { minutes ->
                onEndChange(minutes)
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerModalDialog(
    title: String,
    initialMinutes: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val initialHour = (initialMinutes / 60) % 24
    val initialMinute = initialMinutes % 60
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                color = AppTextMain,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = AppCardBackground,
                        selectorColor = AppPrimary,
                        containerColor = AppCardBackground,
                        periodSelectorBorderColor = AppCardBorder,
                        periodSelectorSelectedContainerColor = AppPrimaryContainer,
                        periodSelectorUnselectedContainerColor = AppCardBackground,
                        periodSelectorSelectedContentColor = AppPrimary,
                        periodSelectorUnselectedContentColor = AppTextMuted,
                        timeSelectorSelectedContainerColor = AppPrimaryContainer,
                        timeSelectorUnselectedContainerColor = AppCardBackground,
                        timeSelectorSelectedContentColor = AppPrimary,
                        timeSelectorUnselectedContentColor = AppTextMain
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val minutes = timePickerState.hour * 60 + timePickerState.minute
                    onConfirm(minutes)
                }
            ) {
                Text(
                    text = "OK",
                    color = AppPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = AppTextMuted
                )
            }
        },
        containerColor = AppCardBackground,
        shape = RoundedCornerShape(16.dp)
    )
}

