package com.saif.scheduleapp.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AppPrimary,
    onPrimary = AppTextMain,
    primaryContainer = AppPrimaryContainer,
    onPrimaryContainer = AppTextMain,
    secondary = AppAccent,
    onSecondary = AppBackground,
    secondaryContainer = AppAccentContainer,
    onSecondaryContainer = AppAccent,
    background = AppBackground,
    onBackground = AppTextMain,
    surface = AppCardBackground,
    onSurface = AppTextMain,
    surfaceVariant = AppCardBorder,
    onSurfaceVariant = AppTextMuted
)

@Composable
fun ScheduleAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        content = content
    )
}
