package com.saif.scheduleapp.presentation.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saif.scheduleapp.core.designsystem.CategoryTile
import com.saif.scheduleapp.core.designsystem.ScheduleTheme
import com.saif.scheduleapp.core.designsystem.Space12
import com.saif.scheduleapp.core.designsystem.Space16
import com.saif.scheduleapp.core.designsystem.Space24
import com.saif.scheduleapp.core.designsystem.Space32
import com.saif.scheduleapp.core.designsystem.Space4
import com.saif.scheduleapp.core.designsystem.Space8
import com.saif.scheduleapp.core.platform.Haptics
import com.saif.scheduleapp.core.util.TimeUtils
import com.saif.scheduleapp.domain.model.ScheduleTask
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import taskweed.shared.generated.resources.Res
import taskweed.shared.generated.resources.ic_flame

/**
 * Stateless Home Screen composable.
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    onToggleTask: (ScheduleTask) -> Unit,
    onNavigateToAddTask: () -> Unit = {},
    onNavigateToEditTask: (Long) -> Unit = {},
    onNavigateToImport: () -> Unit = {},
    haptics: Haptics = koinInject(),
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = ScheduleTheme.colors.background
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val contentMaxWidth = 720.dp
            val horizontalPadding = if (maxWidth > 600.dp) Space32 else Space16

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = contentMaxWidth)
                    .padding(horizontal = horizontalPadding),
                contentPadding = PaddingValues(top = Space24, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(Space24)
            ) {
                // 1. Dashboard Header
                item {
                    Header(
                        currentDate = state.currentDate,
                        greeting = state.greeting,
                        streak = state.currentStreak
                    )
                }

                // Battery Optimization Notice
                item {
                    BatteryOptimizationCard()
                }

                if (state.totalCount == 0) {
                    item {
                        EmptyScheduleCard()
                    }
                } else {
                    // 2. Now Card
                    item {
                        NowCard(
                            nowTask = state.nowTask,
                            progress = state.nowProgress,
                            remainingMinutes = state.nowRemainingMinutes,
                            onToggleTask = onToggleTask,
                            onNavigateToEditTask = onNavigateToEditTask,
                            haptics = haptics
                        )
                    }

                    // 3. Day Progress Section
                    item {
                        DayProgressSection(
                            doneCount = state.doneCount,
                            totalCount = state.totalCount
                        )
                    }

                    // 4. Upcoming Tasks Section
                    item {
                        UpcomingTasksHeader(count = state.upcoming.size)
                    }

                    if (state.upcoming.isEmpty() && state.nowTask == null && state.doneTasks.isNotEmpty()) {
                        item {
                            EmptyUpcomingCard(isAllDone = true)
                        }
                    } else if (state.upcoming.isEmpty()) {
                        item {
                            EmptyUpcomingCard(isAllDone = false)
                        }
                    } else {
                        items(
                            items = state.upcoming,
                            key = { it.id }
                        ) { task ->
                            UpcomingTaskCard(
                                task = task,
                                onToggle = {
                                    haptics.tick()
                                    onToggleTask(task)
                                },
                                onEdit = { onNavigateToEditTask(task.id) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }

                    // 5. Done Section
                    if (state.doneTasks.isNotEmpty()) {
                        item {
                            DoneTasksHeader(count = state.doneTasks.size)
                        }
                        items(
                            items = state.doneTasks,
                            key = { it.id }
                        ) { task ->
                            DoneTaskCard(
                                task = task,
                                onToggle = { onToggleTask(task) },
                                onEdit = { onNavigateToEditTask(task.id) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }

            // Floating Action Buttons
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Space24),
                horizontalArrangement = Arrangement.spacedBy(Space16),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.isPdfImportSupported) {
                    FloatingActionButton(
                        onClick = onNavigateToImport,
                        containerColor = ScheduleTheme.colors.surface,
                        contentColor = ScheduleTheme.colors.accent,
                        shape = CircleShape,
                        modifier = Modifier.border(1.dp, ScheduleTheme.colors.surfaceVariant, CircleShape)
                    ) {
                        Text(
                            text = "📄",
                            fontSize = 20.sp
                        )
                    }
                }

                FloatingActionButton(
                    onClick = onNavigateToAddTask,
                    containerColor = ScheduleTheme.colors.primary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Text(
                        text = "+",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(
    currentDate: String,
    greeting: String,
    streak: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = currentDate.ifEmpty { TimeUtils.currentDateDisplay() },
                style = MaterialTheme.typography.labelSmall,
                color = ScheduleTheme.colors.textMuted
            )
            Spacer(modifier = Modifier.height(Space4))
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ScheduleTheme.colors.text
            )
        }

        // Streak chip
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(ScheduleTheme.colors.surfaceVariant)
                .border(1.dp, ScheduleTheme.colors.surfaceVariant, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space4)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_flame),
                    contentDescription = "Streak",
                    tint = ScheduleTheme.colors.warning,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "$streak",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ScheduleTheme.colors.text
                )
            }
        }
    }
}

@Composable
private fun DayProgressSection(
    doneCount: Int,
    totalCount: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ScheduleTheme.colors.text
            )
            Text(
                text = "$doneCount of $totalCount done",
                style = MaterialTheme.typography.bodyMedium,
                color = ScheduleTheme.colors.textMuted
            )
        }

        Spacer(modifier = Modifier.height(Space8))

        val dayProgress = if (totalCount > 0) doneCount.toFloat() / totalCount.toFloat() else 0f
        val animatedDayProgress by animateFloatAsState(
            targetValue = dayProgress,
            label = "DayProgress"
        )

        LinearProgressIndicator(
            progress = { animatedDayProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = ScheduleTheme.colors.accent,
            trackColor = ScheduleTheme.colors.surfaceVariant
        )
    }
}

@Composable
fun ProgressRing(
    progress: Float,
    remainingMinutes: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "NowProgress"
    )
    val strokeWidth = 8.dp
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { strokeWidth.toPx() }
    val trackColor = ScheduleTheme.colors.surfaceVariant
    val progressColor = ScheduleTheme.colors.accent

    Box(
        modifier = modifier.size(84.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val arcSize = Size(size.width - strokeWidthPx, size.height - strokeWidthPx)
            val topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f)

            // Background Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // Active Progress
            if (animatedProgress > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
        }

        // Remaining time inside ring
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val timeText = if (remainingMinutes >= 60) {
                val hrs = remainingMinutes / 60
                val mins = remainingMinutes % 60
                if (mins > 0) "${hrs}h ${mins}m" else "${hrs}h"
            } else {
                "${remainingMinutes}m"
            }
            Text(
                text = timeText,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                fontWeight = FontWeight.Bold,
                color = ScheduleTheme.colors.text
            )
            Text(
                text = "left",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = ScheduleTheme.colors.textMuted
            )
        }
    }
}

@Composable
private fun NowCard(
    nowTask: ScheduleTask?,
    progress: Float,
    remainingMinutes: Int,
    onToggleTask: (ScheduleTask) -> Unit,
    onNavigateToEditTask: (Long) -> Unit,
    haptics: Haptics,
    modifier: Modifier = Modifier
) {
    if (nowTask != null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(ScheduleTheme.colors.primaryMuted)
                .padding(Space24)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space16)
            ) {
                ProgressRing(
                    progress = progress,
                    remainingMinutes = remainingMinutes
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Space8)
                ) {
                    CategoryTile(category = nowTask.category)

                    Text(
                        text = nowTask.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ScheduleTheme.colors.text,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { onNavigateToEditTask(nowTask.id) }
                    )

                    Text(
                        text = TimeUtils.formatTimeRange(nowTask.startMinutes, nowTask.endMinutes),
                        style = MaterialTheme.typography.bodySmall,
                        color = ScheduleTheme.colors.textMuted
                    )

                    Button(
                        onClick = {
                            haptics.tick()
                            onToggleTask(nowTask)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ScheduleTheme.colors.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    } else {
        NoActiveTaskCard(modifier = modifier)
    }
}

@Composable
private fun NoActiveTaskCard(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(ScheduleTheme.colors.primaryMuted)
            .padding(Space24)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space12)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ScheduleTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⏱",
                    fontSize = 18.sp
                )
            }
            Column {
                Text(
                    text = "No active task right now",
                    style = MaterialTheme.typography.titleMedium,
                    color = ScheduleTheme.colors.text,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Enjoy your break or check upcoming tasks below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ScheduleTheme.colors.textMuted
                )
            }
        }
    }
}

@Composable
private fun UpcomingTasksHeader(count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Upcoming Tasks",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ScheduleTheme.colors.text
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(ScheduleTheme.colors.surfaceVariant)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                color = ScheduleTheme.colors.textMuted,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun UpcomingTaskCard(
    task: ScheduleTask,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ScheduleTheme.colors.surface)
            .border(1.dp, ScheduleTheme.colors.surfaceVariant, RoundedCornerShape(18.dp))
            .clickable(onClick = onEdit)
            .padding(Space16)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space12)
        ) {
            Checkbox(
                checked = false,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    uncheckedColor = ScheduleTheme.colors.textMuted
                )
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Space4)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryTile(category = task.category)
                    Text(
                        text = TimeUtils.formatTimeRange(task.startMinutes, task.endMinutes),
                        style = MaterialTheme.typography.bodySmall,
                        color = ScheduleTheme.colors.accent,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ScheduleTheme.colors.text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.details.isNotBlank()) {
                    Text(
                        text = task.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = ScheduleTheme.colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun DoneTasksHeader(count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Done",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ScheduleTheme.colors.textMuted
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(ScheduleTheme.colors.surfaceVariant)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                color = ScheduleTheme.colors.textMuted,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DoneTaskCard(
    task: ScheduleTask,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .alpha(0.6f)
            .clip(RoundedCornerShape(18.dp))
            .background(ScheduleTheme.colors.taskDoneBackground)
            .border(1.dp, ScheduleTheme.colors.taskDoneBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onToggle)
            .padding(Space16)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space12)
        ) {
            Checkbox(
                checked = true,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = ScheduleTheme.colors.accent,
                    checkmarkColor = ScheduleTheme.colors.background
                )
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Space4)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryTile(category = task.category)
                    Text(
                        text = TimeUtils.formatTimeRange(task.startMinutes, task.endMinutes),
                        style = MaterialTheme.typography.bodySmall,
                        color = ScheduleTheme.colors.taskDoneText
                    )
                }

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ScheduleTheme.colors.taskDoneText,
                    textDecoration = TextDecoration.LineThrough,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.details.isNotBlank()) {
                    Text(
                        text = task.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = ScheduleTheme.colors.taskDoneText,
                        textDecoration = TextDecoration.LineThrough,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyUpcomingCard(
    isAllDone: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ScheduleTheme.colors.surface)
            .border(1.dp, ScheduleTheme.colors.surfaceVariant, RoundedCornerShape(18.dp))
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Space8)
        ) {
            Text(
                text = if (isAllDone) "🎉" else "✨",
                fontSize = 32.sp
            )
            Text(
                text = if (isAllDone) "All Tasks Completed!" else "No Upcoming Tasks",
                style = MaterialTheme.typography.titleMedium,
                color = ScheduleTheme.colors.text,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isAllDone)
                    "Great work! You've finished everything on today's schedule."
                else
                    "Your schedule is all clear for the remainder of the day.",
                style = MaterialTheme.typography.bodyMedium,
                color = ScheduleTheme.colors.textMuted
            )
        }
    }
}

@Composable
private fun EmptyScheduleCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ScheduleTheme.colors.surface)
            .border(1.dp, ScheduleTheme.colors.surfaceVariant, RoundedCornerShape(18.dp))
            .padding(Space32),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Space12)
        ) {
            Text(
                text = "📝",
                fontSize = 36.sp
            )
            Text(
                text = "No tasks yet. Tap + to add one",
                style = MaterialTheme.typography.titleMedium,
                color = ScheduleTheme.colors.text,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
