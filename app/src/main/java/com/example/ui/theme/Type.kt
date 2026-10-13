package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// Custom font family using locally bundled Plus Jakarta Sans
val SalimFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

// Monospace font family for syntax-highlighted code blocks
val SalimCodeFontFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium)
)

// Six fixed typography scales for Salim (34, 28, 20, 17, 15, 13sp)
object SalimTextStyles {
    val HeadlineLarge = TextStyle(
        fontFamily = SalimFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    )

    val HeadlineMedium = TextStyle(
        fontFamily = SalimFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.3).sp
    )

    val Title = TextStyle(
        fontFamily = SalimFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2).sp
    )

    val Body = TextStyle(
        fontFamily = SalimFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

    val Secondary = TextStyle(
        fontFamily = SalimFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )

    val Caption = TextStyle(
        fontFamily = SalimFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.1.sp
    )

    val Code = TextStyle(
        fontFamily = SalimCodeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )
}

val SalimTypography = Typography(
    displayLarge = SalimTextStyles.HeadlineLarge,
    headlineMedium = SalimTextStyles.HeadlineMedium,
    titleLarge = SalimTextStyles.Title,
    bodyLarge = SalimTextStyles.Body,
    bodyMedium = SalimTextStyles.Secondary,
    labelSmall = SalimTextStyles.Caption
)
