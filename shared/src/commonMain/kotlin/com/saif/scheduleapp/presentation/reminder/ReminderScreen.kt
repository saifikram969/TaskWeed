package com.saif.scheduleapp.presentation.reminder

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saif.scheduleapp.core.designsystem.CategoryIcon
import com.saif.scheduleapp.core.designsystem.ScheduleTheme
import com.saif.scheduleapp.core.designsystem.categoryColor
import com.saif.scheduleapp.core.util.TimeUtils
import kotlinx.coroutines.delay

/**
 * Stateless full-screen reminder display with pulse animation and voice sound wave.
 */
@Composable
fun ReminderScreen(
    state: ReminderUiState,
    onStartNow: () -> Unit,
    onSnooze: () -> Unit,
    modifier: Modifier = Modifier,
    isScreenVisible: Boolean = true
) {
    val task = state.task
    val voiceEnabled = task?.voiceEnabled == true

    // 8-second speaking countdown for screen opening when voice is enabled
    var screenOpenedSpeaking by remember(task?.id, voiceEnabled) {
        mutableStateOf(voiceEnabled)
    }

    LaunchedEffect(task?.id, voiceEnabled) {
        if (voiceEnabled) {
            screenOpenedSpeaking = true
            delay(8000L)
            screenOpenedSpeaking = false
        } else {
            screenOpenedSpeaking = false
        }
    }

    // Stop speaking immediately if state.isSpeaking explicitly turned off
    LaunchedEffect(state.isSpeaking) {
        if (!state.isSpeaking && task != null) {
            screenOpenedSpeaking = false
        }
    }

    val isSpeaking = isScreenVisible && voiceEnabled && (state.isSpeaking || screenOpenedSpeaking)

    val timeRange = if (task != null) {
        TimeUtils.formatTimeRange(task.startMinutes, task.endMinutes)
    } else {
        ""
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = ScheduleTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.6f))

            // Large category icon inside violet circle with pulse animation
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isScreenVisible) {
                    val pulseTransition = rememberInfiniteTransition(label = "pulseTransition")
                    val pulseScale by pulseTransition.animateFloat(
                        initialValue = 1.0f,
                        targetValue = 1.25f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1200, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "pulseScale"
                    )
                    val pulseAlpha by pulseTransition.animateFloat(
                        initialValue = 0.6f,
                        targetValue = 0.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1200, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "pulseAlpha"
                    )

                    // Outer pulsing circle
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .graphicsLayer {
                                scaleX = pulseScale
                                scaleY = pulseScale
                                alpha = pulseAlpha
                            }
                            .clip(CircleShape)
                            .background(ScheduleTheme.colors.primary.copy(alpha = 0.35f))
                            .border(2.dp, ScheduleTheme.colors.primary.copy(alpha = 0.6f), CircleShape)
                    )
                }

                // Inner circle with category icon
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .background(ScheduleTheme.colors.primaryMuted)
                        .border(3.dp, ScheduleTheme.colors.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (task != null) {
                        CategoryIcon(
                            category = task.category,
                            tint = ScheduleTheme.colors.primary,
                            size = 52.dp
                        )
                    } else {
                        Text(
                            text = "⏰",
                            fontSize = 48.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Category badge
            if (task != null) {
                val catColor = categoryColor(task.category)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(catColor.copy(alpha = 0.15f))
                        .border(1.dp, catColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = task.category.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = catColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Task title
            Text(
                text = task?.title?.ifBlank { "Task Reminder" } ?: "Task Reminder",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ScheduleTheme.colors.text,
                textAlign = TextAlign.Center
            )

            if (timeRange.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                // Task time range
                Text(
                    text = timeRange,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ScheduleTheme.colors.accent
                )
            }

            // Task details
            if (!task?.details.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = task.details,
                    style = MaterialTheme.typography.bodyLarge,
                    color = ScheduleTheme.colors.textMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sound Wave (nine rounded bars)
            SoundWave(
                isSpeaking = isSpeaking,
                activeColor = ScheduleTheme.colors.primary,
                inactiveColor = ScheduleTheme.colors.surfaceVariant
            )

            Spacer(modifier = Modifier.weight(1.0f))

            // Action buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onStartNow,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ScheduleTheme.colors.primary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Start now",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onSnooze,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ScheduleTheme.colors.surfaceVariant),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = ScheduleTheme.colors.surface,
                        contentColor = ScheduleTheme.colors.text
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Snooze 5 min",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Nine rounded bars sound wave visualizer.
 * Animates heights while isSpeaking is true; stays still at resting heights when false.
 */
@Composable
fun SoundWave(
    isSpeaking: Boolean,
    modifier: Modifier = Modifier,
    activeColor: Color = ScheduleTheme.colors.primary,
    inactiveColor: Color = ScheduleTheme.colors.surfaceVariant
) {
    val barCount = 9
    val barWidth = 4.dp
    val spacing = 5.dp

    val minHeights = remember {
        listOf(6.dp, 8.dp, 10.dp, 8.dp, 12.dp, 8.dp, 10.dp, 8.dp, 6.dp)
    }
    val targetHeights = remember {
        listOf(18.dp, 28.dp, 40.dp, 26.dp, 44.dp, 32.dp, 38.dp, 22.dp, 14.dp)
    }
    val durations = remember {
        listOf(450, 600, 380, 520, 340, 480, 560, 420, 500)
    }

    Row(
        modifier = modifier.height(48.dp),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSpeaking) {
            val transition = rememberInfiniteTransition(label = "soundWaveTransition")
            for (i in 0 until barCount) {
                val animatedHeight by transition.animateValue(
                    initialValue = minHeights[i],
                    targetValue = targetHeights[i],
                    typeConverter = Dp.VectorConverter,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = durations[i],
                            easing = FastOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "waveBar_$i"
                )
                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .height(animatedHeight)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(activeColor)
                )
            }
        } else {
            for (i in 0 until barCount) {
                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .height(minHeights[i])
                        .clip(RoundedCornerShape(percent = 50))
                        .background(inactiveColor)
                )
            }
        }
    }
}
