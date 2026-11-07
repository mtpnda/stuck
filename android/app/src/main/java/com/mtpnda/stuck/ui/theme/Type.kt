package com.mtpnda.stuck.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    displayMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = DlsType.displayMediumSize.sp,
        lineHeight = DlsType.displayMediumLine.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = DlsType.headlineLargeSize.sp,
        lineHeight = DlsType.headlineLargeLine.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = DlsType.titleLargeSize.sp,
        lineHeight = DlsType.titleLargeLine.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = DlsType.titleMediumSize.sp,
        lineHeight = DlsType.titleMediumLine.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = DlsType.bodyLargeSize.sp,
        lineHeight = DlsType.bodyLargeLine.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = DlsType.bodyMediumSize.sp,
        lineHeight = DlsType.bodyMediumLine.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = DlsType.bodySmallSize.sp,
        lineHeight = DlsType.bodySmallLine.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = DlsType.labelLargeSize.sp,
        lineHeight = DlsType.labelLargeLine.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = DlsType.labelMediumSize.sp,
        lineHeight = DlsType.labelMediumLine.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = DlsType.labelSmallSize.sp,
        lineHeight = DlsType.labelSmallLine.sp
    )
)
