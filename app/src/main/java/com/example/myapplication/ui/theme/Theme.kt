package com.example.myapplication.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Create composition locals for our anime-inspired theme properties
data class AnimationSpec(
    val duration: Int = 500,
    val easing: androidx.compose.animation.core.Easing = androidx.compose.animation.core.FastOutSlowInEasing
)

data class Gradients(
    val primary: List<Color>,
    val secondary: List<Color>,
    val accent: List<Color>,
    val surface: List<Color>
)

data class AnimatedTheme(
    val defaultAnimation: AnimationSpec = AnimationSpec(),
    val gradients: Gradients,
    val animatedElevation: ElevationValues = ElevationValues()
)

data class ElevationValues(
    val default: Float = 4f,
    val pressed: Float = 2f,
    val card: Float = 6f,
    val dialog: Float = 24f
)

// Create composition locals
val LocalAnimatedTheme = compositionLocalOf { 
    AnimatedTheme(
        gradients = Gradients(
            primary = listOf(GreenPrimary, GreenDark),
            secondary = listOf(AccentColor, GreenPrimary),
            accent = listOf(NeonPink, NeonPurple),
            surface = listOf(CardLight.copy(alpha = 0.8f), CardLight)
        )
    ) 
}

val LocalAnimationSpec = compositionLocalOf { AnimationSpec() }

// Enhanced dark color scheme with anime.js/three.js inspired colors
private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    secondary = NeonBlue,
    tertiary = NeonPurple,
    background = Color(0xFF121212), // Darker background for better contrast with neon colors
    surface = CardDark,
    onPrimary = Color.Black, // Text on neon colors should be dark for better readability
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    primaryContainer = GreenDark,
    onPrimaryContainer = TextOnGreen,
    surfaceVariant = ElevatedSurfaceDark,
    error = Color(0xFFFF5252) // Bright red for errors
)

// Enhanced light color scheme with anime.js/three.js inspired colors
private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    secondary = NeonBlue.copy(alpha = 0.8f), // Slightly muted for light theme
    tertiary = NeonPurple.copy(alpha = 0.8f),
    background = Color(0xFFF8F8F8), // Slightly off-white for better eye comfort
    surface = CardLight,
    onPrimary = TextOnGreen,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    primaryContainer = GreenLight,
    onPrimaryContainer = Color.Black,
    surfaceVariant = ElevatedSurfaceLight,
    error = Color(0xFFB00020) // Standard Material Design error color
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
    
    // Define animated theme properties based on dark/light theme
    val animatedTheme = if (darkTheme) {
        AnimatedTheme(
            gradients = Gradients(
                primary = listOf(NeonGreen, NeonBlue),
                secondary = listOf(NeonBlue, NeonPurple),
                accent = listOf(NeonPink, NeonPurple),
                surface = listOf(CardDark.copy(alpha = 0.8f), CardDark)
            ),
            animatedElevation = ElevationValues(
                default = 6f,
                pressed = 4f,
                card = 8f,
                dialog = 24f
            )
        )
    } else {
        AnimatedTheme(
            gradients = Gradients(
                primary = listOf(GreenPrimary, NeonBlue.copy(alpha = 0.7f)),
                secondary = listOf(AccentColor, GreenPrimary),
                accent = listOf(NeonPink.copy(alpha = 0.7f), NeonPurple.copy(alpha = 0.7f)),
                surface = listOf(CardLight.copy(alpha = 0.8f), CardLight)
            ),
            animatedElevation = ElevationValues(
                default = 4f,
                pressed = 2f,
                card = 6f,
                dialog = 20f
            )
        )
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Use a gradient-like color for the status bar
            window.statusBarColor = if (darkTheme) NeonGreen.toArgb() else GreenPrimary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    
    // Provide our custom theme values
    CompositionLocalProvider(
        LocalAnimatedTheme provides animatedTheme,
        LocalAnimationSpec provides AnimationSpec()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// Extension functions to easily access animated theme properties
val MaterialTheme.animatedTheme: AnimatedTheme
    @Composable
    get() = LocalAnimatedTheme.current

val MaterialTheme.animatedElevation: ElevationValues
    @Composable
    get() = LocalAnimatedTheme.current.animatedElevation

// Create a function to generate a gradient brush from the theme
@Composable
fun primaryGradient(isVertical: Boolean = false): Brush {
    val colors = MaterialTheme.animatedTheme.gradients.primary
    return if (isVertical) {
        Brush.verticalGradient(colors = colors)
    } else {
        Brush.horizontalGradient(colors = colors)
    }
}

@Composable
fun accentGradient(isVertical: Boolean = false): Brush {
    val colors = MaterialTheme.animatedTheme.gradients.accent
    return if (isVertical) {
        Brush.verticalGradient(colors = colors)
    } else {
        Brush.horizontalGradient(colors = colors)
    }
}

// Create an animated gradient brush for special effects
@Composable
fun animatedGradient(
    colors: List<Color> = MaterialTheme.animatedTheme.gradients.accent,
    durationMillis: Int = 3000
): Brush {
    val transition = rememberInfiniteTransition(label = "gradient")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient"
    )
    
    return Brush.linearGradient(
        colors = colors,
        start = androidx.compose.ui.geometry.Offset(translateAnim, 0f),
        end = androidx.compose.ui.geometry.Offset(translateAnim + 1000f, 1000f)
    )
}
