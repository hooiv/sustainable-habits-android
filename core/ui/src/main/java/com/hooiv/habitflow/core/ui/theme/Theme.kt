package com.hooiv.habitflow.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.hooiv.habitflow.core.data.util.ThemePreferenceManager

// ---------------------------------------------------------------------------
// Custom theme extension values
// ---------------------------------------------------------------------------

data class AnimationSpec(
    val duration: Int = 500,
    val easing: Easing = FastOutSlowInEasing
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

data class ElevationValues(
    val default: Float = 4f,
    val pressed: Float = 2f,
    val card: Float = 6f,
    val dialog: Float = 24f
)

data class AnimatedTheme(
    val defaultAnimation: AnimationSpec = AnimationSpec(),
    val gradients: Gradients,
    val animatedElevation: ElevationValues = ElevationValues(),
    val spacing: SpacingValues = SpacingValues()
)

val LocalAnimatedTheme = compositionLocalOf {
    AnimatedTheme(
        gradients = Gradients(
            primary = listOf(Indigo40, Teal40),
            secondary = listOf(Teal40, Indigo40),
            accent = listOf(Amber40, StreakMedium),
            surface = listOf(SurfaceLight.copy(alpha = 0.8f), SurfaceLight)
        )
    )
}

val LocalAnimationSpec = compositionLocalOf { AnimationSpec() }

// ---------------------------------------------------------------------------
// Material 3 color schemes — both built from the HabitFlow brand palette
// ---------------------------------------------------------------------------

private val LightColorScheme = lightColorScheme(
    primary              = Indigo40,
    onPrimary            = Color.White,
    primaryContainer     = Indigo90,
    onPrimaryContainer   = Indigo10,
    secondary            = Teal40,
    onSecondary          = Color.White,
    secondaryContainer   = Teal90,
    onSecondaryContainer = Teal10,
    tertiary             = Amber40,
    onTertiary           = Color.White,
    tertiaryContainer    = Amber90,
    onTertiaryContainer  = Amber10,
    error                = Color(0xFFB3261E),
    onError              = Color.White,
    errorContainer       = Color(0xFFF9DEDC),
    onErrorContainer     = Color(0xFF370B09),
    background           = BackgroundLight,
    onBackground         = Neutral10,
    surface              = SurfaceLight,
    onSurface            = Neutral10,
    surfaceVariant       = NeutralVar90,
    onSurfaceVariant     = NeutralVar30,
    outline              = NeutralVar50,
    outlineVariant       = NeutralVar80
)

private val DarkColorScheme = darkColorScheme(
    primary              = Indigo80,
    onPrimary            = Indigo20,
    primaryContainer     = Indigo30,
    onPrimaryContainer   = Indigo90,
    secondary            = Teal80,
    onSecondary          = Teal10,
    secondaryContainer   = Teal20,
    onSecondaryContainer = Teal90,
    tertiary             = Amber80,
    onTertiary           = Amber20,
    tertiaryContainer    = Amber30,
    onTertiaryContainer  = Amber90,
    error                = Color(0xFFF2B8B5),
    onError              = Color(0xFF601410),
    errorContainer       = Color(0xFF8C1D18),
    onErrorContainer     = Color(0xFFF9DEDC),
    background           = BackgroundDark,
    onBackground         = Neutral90,
    surface              = SurfaceDark,
    onSurface            = Neutral90,
    surfaceVariant       = Neutral20,
    onSurfaceVariant     = NeutralVar80,
    outline              = NeutralVar50,
    outlineVariant       = NeutralVar30
)

// ---------------------------------------------------------------------------
// Theme composable
// ---------------------------------------------------------------------------

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color deliberately off — it overrides the HabitFlow brand palette on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isDarkFromPrefs by remember(context) { ThemePreferenceManager.isDarkModeEnabled(context) }
        .collectAsState(initial = darkTheme)

    val resolvedDark = isDarkFromPrefs

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (resolvedDark) {
                androidx.compose.material3.dynamicDarkColorScheme(context)
            } else {
                androidx.compose.material3.dynamicLightColorScheme(context)
            }
        }
        resolvedDark -> DarkColorScheme
        else -> LightColorScheme
    }

    val animatedTheme = if (resolvedDark) {
        AnimatedTheme(
            gradients = Gradients(
                primary = listOf(Indigo80, Teal80),
                secondary = listOf(Teal80, Indigo80),
                accent = listOf(Amber80, StreakMedium),
                surface = listOf(SurfaceDark.copy(alpha = 0.8f), SurfaceDark)
            ),
            animatedElevation = ElevationValues(default = 6f, pressed = 4f, card = 8f)
        )
    } else {
        AnimatedTheme(
            gradients = Gradients(
                primary = listOf(Indigo40, Teal40),
                secondary = listOf(Teal40, Indigo40),
                accent = listOf(Amber40, StreakMedium),
                surface = listOf(SurfaceLight.copy(alpha = 0.8f), SurfaceLight)
            )
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Edge-to-edge: transparent status + nav bars; system handles the icon colours
            @Suppress("DEPRECATION")
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            @Suppress("DEPRECATION")
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !resolvedDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !resolvedDark
        }
    }

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

// ---------------------------------------------------------------------------
// Theme extension helpers
// ---------------------------------------------------------------------------

val MaterialTheme.animatedTheme: AnimatedTheme
    @Composable get() = LocalAnimatedTheme.current

val MaterialTheme.animatedElevation: ElevationValues
    @Composable get() = LocalAnimatedTheme.current.animatedElevation

val MaterialTheme.spacing: SpacingValues
    @Composable get() = LocalAnimatedTheme.current.spacing

@Composable
fun primaryGradient(isVertical: Boolean = false): Brush {
    val colors = MaterialTheme.animatedTheme.gradients.primary
    return if (isVertical) Brush.verticalGradient(colors) else Brush.horizontalGradient(colors)
}

@Composable
fun accentGradient(isVertical: Boolean = false): Brush {
    val colors = MaterialTheme.animatedTheme.gradients.accent
    return if (isVertical) Brush.verticalGradient(colors) else Brush.horizontalGradient(colors)
}

@Composable
fun animatedGradient(
    colors: List<Color> = MaterialTheme.animatedTheme.gradients.accent,
    durationMillis: Int = 3000
): Brush {
    val transition = rememberInfiniteTransition(label = "gradient")
    val offset by transition.animateFloat(
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
        start = Offset(offset, 0f),
        end = Offset(offset + 1000f, 1000f)
    )
}
