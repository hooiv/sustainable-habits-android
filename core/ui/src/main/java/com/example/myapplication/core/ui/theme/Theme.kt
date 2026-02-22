package com.example.myapplication.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.myapplication.core.data.util.ThemePreferenceManager

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

data class SpacingValues(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp
)

data class AnimatedTheme(
    val defaultAnimation: AnimationSpec = AnimationSpec(),
    val gradients: Gradients,
    val animatedElevation: ElevationValues = ElevationValues(),
    val spacing: SpacingValues = SpacingValues()
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
            primary = listOf(GreenPrimary, SoftTeal),
            secondary = listOf(AccentColor, GreenPrimary),
            accent = listOf(SoftPink, SoftPurple),
            surface = listOf(CardLight.copy(alpha = 0.8f), CardLight)
        ),
        spacing = SpacingValues()
    ) 
}

val LocalAnimationSpec = compositionLocalOf { AnimationSpec() }

// Elegant dark color scheme
private val DarkColorScheme = darkColorScheme(
    primary = SoftTeal,
    secondary = SoftSky,
    tertiary = SoftPurple,
    background = Color(0xFF121212),
    surface = CardDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    primaryContainer = GreenDark,
    onPrimaryContainer = TextOnGreen,
    surfaceVariant = ElevatedSurfaceDark,
    error = Color(0xFFFF5252) // Bright red for errors
)

// Elegant light color scheme
private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    secondary = SoftSky,
    tertiary = SoftPurple,
    background = Color(0xFFF8F8F8),
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
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    // Observe the dark mode preference from ThemePreferenceManager
    // Use isSystemInDarkTheme() as the initial value before the preference is loaded
    val darkTheme by remember(context) { ThemePreferenceManager.isDarkModeEnabled(context) }
        .collectAsState(initial = isSystemInDarkTheme())

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    // Define animated theme properties based on dark/light theme
    val animatedTheme = if (darkTheme) {
        AnimatedTheme(
            gradients = Gradients(
                primary = listOf(SoftTeal, SoftSky),
                secondary = listOf(SoftSky, SoftPurple),
                accent = listOf(SoftPink, SoftPurple),
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
                primary = listOf(GreenPrimary, SoftSky),
                secondary = listOf(AccentColor, GreenPrimary),
                accent = listOf(SoftPink, SoftPurple),
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
            window.statusBarColor = if (darkTheme) SoftTeal.toArgb() else GreenPrimary.toArgb()
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
            shapes = AppShapes,
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

val MaterialTheme.spacing: SpacingValues
    @Composable
    get() = LocalAnimatedTheme.current.spacing

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
