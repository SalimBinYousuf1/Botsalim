package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryButtonBgDark,
    onPrimary = PrimaryButtonTextDark,
    secondary = SecondaryButtonBgDark,
    onSecondary = SecondaryButtonTextDark,
    background = WarmNearBlackBottom,
    onBackground = HeadlineDark,
    surface = CardSurfaceDark,
    onSurface = HeadlineDark,
    outline = SecondaryButtonBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryButtonBgLight,
    onPrimary = PrimaryButtonTextLight,
    secondary = SecondaryButtonBgLight,
    onSecondary = SecondaryButtonTextLight,
    background = PaleWarmCreamBottom,
    onBackground = HeadlineLight,
    surface = CardSurfaceLight,
    onSurface = HeadlineLight,
    outline = SecondaryButtonBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SalimTypography,
        content = content
    )
}
