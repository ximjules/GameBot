package com.gamebot.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GameBotColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    secondary = PrimaryBlue,
    tertiary = PurpleLight,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariant,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed
)

@Composable
fun GameBotTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GameBotColorScheme,
        typography = Typography,
        content = content
    )
}