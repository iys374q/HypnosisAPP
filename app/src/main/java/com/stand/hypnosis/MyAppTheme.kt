package com.stand.hypnosis

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 定义主题
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFa500a2),
    onPrimary = Color(0xFF252525),
    surface = Color(0xFF252525),
    onSurface = Color(0xFFE9E9E9),
    secondary = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFf036ce),
    onPrimary = Color(0xFFD7D7D7),
    surface = Color(0xFFD7D7D7),
    onSurface = Color(0xFF2A2A2A),
    secondary = Color.Black
)

@Composable
fun MyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}