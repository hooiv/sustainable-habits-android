package com.example.myapplication.core.ui.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
// Removed blur import as we'll implement our own
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

// Using the custom blur implementation from BlurUtils.kt

/**
 * Creates a morphing blob shape that animates organically
 */
@Composable
fun MorphingBlob(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    pointCount: Int = 8,
    animationDuration: Int = 10000,
    minRadius: Float = 0.6f,
    maxRadius: Float = 1.0f,
    blurRadius: Dp = 5.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blobMorph")

    // Create animated values for each point
    val animatedValues = List(pointCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = minRadius + Random.nextFloat() * (maxRadius - minRadius),
            targetValue = minRadius + Random.nextFloat() * (maxRadius - minRadius),
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = animationDuration,
                    easing = AnimeEasing.EaseInOutSine,
                    delayMillis = (index * 100) % 1000
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "point$index"
        )
    }

    Canvas(
        modifier = modifier
            .softShadowEffect(blurRadius)
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.width.coerceAtMost(size.height) / 2

        val path = Path()

        // Create a closed path with animated points
        for (i in 0 until pointCount) {
            val angle = (i * 360f / pointCount) * (PI / 180f)
            val pointRadius = radius * animatedValues[i].value
            val x = centerX + pointRadius * cos(angle).toFloat()
            val y = centerY + pointRadius * sin(angle).toFloat()

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        path.close()

        // Draw the blob with gradient fill
        drawPath(
            path = path,
            brush = Brush.radialGradient(
                colors = listOf(
                    color,
                    color.copy(alpha = 0.7f)
                ),
                center = Offset(centerX, centerY),
                radius = radius
            )
        )
    }
}

/**
 * Creates a glowing text effect with animated particles
 */
@Composable
fun GlowingText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    glowColor: Color = color.copy(alpha = 0.5f),
    glowRadius: Dp = 10.dp,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    particlesEnabled: Boolean = true
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Glow layer
        Text(
            text = text,
            color = glowColor,
            style = style,
            modifier = Modifier.textGlowEffect(glowRadius, glowColor)
        )

        // Main text
        Text(
            text = text,
            color = color,
            style = style
        )

        // Particle effect around text
        if (particlesEnabled) {
            ParticleSystem(
                modifier = Modifier.matchParentSize(),
                particleCount = 20,
                particleSize = 3.dp,
                particleColor = color.copy(alpha = 0.6f),
                maxSpeed = 0.5f,
                particleShape = ParticleShape.CIRCLE,
                particleEffect = ParticleEffect.FLOAT,
                glowEffect = true
            )
        }
    }
}

/** Shape of individual particles rendered by [ParticleSystem]. */
enum class ParticleShape { CIRCLE, SQUARE, TRIANGLE, STAR }

/** Motion pattern applied to particles by [ParticleSystem]. */
enum class ParticleEffect { FLOAT, WAVE, VORTEX, PULSE }

/**
 * Renders an ambient particle background. Delegates rendering to [ParticleWave].
 * [particleShape], [particleEffect], [glowEffect], and [colorVariation] are accepted
 * for call-site compatibility; the current implementation uses a wave pattern
 * that visually covers the FLOAT/WAVE use-cases.
 */
@Composable
fun ParticleSystem(
    modifier: Modifier = Modifier,
    particleCount: Int = 30,
    particleColor: Color = Color.White.copy(alpha = 0.3f),
    particleShape: ParticleShape = ParticleShape.CIRCLE,
    particleEffect: ParticleEffect = ParticleEffect.FLOAT,
    maxSpeed: Float = 0.5f,
    particleSize: Dp = 4.dp,
    glowEffect: Boolean = false,
    colorVariation: Boolean = false
) {
    ParticleWave(
        modifier = modifier,
        particleColor = particleColor,
        particleCount = particleCount,
        particleSizeRange = (particleSize.value * 0.5f)..(particleSize.value * 2f),
        speed = maxSpeed
    )
}

/**
 * Lightweight 3D-scene container that wraps [content] with a subtle graphicsLayer
 * rotation. Replaces the deleted WebView-based ThreeJS integration.
 * [enableParallax] and [enableShadows] are accepted for call-site compatibility
 * but are not implemented in this pure-Compose stub.
 */
@Composable
fun ThreeJSScene(
    modifier: Modifier = Modifier,
    rotationEnabled: Boolean = true,
    initialRotationY: Float = 0f,
    cameraDistance: Float = 12f,
    enableParallax: Boolean = false,
    enableShadows: Boolean = false,
    backgroundColor: Color = Color.Transparent,
    content: @Composable (Modifier) -> Unit
) {
    val density = LocalDensity.current
    Box(modifier = modifier.background(backgroundColor)) {
        content(
            Modifier.graphicsLayer {
                this.rotationY = if (rotationEnabled) initialRotationY else 0f
                this.cameraDistance = cameraDistance * density.density
            }
        )
    }
}

/**
 * One-shot particle burst effect. Rendered as an animated [ParticleWave] that
 * fades out after [duration] ms. When [repeat] is false the animation stops after
 * one cycle.
 */
@Composable
fun ParticleExplosion(
    modifier: Modifier = Modifier,
    particleColor: Color = Color.White.copy(alpha = 0.4f),
    particleCount: Int = 20,
    explosionRadius: Float = 150f,
    duration: Int = 1500,
    repeat: Boolean = false
) {
    var visible by remember { mutableStateOf(true) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(duration),
        label = "explosionFade"
    )
    LaunchedEffect(repeat) {
        if (!repeat) {
            delay(duration.toLong())
            visible = false
        } else {
            visible = true
        }
    }
    if (alpha > 0f) {
        ParticleWave(
            modifier = modifier.alpha(alpha),
            particleColor = particleColor,
            particleCount = particleCount,
            waveHeight = explosionRadius / 5f
        )
    }
}
