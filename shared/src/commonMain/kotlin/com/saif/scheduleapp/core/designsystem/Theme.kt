package com.saif.scheduleapp.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkText,
    primaryContainer = DarkPrimaryMuted,
    onPrimaryContainer = DarkText,
    secondary = DarkAccent,
    onSecondary = DarkBackground,
    secondaryContainer = DarkAccentContainer,
    onSecondaryContainer = DarkAccent,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextMuted,
    error = DarkDanger,
    onError = DarkText,
    errorContainer = DarkDangerContainer,
    onErrorContainer = DarkDanger
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = LightPrimaryMuted,
    onPrimaryContainer = LightPrimary,
    secondary = LightAccent,
    onSecondary = Color.White,
    secondaryContainer = LightAccentContainer,
    onSecondaryContainer = LightAccent,
    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextMuted,
    error = LightDanger,
    onError = Color.White,
    errorContainer = LightDangerContainer,
    onErrorContainer = LightDanger
)

val ScheduleShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

object ScheduleTheme {
    val colors: ScheduleColors
        @Composable
        @ReadOnlyComposable
        get() = LocalScheduleColors.current

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.shapes

    val spacing: ScheduleSpacing
        get() = ScheduleSpacing()
}

@Composable
fun ScheduleTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val scheduleColors = if (darkTheme) DarkScheduleColors else LightScheduleColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalScheduleColors provides scheduleColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = ScheduleShapes,
            content = content
        )
    }
}

// Backwards compatibility alias
@Composable
fun ScheduleAppTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) = ScheduleTheme(darkTheme = darkTheme, content = content)
