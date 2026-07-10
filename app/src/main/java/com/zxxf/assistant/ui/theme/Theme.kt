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
    primary = Blue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = Teal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF134E4A),
    tertiary = Peach,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFEDD5),
    onTertiaryContainer = Color(0xFF9A3412),
    background = Base,
    onBackground = Text,
    surface = Color.White,
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

private val DarkColorScheme = darkColorScheme(
    primary = BlueDark,
    onPrimary = CrustDark,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = TextDark,
    secondary = TealDark,
    onSecondary = CrustDark,
    secondaryContainer = Color(0xFF134E4A),
    onSecondaryContainer = TextDark,
    tertiary = PeachDark,
    onTertiary = CrustDark,
    tertiaryContainer = Color(0xFF7C2D12),
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
fun CorpKnowCompassTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
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
