package com.zxxf.assistant.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Blue500,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD2E3FC),
    onPrimaryContainer = Blue700,
    secondary = Color(0xFF5F6368),
    onSecondary = Color.White,
    background = GrayBg,
    onBackground = GrayDark,
    surface = Color.White,
    onSurface = GrayDark,
    error = Red500,
    onError = Color.White,
    surfaceVariant = Color(0xFFE8EAED),
    outline = Color(0xFFDADCE0)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Blue700,
    primaryContainer = Blue700,
    onPrimaryContainer = Color(0xFFD2E3FC),
    secondary = Color(0xFFBDC1C6),
    onSecondary = Color(0xFF202124),
    background = Color(0xFF202124),
    onBackground = Color(0xFFE8EAED),
    surface = Color(0xFF303134),
    onSurface = Color(0xFFE8EAED),
    error = Color(0xFFF28B82),
    onError = Color(0xFF202124),
    surfaceVariant = Color(0xFF3C4043),
    outline = Color(0xFF5F6368)
)

@Composable
fun AssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
