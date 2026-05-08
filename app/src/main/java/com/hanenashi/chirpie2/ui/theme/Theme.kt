package com.hanenashi.chirpie2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ChirpieDarkColors = darkColorScheme(
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2A2A2A),
    primary = Color(0xFFE0E0E0),
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFFC8C8C8)
)

@Composable
fun ChirpieTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ChirpieDarkColors,
        content = content
    )
}
