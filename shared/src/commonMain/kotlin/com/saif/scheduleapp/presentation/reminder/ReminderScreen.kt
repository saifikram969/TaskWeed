package com.saif.scheduleapp.presentation.reminder

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.saif.scheduleapp.core.util.TimeUtils
import com.saif.scheduleapp.domain.model.Category

/**
 * Stateless full-screen reminder display.
 */
@Composable
fun ReminderScreen(
    state: ReminderUiState,
    onStartNow: () -> Unit,
    onSnooze: () -> Unit,
    modifier: Modifier = Modifier
) {
    val task = state.task

    val categoryIcon = when (task?.category) {
        Category.LEARNING -> "📖"
        Category.JOBS -> "💼"
        Category.PRAYER -> "🤲"
        Category.REST -> "🌙"
        null -> "⏰"
    }

    val timeRange = if (task != null) {
        TimeUtils.formatTimeRange(task.startMinutes, task.endMinutes)
    } else {
        ""
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = AppBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.6f))

            // Large category icon inside a violet circle
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(AppPrimaryContainer)
                    .border(3.dp, AppPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = categoryIcon,
                    fontSize = 48.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Category badge
            if (task != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppPrimary.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = task.category.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = AppPrimary,
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
                color = AppTextMain,
                textAlign = TextAlign.Center
            )

            if (timeRange.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                // Task time range
                Text(
                    text = timeRange,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppAccent
                )
            }

            // Task details
            if (!task?.details.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = task.details,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppTextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

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
                        containerColor = AppPrimary,
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
                    border = BorderStroke(1.dp, AppCardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = AppCardBackground,
                        contentColor = AppTextMain
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
