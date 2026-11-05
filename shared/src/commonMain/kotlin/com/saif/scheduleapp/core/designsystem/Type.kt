package com.saif.scheduleapp.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// TODO: Plus Jakarta Sans font files are missing from shared/src/commonMain/composeResources/font/.
// Required font filenames:
// - plus_jakarta_sans_regular.ttf
// - plus_jakarta_sans_medium.ttf
// - plus_jakarta_sans_semibold.ttf
// - plus_jakarta_sans_bold.ttf
// When font files are added, initialize:
// val PlusJakartaSansFamily = FontFamily(
//     Font(Res.font.plus_jakarta_sans_regular, FontWeight.Normal),
//     Font(Res.font.plus_jakarta_sans_medium, FontWeight.Medium),
//     Font(Res.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
//     Font(Res.font.plus_jakarta_sans_bold, FontWeight.Bold)
// )

val AppFontFamily: FontFamily = FontFamily.Default

val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.5).sp,
        color = AppTextMain
    ),
    headlineMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        color = AppTextMain
    ),
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 23.sp,
        color = AppTextMain
    ),
    titleMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        color = AppTextMain
    ),
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = AppTextMain
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = AppTextMuted
    ),
    labelLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = AppTextMain
    ),
    labelSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
        color = AppTextMuted
    )
)
