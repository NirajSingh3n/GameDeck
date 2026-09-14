package com.gamedeck.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GameDeckColors = darkColorScheme(
    primary = Color(0xFFFF3D5A),
    secondary = Color(0xFF00E5FF),
    tertiary = Color(0xFF7C4DFF),
    background = Color(0xFF0B0E14),
    surface = Color(0xFF131722),
    surfaceVariant = Color(0xFF1B2130),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color(0xFFE8ECF5),
    onSurface = Color(0xFFE8ECF5),
    onSurfaceVariant = Color(0xFFB8C0D4)
)

@Composable
fun GameDeckTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = GameDeckColors, content = content)
}
