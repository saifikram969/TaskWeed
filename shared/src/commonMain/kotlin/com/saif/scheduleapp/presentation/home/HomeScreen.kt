package com.saif.scheduleapp.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.semantics.semantics
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
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import taskweed.shared.generated.resources.Res
import taskweed.shared.generated.resources.ic_flame
import taskweed.shared.generated.resources.ic_tab_done
import taskweed.shared.generated.resources.ic_tab_missed
import taskweed.shared.generated.resources.ic_tab_upcoming

/**
 * Stateless Home Screen composable featuring 3 tabs: Upcoming, Missed, and Done.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onToggleTask: (ScheduleTask) -> Unit,
    onTabSelected: (HomeTab) -> Unit = {},
    onCompleteMissedTask: (ScheduleTask) -> Unit = { onToggleTask(it) },
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
            val listState = rememberLazyListState()

            // Scroll back to the tab bar (index = 3) on tab switch if list is scrolled below it
            LaunchedEffect(state.selectedTab) {
                if (listState.firstVisibleItemIndex > 3) {
                    listState.animateScrollToItem(3)
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = contentMaxWidth)
                    .padding(horizontal = horizontalPadding),
                contentPadding = PaddingValues(top = Space24, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(Space16)
            ) {
                // Item 0: Header & Battery Notice
                item(key = "header") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Space16)
                    ) {
                        Header(
                            currentDate = state.currentDate,
                            greeting = state.greeting,
                            streak = state.currentStreak
                        )
                        BatteryOptimizationCard()
                    }
                }

                // Item 1: Now Card
                item(key = "now_card") {
                    NowCard(
                        nowTask = state.nowTask,
                        progress = state.nowProgress,
                        remainingMinutes = state.nowRemainingMinutes,
                        onToggleTask = onToggleTask,
                        onNavigateToEditTask = onNavigateToEditTask,
                        haptics = haptics
                    )
                }

                // Item 2: Day Progress
                item(key = "day_progress") {
                    DayProgressSection(
                        doneCount = state.doneCount,
                        totalCount = state.totalCount
                    )
                }

                // Item 3: Sticky 3-Tab Bar
                stickyHeader(key = "home_tabs") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ScheduleTheme.colors.background)
                            .padding(vertical = Space8)
                    ) {
                        HomeTabBar(
                            selectedTab = state.selectedTab,
                            upcomingCount = state.upcoming.size,
                            missedCount = state.missed.size,
                            doneCount = state.doneTasks.size,
                            onTabSelected = { tab ->
                                haptics.tick()
                                onTabSelected(tab)
                            }
                        )
                    }
                }

                // Item 4+: Selected Tab's task list only
                when (state.selectedTab) {
                    HomeTab.UPCOMING -> {
                        if (state.upcoming.isEmpty()) {
                            item(key = "empty_upcoming") {
                                EmptyTabCard(
                                    emoji = "✨",
                                    title = "No more tasks today",
                                    subtitle = "Enjoy your free time or add a new task"
                                )
                            }
                        } else {
                            items(
                                items = state.upcoming,
                                key = { "upcoming_${it.id}" }
                            ) { task ->
                                UpcomingTaskRow(
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
                    }

                    HomeTab.MISSED -> {
                        if (state.missed.isEmpty()) {
                            item(key = "empty_missed") {
                                EmptyTabCard(
                                    emoji = "🎯",
                                    title = "Nothing missed",
                                    subtitle = "You're on track with today's schedule"
                                )
                            }
                        } else {
                            items(
                                items = state.missed,
                                key = { "missed_${it.id}" }
                            ) { task ->
                                MissedTaskRow(
                                    task = task,
                                    onMarkDone = {
                                        haptics.tick()
                                        onCompleteMissedTask(task)
                                    },
                                    onEdit = { onNavigateToEditTask(task.id) },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }

                    HomeTab.DONE -> {
                        if (state.doneTasks.isEmpty()) {
                            item(key = "empty_done") {
                                EmptyTabCard(
                                    emoji = "📝",
                                    title = "Nothing done yet",
                                    subtitle = "Complete tasks to see your progress here"
                                )
                            }
                        } else {
                            items(
                                items = state.doneTasks,
                                key = { "done_${it.id}" }
                            ) { task ->
                                DoneTaskRow(
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
fun HomeTabBar(
    selectedTab: HomeTab,
    upcomingCount: Int,
    missedCount: Int,
    doneCount: Int,
    onTabSelected: (HomeTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HomeTabChip(
            label = "Upcoming",
            count = upcomingCount,
            isSelected = selectedTab == HomeTab.UPCOMING,
            icon = Res.drawable.ic_tab_upcoming,
            activeColor = ScheduleTheme.colors.primary,
            showWarningBadge = false,
            onClick = { onTabSelected(HomeTab.UPCOMING) }
        )

        HomeTabChip(
            label = "Missed",
            count = missedCount,
            isSelected = selectedTab == HomeTab.MISSED,
            icon = Res.drawable.ic_tab_missed,
            activeColor = ScheduleTheme.colors.warning,
            showWarningBadge = missedCount > 0,
            onClick = { onTabSelected(HomeTab.MISSED) }
        )

        HomeTabChip(
            label = "Done",
            count = doneCount,
            isSelected = selectedTab == HomeTab.DONE,
            icon = Res.drawable.ic_tab_done,
            activeColor = ScheduleTheme.colors.primary,
            showWarningBadge = false,
            onClick = { onTabSelected(HomeTab.DONE) }
        )
    }
}

@Composable
private fun HomeTabChip(
    label: String,
    count: Int,
    isSelected: Boolean,
    icon: DrawableResource,
    activeColor: Color,
    showWarningBadge: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            activeColor.copy(alpha = 0.14f)
        } else {
            ScheduleTheme.colors.surfaceVariant.copy(alpha = 0.65f)
        },
        animationSpec = tween(durationMillis = 280),
        label = "chip_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            activeColor
        } else {
            ScheduleTheme.colors.surfaceVariant
        },
        animationSpec = tween(durationMillis = 280),
        label = "chip_border"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 1.5.dp else 1.dp,
        animationSpec = tween(durationMillis = 280),
        label = "chip_border_width"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isSelected) {
            activeColor
        } else if (showWarningBadge) {
            ScheduleTheme.colors.warning
        } else {
            Color(0xFFC0C5D6)
        },
        animationSpec = tween(durationMillis = 280),
        label = "chip_icon_tint"
    )

    val horizontalPadding by animateDpAsState(
        targetValue = if (isSelected) 14.dp else 12.5.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "chip_padding"
    )

    val endMargin by animateDpAsState(
        targetValue = if (!isSelected && count > 0) 5.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "chip_end_margin"
    )

    Box(
        modifier = modifier.padding(end = endMargin),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .height(44.dp)
                .widthIn(min = 44.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(borderWidth, borderColor, CircleShape)
                .clickable(onClick = onClick)
                .padding(horizontal = horizontalPadding),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(19.dp)
                )

                AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn(animationSpec = tween(180, delayMillis = 60)) + expandHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        expandFrom = Alignment.Start
                    ),
                    exit = fadeOut(animationSpec = tween(100)) + shrinkHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        shrinkTowards = Alignment.Start
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(activeColor)
                                .padding(horizontal = 8.dp, vertical = 2.5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = !isSelected && count > 0,
            enter = fadeIn(animationSpec = tween(180)) + scaleIn(animationSpec = tween(180)),
            exit = fadeOut(animationSpec = tween(100)) + scaleOut(animationSpec = tween(100)),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 28.dp, y = (-2).dp)
        ) {
            val badgeBg = if (showWarningBadge) {
                ScheduleTheme.colors.warning
            } else {
                Color(0xFF2C3042)
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(badgeBg)
                    .padding(horizontal = 5.dp, vertical = 1.5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false
                )
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
private fun UpcomingTaskRow(
    task: ScheduleTask,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { }
            .clip(RoundedCornerShape(18.dp))
            .background(ScheduleTheme.colors.surface)
            .border(1.dp, ScheduleTheme.colors.surfaceVariant, RoundedCornerShape(18.dp))
            .clickable(onClick = onEdit)
            .padding(horizontal = Space16, vertical = Space12)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space12)
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Checkbox(
                    checked = false,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        uncheckedColor = ScheduleTheme.colors.textMuted
                    )
                )
            }

            CategoryTile(category = task.category)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Space4)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ScheduleTheme.colors.text,
                    maxLines = 1,
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

                Text(
                    text = TimeUtils.formatTimeRange(task.startMinutes, task.endMinutes),
                    style = MaterialTheme.typography.bodySmall,
                    color = ScheduleTheme.colors.accent,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun MissedTaskRow(
    task: ScheduleTask,
    onMarkDone: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { }
            .clip(RoundedCornerShape(18.dp))
            .background(ScheduleTheme.colors.surface)
            .border(
                1.dp,
                ScheduleTheme.colors.warning.copy(alpha = 0.4f),
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onEdit)
            .padding(horizontal = Space16, vertical = Space12)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space12)
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Checkbox(
                    checked = false,
                    onCheckedChange = { onMarkDone() },
                    colors = CheckboxDefaults.colors(
                        uncheckedColor = ScheduleTheme.colors.warning
                    )
                )
            }

            CategoryTile(category = task.category)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Space4)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ScheduleTheme.colors.text,
                    maxLines = 1,
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

                Text(
                    text = TimeUtils.formatTimeRange(task.startMinutes, task.endMinutes),
                    style = MaterialTheme.typography.bodySmall,
                    color = ScheduleTheme.colors.warning,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Button(
                onClick = onMarkDone,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ScheduleTheme.colors.warning,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "Mark done",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DoneTaskRow(
    task: ScheduleTask,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { }
            .alpha(0.6f)
            .clip(RoundedCornerShape(18.dp))
            .background(ScheduleTheme.colors.taskDoneBackground)
            .border(1.dp, ScheduleTheme.colors.taskDoneBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = Space16, vertical = Space12)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space12)
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Checkbox(
                    checked = true,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = ScheduleTheme.colors.accent,
                        checkmarkColor = ScheduleTheme.colors.background
                    )
                )
            }

            CategoryTile(category = task.category)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Space4)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ScheduleTheme.colors.taskDoneText,
                    textDecoration = TextDecoration.LineThrough,
                    maxLines = 1,
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

                Text(
                    text = TimeUtils.formatTimeRange(task.startMinutes, task.endMinutes),
                    style = MaterialTheme.typography.bodySmall,
                    color = ScheduleTheme.colors.taskDoneText,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun EmptyTabCard(
    emoji: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ScheduleTheme.colors.surface)
            .border(1.dp, ScheduleTheme.colors.surfaceVariant, RoundedCornerShape(18.dp))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Space8)
        ) {
            Text(
                text = emoji,
                fontSize = 32.sp
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = ScheduleTheme.colors.text,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = ScheduleTheme.colors.textMuted
            )
        }
    }
}
