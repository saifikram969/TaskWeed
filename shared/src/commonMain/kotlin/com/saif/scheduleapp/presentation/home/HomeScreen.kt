package com.saif.scheduleapp.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saif.scheduleapp.core.designsystem.AppAccent
import com.saif.scheduleapp.core.designsystem.AppAccentContainer
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
import com.saif.scheduleapp.core.designsystem.TaskDoneBackground
import com.saif.scheduleapp.core.designsystem.TaskDoneBorder
import com.saif.scheduleapp.core.designsystem.TaskDoneText
import com.saif.scheduleapp.core.util.TimeUtils
import com.saif.scheduleapp.domain.model.Category
import com.saif.scheduleapp.domain.model.ScheduleTask

/**
 * Stateless Home Screen composable.
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    onToggleTask: (ScheduleTask) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = AppBackground
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val contentMaxWidth = 720.dp
            val horizontalPadding = if (maxWidth > 600.dp) 32.dp else 16.dp

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = contentMaxWidth)
                    .padding(horizontal = horizontalPadding),
                contentPadding = PaddingValues(top = 28.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Dashboard Header
                item {
                    DashboardHeader(
                        dateDisplay = state.currentDate.ifEmpty { TimeUtils.currentDateDisplay() }
                    )
                }

                // 2. Progress Overview Card
                item {
                    ProgressSummaryCard(
                        doneCount = state.doneCount,
                        totalCount = state.totalCount
                    )
                }

                // 3. Current Task Section
                item {
                    CurrentTaskSection(
                        nowTask = state.nowTask,
                        onToggleTask = onToggleTask
                    )
                }

                // 4. Upcoming Tasks Section
                item {
                    UpcomingTasksHeader(count = state.upcoming.size)
                }

                if (state.upcoming.isEmpty()) {
                    item {
                        EmptyUpcomingCard(
                            isAllDone = state.totalCount > 0 && state.doneCount == state.totalCount
                        )
                    }
                } else {
                    items(
                        items = state.upcoming,
                        key = { it.id }
                    ) { task ->
                        TaskItemCard(
                            task = task,
                            onToggle = { onToggleTask(task) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    dateDisplay: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = dateDisplay,
            style = MaterialTheme.typography.labelSmall.copy(color = AppAccent),
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Daily Schedule",
            style = MaterialTheme.typography.headlineLarge,
            color = AppTextMain
        )
    }
}

@Composable
private fun ProgressSummaryCard(
    doneCount: Int,
    totalCount: Int
) {
    val progress = if (totalCount > 0) doneCount.toFloat() / totalCount.toFloat() else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppCardBackground)
            .border(1.dp, AppCardBorder, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Completion",
                        style = MaterialTheme.typography.titleMedium,
                        color = AppTextMain
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$doneCount of $totalCount tasks completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTextMuted
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppAccentContainer)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = AppAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = AppAccent,
                trackColor = AppBackground
            )
        }
    }
}

@Composable
private fun CurrentTaskSection(
    nowTask: ScheduleTask?,
    onToggleTask: (ScheduleTask) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (nowTask != null) AppAccent else AppTextMuted)
            )
            Text(
                text = "HAPPENING NOW",
                style = MaterialTheme.typography.labelSmall,
                color = if (nowTask != null) AppAccent else AppTextMuted,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (nowTask != null) {
            ActiveTaskCard(
                task = nowTask,
                onToggle = { onToggleTask(nowTask) }
            )
        } else {
            NoActiveTaskCard()
        }
    }
}

@Composable
private fun ActiveTaskCard(
    task: ScheduleTask,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(AppCardBackground)
            .border(1.5.dp, AppAccent, RoundedCornerShape(22.dp))
            .clickable(onClick = onToggle)
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryBadge(category = task.category)

                Text(
                    text = TimeUtils.formatTimeRange(task.startMinutes, task.endMinutes),
                    style = MaterialTheme.typography.labelLarge,
                    color = AppAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = task.title,
                style = MaterialTheme.typography.titleLarge,
                color = AppTextMain,
                fontWeight = FontWeight.Bold
            )

            if (task.details.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = task.details,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTextMuted
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppPrimaryContainer)
                        .clickable(onClick = onToggle)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Mark Completed",
                        style = MaterialTheme.typography.labelLarge,
                        color = AppPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun NoActiveTaskCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AppCardBackground)
            .border(1.dp, AppCardBorder, RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AppBackground),
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
                    color = AppTextMain
                )
                Text(
                    text = "Enjoy your break or check upcoming tasks below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTextMuted
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
            style = MaterialTheme.typography.headlineMedium,
            color = AppTextMain
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(AppCardBackground)
                .border(1.dp, AppCardBorder, CircleShape)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                color = AppTextMuted,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TaskItemCard(
    task: ScheduleTask,
    onToggle: () -> Unit
) {
    val cardBackground by animateColorAsState(
        targetValue = if (task.isDone) TaskDoneBackground else AppCardBackground
    )
    val cardBorder by animateColorAsState(
        targetValue = if (task.isDone) TaskDoneBorder else AppCardBorder
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardBackground)
            .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onToggle)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = AppAccent,
                    uncheckedColor = AppTextMuted,
                    checkmarkColor = AppBackground
                ),
                modifier = Modifier.padding(top = 2.dp, end = 12.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryBadge(category = task.category, isMuted = task.isDone)

                    Text(
                        text = TimeUtils.formatTimeRange(task.startMinutes, task.endMinutes),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (task.isDone) TaskDoneText else AppAccent
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (task.isDone) TaskDoneText else AppTextMain,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.details.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.details,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (task.isDone) TaskDoneText else AppTextMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryBadge(
    category: Category,
    isMuted: Boolean = false
) {
    val categoryColor = when (category) {
        Category.LEARNING -> CategoryLearning
        Category.JOBS -> CategoryJobs
        Category.PRAYER -> CategoryPrayer
        Category.REST -> CategoryRest
    }
    val effectiveColor = if (isMuted) TaskDoneText else categoryColor

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(effectiveColor.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = effectiveColor,
            fontWeight = FontWeight.Bold
        )
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
            .background(AppCardBackground)
            .border(1.dp, AppCardBorder, RoundedCornerShape(18.dp))
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (isAllDone) "🎉" else "✨",
                fontSize = 32.sp
            )
            Text(
                text = if (isAllDone) "All Tasks Completed!" else "No Upcoming Tasks",
                style = MaterialTheme.typography.titleMedium,
                color = AppTextMain,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isAllDone)
                    "Great work! You've finished everything on today's schedule."
                else
                    "Your schedule is all clear for the remainder of the day.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTextMuted
            )
        }
    }
}
