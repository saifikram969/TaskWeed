package com.saif.scheduleapp.presentation.addtask

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    state: AddTaskUiState,
    onTitleChange: (String) -> Unit,
    onDetailsChange: (String) -> Unit,
    onStartChange: (Int) -> Unit,
    onEndChange: (Int) -> Unit,
    onCategoryChange: (Category) -> Unit,
    onSaveClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

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
                        text = "Add task",
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

                // Error Message Display
                if (state.error != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF2D1619))
                            .border(1.dp, Color(0xFFFF6B6B), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = state.error,
                            color = Color(0xFFFF6B6B),
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
                        text = "Save Task",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
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
private fun TimePickerModalDialog(
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
