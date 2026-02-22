package com.hooiv.habitflow.features.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hooiv.habitflow.core.ui.R
import com.hooiv.habitflow.core.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

/**
 * Splash screen shown while the app initialises.
 *
 * Design:
 *  - Full-screen brand gradient (indigo → teal)
 *  - HabitFlow logo icon scales in with a spring bounce
 *  - App name slides up + fades in
 *  - Tagline fades in below
 *  - Three pulsing dots at the bottom indicate loading
 */
@Composable
fun SplashScreen() {
    // --- Animation targets ---
    val logoScale = remember { Animatable(0.4f) }
    val logoAlpha = remember { Animatable(0f) }
    val nameTranslationY = remember { Animatable(30f) }
    val nameAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    val dotsAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Logo springs in
        logoAlpha.animateTo(1f, tween(300))
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
        // App name slides up
        delay(80)
        nameAlpha.animateTo(1f, tween(350, easing = FastOutSlowInEasing))
        nameTranslationY.animateTo(0f, tween(350, easing = FastOutSlowInEasing))
        // Tagline fades in
        delay(100)
        taglineAlpha.animateTo(1f, tween(400))
        // Loading dots appear
        delay(150)
        dotsAlpha.animateTo(1f, tween(300))
    }

    // Pulsing dot animation (staggered)
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dot1Scale by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(600), androidx.compose.animation.core.RepeatMode.Reverse),
        label = "dot1"
    )
    val dot2Scale by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 150), androidx.compose.animation.core.RepeatMode.Reverse),
        label = "dot2"
    )
    val dot3Scale by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 300), androidx.compose.animation.core.RepeatMode.Reverse),
        label = "dot3"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2E2B6A), // deep indigo
                        Color(0xFF1A4B6E), // midnight blue
                        Color(0xFF006B5E)  // deep teal
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Logo icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                        alpha = logoAlpha.value
                    }
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = "HabitFlow logo",
                    modifier = Modifier.size(92.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App name
            Text(
                text = "HabitFlow",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White,
                modifier = Modifier.graphicsLayer {
                    alpha = nameAlpha.value
                    translationY = nameTranslationY.value
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Build habits that last",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.72f),
                modifier = Modifier.alpha(taglineAlpha.value)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Loading dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 48.dp)
                    .alpha(dotsAlpha.value)
            ) {
                listOf(dot1Scale, dot2Scale, dot3Scale).forEach { dotScale ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(dotScale)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.7f))
                    )
                }
            }
        }
    }
}

@Preview(name = "Splash — Phone", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SplashScreenPreview() {
    MyApplicationTheme {
        SplashScreen()
    }
}
