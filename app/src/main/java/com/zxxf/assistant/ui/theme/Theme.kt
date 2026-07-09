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

// ═══ Catppuccin Latte — Light Color Scheme ═══

private val LightColorScheme = lightColorScheme(
    primary = Blue,
    onPrimary = Color.White,
    primaryContainer = Surface0,
    onPrimaryContainer = Text,
    secondary = Lavender,
    onSecondary = Color.White,
    secondaryContainer = Surface0,
    onSecondaryContainer = Text,
    tertiary = Mauve,
    onTertiary = Color.White,
    tertiaryContainer = Surface0,
    onTertiaryContainer = Text,
    background = Base,
    onBackground = Text,
    surface = Base,
    onSurface = Text,
    surfaceVariant = Surface0,
    onSurfaceVariant = Subtext0,
    error = Red,
    onError = Color.White,
    errorContainer = Maroon.copy(alpha = 0.2f),
    onErrorContainer = Text,
    outline = Overlay1,
    outlineVariant = Surface1,
)

// ═══ Catppuccin Mocha — Dark Color Scheme ═══

private val DarkColorScheme = darkColorScheme(
    primary = BlueDark,
    onPrimary = CrustDark,
    primaryContainer = Surface0Dark,
    onPrimaryContainer = TextDark,
    secondary = LavenderDark,
    onSecondary = CrustDark,
    secondaryContainer = Surface0Dark,
    onSecondaryContainer = TextDark,
    tertiary = MauveDark,
    onTertiary = CrustDark,
    tertiaryContainer = Surface0Dark,
    onTertiaryContainer = TextDark,
    background = BaseDark,
    onBackground = TextDark,
    surface = BaseDark,
    onSurface = TextDark,
    surfaceVariant = Surface0Dark,
    onSurfaceVariant = Subtext0Dark,
    error = RedDark,
    onError = CrustDark,
    errorContainer = MaroonDark.copy(alpha = 0.2f),
    onErrorContainer = TextDark,
    outline = Overlay1Dark,
    outlineVariant = Surface1Dark,
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
            // Status bar follows surface background (softer Catppuccin look)
            window.statusBarColor = colorScheme.surface.toArgb()
            // Navigation bar also follows surface for immersive feel
            window.navigationBarColor = colorScheme.surface.toArgb()
            // Light theme: dark icons on light bars; Dark theme: light icons on dark bars
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
