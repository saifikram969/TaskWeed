package com.saif.scheduleapp.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ScheduleSpacing(
    val space4: Dp = 4.dp,
    val space8: Dp = 8.dp,
    val space12: Dp = 12.dp,
    val space16: Dp = 16.dp,
    val space24: Dp = 24.dp,
    val space32: Dp = 32.dp
) {
    val extraSmall: Dp = space4
    val small: Dp = space8
    val medium: Dp = space16
    val large: Dp = space24
    val extraLarge: Dp = space32
}

val Space4: Dp = 4.dp
val Space8: Dp = 8.dp
val Space12: Dp = 12.dp
val Space16: Dp = 16.dp
val Space24: Dp = 24.dp
val Space32: Dp = 32.dp

