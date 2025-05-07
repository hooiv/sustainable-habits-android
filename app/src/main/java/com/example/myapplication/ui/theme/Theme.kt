package com.example.myapplication.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GreenDark,
    secondary = AccentColor,
    tertiary = Pink80,
    background = Color(0xFF1C1B1F), // Default dark background
    surface = Color(0xFF1C1B1F),
    onPrimary = TextOnGreen,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    primaryContainer = GreenDark, // For TopAppBar example
    onPrimaryContainer = TextOnGreen
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    secondary = AccentColor,
    tertiary = Pink40,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = TextOnGreen,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    primaryContainer = GreenLight, // For TopAppBar example
    onPrimaryContainer = Color.Black

    /* Other default colors to override
    surfaceVariant = MaterialTheme.colorScheme.surfaceVariant,
    outline = MaterialTheme.colorScheme.outline,
    inverseOnSurface = MaterialTheme.colorScheme.inverseOnSurface,
    inverseSurface = MaterialTheme.colorScheme.inverseSurface,
    inversePrimary = MaterialTheme.colorScheme.inversePrimary,
    surfaceTint = MaterialTheme.colorScheme.surfaceTint,
    outlineVariant = MaterialTheme.colorScheme.outlineVariant,
    scrim = MaterialTheme.colorScheme.scrim,
    */
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme // Adjusted for better contrast with GreenPrimary/GreenDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // We'll create Typography.kt next
        content = content
    )
}
