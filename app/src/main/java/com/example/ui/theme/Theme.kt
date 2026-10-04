package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = TechSlateDark,
    primaryContainer = CyanDark,
    onPrimaryContainer = CyanLight,
    secondary = CyanPrimaryVariant,
    onSecondary = TechSlateDark,
    secondaryContainer = TechCardBorderDark,
    onSecondaryContainer = CyanLight,
    tertiary = TechAccentGreen,
    onTertiary = TechSlateDark,
    background = TechSlateDark,
    onBackground = TechTextPrimaryDark,
    surface = TechSurfaceDark,
    onSurface = TechTextPrimaryDark,
    surfaceVariant = TechCardDark,
    onSurfaceVariant = TechTextSecondaryDark,
    outline = TechCardBorderDark,
    error = TechAccentRed
)

private val LightColorScheme = lightColorScheme(
    primary = CyanPrimaryLight,
    onPrimary = TechSurfaceLight,
    primaryContainer = CyanLight,
    onPrimaryContainer = CyanDark,
    secondary = CyanPrimaryVariant,
    onSecondary = TechSurfaceLight,
    secondaryContainer = TechCardLight,
    onSecondaryContainer = CyanPrimaryLight,
    tertiary = TechAccentGreen,
    onTertiary = TechSurfaceLight,
    background = TechSlateLight,
    onBackground = TechTextPrimaryLight,
    surface = TechSurfaceLight,
    onSurface = TechTextPrimaryLight,
    surfaceVariant = TechCardLight,
    onSurfaceVariant = TechTextSecondaryLight,
    outline = TechCardBorderLight,
    error = TechAccentRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to dark cyber-robotics theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
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
