package com.example.nextstoprealtime.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BluePrimary = Color(0xFF1565C0)
private val BlueSecondary = Color(0xFF0277BD)
private val LiveGreen = Color(0xFF2E7D32)
private val DelayOrange = Color(0xFFEF6C00)

private val LightColors = lightColorScheme(
    primary = BluePrimary,
    secondary = BlueSecondary,
    tertiary = LiveGreen,
    surface = Color(0xFFF5F7FA),
    onSurface = Color(0xFF1A1C1E)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF90CAF9),
    secondary = Color(0xFF81D4FA),
    tertiary = Color(0xFF81C784),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFE3E2E6)
)

@Composable
fun NextStopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
