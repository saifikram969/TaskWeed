package com.saif.scheduleapp.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.saif.scheduleapp.domain.model.Category

// --- Dark Color Tokens ---
val DarkBackground = Color(0xFF0F1117)
val DarkSurface = Color(0xFF1A1D27)
val DarkSurfaceVariant = Color(0xFF272B3B)
val DarkPrimary = Color(0xFF6C5CE7)
val DarkPrimaryMuted = Color(0xFF282352)
val DarkAccent = Color(0xFF00D2A0)
val DarkAccentContainer = Color(0xFF00382A)
val DarkWarning = Color(0xFFFF9F43)
val DarkText = Color(0xFFFFFFFF)
val DarkTextMuted = Color(0xFFA0A6B8)
val DarkDanger = Color(0xFFFF6B6B)
val DarkDangerContainer = Color(0xFF2D1619)

// --- Light Color Tokens ---
val LightBackground = Color(0xFFF8F9FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE5E7EB)
val LightPrimary = Color(0xFF6C5CE7)
val LightPrimaryMuted = Color(0xFFECE9FE)
val LightAccent = Color(0xFF00B88C)
val LightAccentContainer = Color(0xFFE6F8F3)
val LightWarning = Color(0xFFE67E22)
val LightText = Color(0xFF111827)
val LightTextMuted = Color(0xFF6B7280)
val LightDanger = Color(0xFFDC2626)
val LightDangerContainer = Color(0xFFFEE2E2)

// --- Category Styling Colors ---
val CategoryLearning = Color(0xFF6C5CE7)
val CategoryJobs = Color(0xFF3867D6)
val CategoryPrayer = Color(0xFF00D2A0)
val CategoryRest = Color(0xFFFF9F43)

// --- Task Done Tokens ---
val TaskDoneBackground = Color(0xFF141720)
val TaskDoneBorder = Color(0xFF1E222F)
val TaskDoneText = Color(0xFF636E72)
val LightTaskDoneBackground = Color(0xFFF1F3F5)
val LightTaskDoneBorder = Color(0xFFE2E8F0)
val LightTaskDoneText = Color(0xFF94A3B8)

// --- Legacy / Direct App Aliases (preserved for compatibility) ---
val AppBackground = DarkBackground
val AppCardBackground = DarkSurface
val AppCardBorder = DarkSurfaceVariant
val AppPrimary = DarkPrimary
val AppPrimaryContainer = DarkPrimaryMuted
val AppAccent = DarkAccent
val AppAccentContainer = Color(0xFF00382A)
val AppTextMain = DarkText
val AppTextMuted = DarkTextMuted
val AppWarning = DarkWarning
val AppDanger = DarkDanger
val AppDangerContainer = DarkDangerContainer

@Immutable
data class ScheduleColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val primaryMuted: Color,
    val accent: Color,
    val warning: Color,
    val text: Color,
    val textMuted: Color,
    val danger: Color,
    val dangerContainer: Color,
    val categoryLearning: Color = CategoryLearning,
    val categoryJobs: Color = CategoryJobs,
    val categoryPrayer: Color = CategoryPrayer,
    val categoryRest: Color = CategoryRest,
    val taskDoneBackground: Color = TaskDoneBackground,
    val taskDoneBorder: Color = TaskDoneBorder,
    val taskDoneText: Color = TaskDoneText,
    val isDark: Boolean = true
) {
    fun categoryColor(category: Category): Color = when (category) {
        Category.LEARNING -> categoryLearning
        Category.JOBS -> categoryJobs
        Category.PRAYER -> categoryPrayer
        Category.REST -> categoryRest
    }
}

val DarkScheduleColors = ScheduleColors(
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    primary = DarkPrimary,
    primaryMuted = DarkPrimaryMuted,
    accent = DarkAccent,
    warning = DarkWarning,
    text = DarkText,
    textMuted = DarkTextMuted,
    danger = DarkDanger,
    dangerContainer = DarkDangerContainer,
    isDark = true
)

val LightScheduleColors = ScheduleColors(
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    primary = LightPrimary,
    primaryMuted = LightPrimaryMuted,
    accent = LightAccent,
    warning = LightWarning,
    text = LightText,
    textMuted = LightTextMuted,
    danger = LightDanger,
    dangerContainer = LightDangerContainer,
    taskDoneBackground = LightTaskDoneBackground,
    taskDoneBorder = LightTaskDoneBorder,
    taskDoneText = LightTaskDoneText,
    isDark = false
)

val LocalScheduleColors = staticCompositionLocalOf { DarkScheduleColors }

fun categoryColor(category: Category): Color = when (category) {
    Category.LEARNING -> CategoryLearning
    Category.JOBS -> CategoryJobs
    Category.PRAYER -> CategoryPrayer
    Category.REST -> CategoryRest
}
