package com.hooiv.habitflow.features.habits

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.hooiv.habitflow.core.ui.theme.AnimatedTheme
import com.hooiv.habitflow.core.ui.theme.AnimationSpec
import com.hooiv.habitflow.core.ui.theme.AppShapes
import com.hooiv.habitflow.core.ui.theme.ElevationValues
import com.hooiv.habitflow.core.ui.theme.Gradients
import com.hooiv.habitflow.core.ui.theme.LocalAnimatedTheme
import com.hooiv.habitflow.core.ui.theme.LocalAnimationSpec
import com.hooiv.habitflow.core.ui.theme.Typography

// Copied from Theme.kt (simplified for test)
val GreenPrimary = Color(0xFF00E676)
val GreenDark = Color(0xFF00B248)
val GreenLight = Color(0xFF66FFA6)
val TextOnGreen = Color(0xFF000000)

val NeonGreen = Color(0xFF39FF14)
val NeonBlue = Color(0xFF00BFFF)
val NeonPink = Color(0xFFFF00FF)
val NeonPurple = Color(0xFF9D00FF)
val AccentColor = Color(0xFFFF4081)

val CardLight = Color(0xFFFFFFFF)
val CardDark = Color(0xFF1E1E1E)
val ElevatedSurfaceLight = Color(0xFFF5F5F5)
val ElevatedSurfaceDark = Color(0xFF2D2D2D)

private val TestDarkColorScheme = darkColorScheme(
    primary = NeonGreen, secondary = NeonBlue, tertiary = NeonPurple,
    background = Color(0xFF121212), surface = CardDark,
    onPrimary = Color.Black, onSecondary = Color.Black, onTertiary = Color.Black,
    onBackground = Color.White, onSurface = Color.White,
    primaryContainer = GreenDark, onPrimaryContainer = TextOnGreen,
    surfaceVariant = ElevatedSurfaceDark, error = Color(0xFFFF5252)
)

private val TestLightColorScheme = lightColorScheme(
    primary = GreenPrimary, secondary = NeonBlue.copy(alpha = 0.8f),
    tertiary = NeonPurple.copy(alpha = 0.8f), background = Color(0xFFF8F8F8),
    surface = CardLight, onPrimary = TextOnGreen, onSecondary = Color.Black,
    onTertiary = Color.White, onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F), primaryContainer = GreenLight,
    onPrimaryContainer = Color.Black, surfaceVariant = ElevatedSurfaceLight,
    error = Color(0xFFB00020)
)

@Composable
fun TestTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) TestDarkColorScheme else TestLightColorScheme

    val animatedTheme = if (darkTheme) {
        AnimatedTheme(
            gradients = Gradients(
                primary = listOf(NeonGreen, NeonBlue),
                secondary = listOf(NeonBlue, NeonPurple),
                accent = listOf(NeonPink, NeonPurple),
                surface = listOf(CardDark.copy(alpha = 0.8f), CardDark)
            ),
            animatedElevation = ElevationValues(6f, 4f, 8f, 24f)
        )
    } else {
        AnimatedTheme(
            gradients = Gradients(
                primary = listOf(GreenPrimary, NeonBlue.copy(alpha = 0.7f)),
                secondary = listOf(AccentColor, GreenPrimary),
                accent = listOf(NeonPink.copy(alpha = 0.7f), NeonPurple.copy(alpha = 0.7f)),
                surface = listOf(CardLight.copy(alpha = 0.8f), CardLight)
            ),
            animatedElevation = ElevationValues(4f, 2f, 6f, 20f)
        )
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
