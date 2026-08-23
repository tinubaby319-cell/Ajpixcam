package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NaturalColorScheme = lightColorScheme(
    primary = NaturalOlive,
    onPrimary = NaturalBg,
    primaryContainer = NaturalCard,
    onPrimaryContainer = NaturalDark,
    secondary = NaturalMoss,
    onSecondary = NaturalBg,
    secondaryContainer = NaturalSurface,
    onSecondaryContainer = NaturalDark,
    tertiary = NaturalSage,
    onTertiary = NaturalBg,
    background = NaturalBg,
    onBackground = NaturalDark,
    surface = NaturalSurface,
    onSurface = NaturalDark,
    surfaceVariant = NaturalCard,
    onSurfaceVariant = NaturalTextMuted,
    outline = NaturalBorder,
    outlineVariant = NaturalBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NaturalColorScheme,
        typography = Typography,
        content = content
    )
}
