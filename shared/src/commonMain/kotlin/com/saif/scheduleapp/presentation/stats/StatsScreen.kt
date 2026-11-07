package com.saif.scheduleapp.presentation.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saif.scheduleapp.core.designsystem.ScheduleTheme
import com.saif.scheduleapp.core.designsystem.Space12
import com.saif.scheduleapp.core.designsystem.Space16
import com.saif.scheduleapp.core.designsystem.Space24
import com.saif.scheduleapp.core.designsystem.Space32
import com.saif.scheduleapp.core.designsystem.Space4
import com.saif.scheduleapp.domain.progress.DayStats
import com.saif.scheduleapp.domain.progress.STREAK_THRESHOLD
import kotlin.math.roundToInt

/**
 * Stateless Stats Screen composable.
 */
@Composable
fun StatsScreen(
    state: StatsUiState,
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
                contentPadding = PaddingValues(top = Space24, bottom = Space32),
                verticalArrangement = Arrangement.spacedBy(Space24)
            ) {
                // Header
                item {
                    Column {
                        Text(
                            text = "Overview",
                            style = MaterialTheme.typography.labelSmall,
                            color = ScheduleTheme.colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(Space4))
                        Text(
                            text = "Stats & Streaks",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = ScheduleTheme.colors.text
                        )
                    }
                }

                if (state.isEmpty) {
                    item {
                        EmptyStatsCard()
                    }
                } else {
                    // 3 Statistic Cards
                    item {
                        StatCardsRow(
                            currentStreak = state.currentStreak,
                            longestStreak = state.longestStreak,
                            averageLast30 = state.averageLast30
                        )
                    }

                    // Last 7 Days Bar Chart
                    item {
                        Last7DaysChartCard(last7 = state.last7)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCardsRow(
    currentStreak: Int,
    longestStreak: Int,
    averageLast30: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Space12)
    ) {
        StatCard(
            title = "Current",
            value = "$currentStreak",
            subtitle = if (currentStreak == 1) "day streak" else "days streak",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Longest",
            value = "$longestStreak",
            subtitle = if (longestStreak == 1) "day streak" else "days streak",
            modifier = Modifier.weight(1f)
        )
        val formattedAvg = (averageLast30 * 10f).roundToInt() / 10f
        StatCard(
            title = "30-Day Avg",
            value = "$formattedAvg",
            subtitle = "tasks / day",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ScheduleTheme.colors.surface)
            .border(1.dp, ScheduleTheme.colors.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(Space16)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Space4)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = ScheduleTheme.colors.textMuted
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ScheduleTheme.colors.text
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = ScheduleTheme.colors.textMuted
            )
        }
    }
}

@Composable
private fun Last7DaysChartCard(
    last7: List<DayStats>,
    modifier: Modifier = Modifier
) {
    val days = last7.takeLast(7)
    val primaryViolet = ScheduleTheme.colors.primary
    val mutedViolet = ScheduleTheme.colors.primaryMuted
    val surfaceVariant = ScheduleTheme.colors.surfaceVariant
    val warningColor = ScheduleTheme.colors.warning
    val textMuted = ScheduleTheme.colors.textMuted

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ScheduleTheme.colors.surface)
            .border(1.dp, ScheduleTheme.colors.surfaceVariant, RoundedCornerShape(20.dp))
            .padding(Space24)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Last 7 Days",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ScheduleTheme.colors.text
                )
                Text(
                    text = "80% goal",
                    style = MaterialTheme.typography.labelSmall,
                    color = warningColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(Space24))

            // Chart area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val count = days.size.coerceAtLeast(1)
                    val slotWidth = size.width / count
                    val barWidth = (slotWidth * 0.45f).coerceIn(16.dp.toPx(), 32.dp.toPx())
                    val chartHeight = size.height

                    // 1. Draw dashed horizontal line at 80% streak threshold
                    val thresholdY = chartHeight * (1f - STREAK_THRESHOLD)
                    drawLine(
                        color = warningColor.copy(alpha = 0.6f),
                        start = Offset(0f, thresholdY),
                        end = Offset(size.width, thresholdY),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )

                    // 2. Draw bars
                    days.forEachIndexed { i, day ->
                        val ratio = if (day.total > 0) {
                            (day.done.toFloat() / day.total.toFloat()).coerceIn(0f, 1f)
                        } else {
                            0f
                        }
                        val isToday = i == days.lastIndex
                        val barColor = if (isToday) primaryViolet else mutedViolet

                        val centerX = (i + 0.5f) * slotWidth
                        val left = centerX - barWidth / 2f

                        // Background track (0% to 100%)
                        drawRoundRect(
                            color = surfaceVariant.copy(alpha = 0.35f),
                            topLeft = Offset(left, 0f),
                            size = Size(barWidth, chartHeight),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                        )

                        // Filled bar
                        val fillHeight = chartHeight * ratio
                        if (fillHeight > 0f) {
                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(left, chartHeight - fillHeight),
                                size = Size(barWidth, fillHeight),
                                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Space12))

            // Day letters row below bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                days.forEachIndexed { i, day ->
                    val isToday = i == days.lastIndex
                    val dayLetter = day.date.dayOfWeek.name.take(1)
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayLetter,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                            color = if (isToday) primaryViolet else textMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStatsCard(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ScheduleTheme.colors.surface)
            .border(1.dp, ScheduleTheme.colors.surfaceVariant, RoundedCornerShape(20.dp))
            .padding(Space32),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Space12)
        ) {
            Text(
                text = "📊",
                fontSize = 40.sp
            )
            Text(
                text = "Add tasks to see your progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = ScheduleTheme.colors.text
            )
            Text(
                text = "Your streaks, completion charts, and daily statistics will appear here once you create tasks.",
                style = MaterialTheme.typography.bodyMedium,
                color = ScheduleTheme.colors.textMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

