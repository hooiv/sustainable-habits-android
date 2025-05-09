package com.example.myapplication.ui.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

/**
 * Creates a particle wave effect similar to anime.js demos
 */
@Composable
fun ParticleWave(
    modifier: Modifier = Modifier,
    particleColor: Color = Color.White,
    particleCount: Int = 100,
    waveHeight: Float = 100f,
    waveWidth: Float = 1000f,
    speed: Float = 0.5f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (2000 / speed).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerY = canvasHeight / 2

        for (i in 0 until particleCount) {
            val x = (i.toFloat() / particleCount) * canvasWidth
            val wavePhase = phase + (x / waveWidth) * 2 * PI.toFloat()
            val y = centerY + sin(wavePhase) * waveHeight

            // Vary particle size based on y position
            val particleSize = 2f + 2f * (1 + sin(wavePhase)) / 2

            drawCircle(
                color = particleColor.copy(alpha = 0.7f),
                radius = particleSize,
                center = Offset(x, y)
            )
        }
    }
}

/**
 * Creates a particle explosion effect
 */
@Composable
fun ParticleExplosion(
    modifier: Modifier = Modifier,
    particleCount: Int = 50,
    particleColor: Color = Color.White,
    explosionRadius: Float = 200f,
    duration: Int = 2000,
    repeat: Boolean = true,
    onAnimationEnd: () -> Unit = {}
) {
    val particles = remember {
        List(particleCount) {
            val angle = Random.nextFloat() * 2 * PI.toFloat()
            val speed = 0.5f + Random.nextFloat() * 0.5f
            ExplosionParticle(
                angle = angle,
                speed = speed,
                size = 2f + Random.nextFloat() * 4f,
                distance = 0f
            )
        }
    }

    var isExploding by remember { mutableStateOf(true) }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(isExploding) {
        if (isExploding) {
            animationProgress.snapTo(0f)
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = duration,
                    easing = LinearEasing
                )
            )

            if (repeat) {
                isExploding = false
                delay(500) // Wait before repeating
                isExploding = true
            } else {
                onAnimationEnd()
            }
        }
    }

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2
        val centerY = canvasHeight / 2

        particles.forEach { particle ->
            val distance = explosionRadius * animationProgress.value * particle.speed
            val x = centerX + cos(particle.angle) * distance
            val y = centerY + sin(particle.angle) * distance

            // Fade out as particles move away
            val alpha = 1f - animationProgress.value

            drawCircle(
                color = particleColor.copy(alpha = alpha),
                radius = particle.size * (1f - animationProgress.value * 0.5f),
                center = Offset(x, y)
            )
        }
    }
}

/**
 * Data class for explosion particles
 */
private data class ExplosionParticle(
    val angle: Float,
    val speed: Float,
    val size: Float,
    var distance: Float
)

/**
 * Creates a particle text effect where text dissolves into particles
 */
@Composable
fun ParticleText(
    modifier: Modifier = Modifier,
    particleColor: Color = Color.White,
    particleSize: Dp = 4.dp,
    particleDensity: Float = 0.5f, // 0.0 to 1.0
    animationDuration: Int = 2000,
    animationDelay: Int = 0,
    onAnimationEnd: () -> Unit = {}
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        animationPlayed = true
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = animationDuration,
                easing = AnimeEasing.EaseInOutQuad
            )
        )
        onAnimationEnd()
    }

    Box(modifier = modifier) {
        // This would be where we'd render the actual text and particles
        // For a real implementation, we'd need to use a custom text layout
        // and generate particles based on the text shape

        // For this example, we'll just show a simple particle effect
        val particleSizePx = with(LocalDensity.current) { particleSize.toPx() }
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Create a grid of particles
            val gridSize = 20
            val spacing = canvasWidth / gridSize

            for (x in 0 until gridSize) {
                for (y in 0 until (canvasHeight / spacing).toInt()) {
                    // Only draw some particles based on density
                    if (Random.nextFloat() > particleDensity) continue

                    val posX = x * spacing + spacing / 2
                    val posY = y * spacing + spacing / 2

                    // Calculate particle movement
                    val angle = Random.nextFloat() * 2 * PI.toFloat()
                    val distance = 100f * animationProgress.value
                    val offsetX = cos(angle) * distance
                    val offsetY = sin(angle) * distance

                    // Fade out as animation progresses
                    val alpha = 1f - animationProgress.value

                    drawCircle(
                        color = particleColor.copy(alpha = alpha),
                        radius = particleSizePx * (1f - animationProgress.value * 0.5f),
                        center = Offset(posX + offsetX, posY + offsetY)
                    )
                }
            }
        }
    }
}

/**
 * Creates a particle trail effect that follows a path
 */
@Composable
fun ParticleTrail(
    modifier: Modifier = Modifier,
    particleColor: Color = Color.White,
    particleCount: Int = 20,
    particleSize: Dp = 4.dp,
    pathPoints: List<Offset>,
    duration: Int = 2000
) {
    val density = LocalDensity.current
    val particleSizePx = with(density) { particleSize.toPx() }

    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(pathPoints) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = duration,
                easing = LinearEasing
            )
        )
    }

    Canvas(modifier = modifier) {
        if (pathPoints.size < 2) return@Canvas

        // Calculate the total path length
        var totalLength = 0f
        for (i in 0 until pathPoints.size - 1) {
            totalLength += (pathPoints[i + 1] - pathPoints[i]).getDistance()
        }

        // Draw particles along the path
        for (i in 0 until particleCount) {
            val particleProgress = (i.toFloat() / particleCount + animationProgress.value) % 1f
            var currentLength = particleProgress * totalLength

            // Find the segment where this particle should be
            var segmentStart = 0
            var segmentLength = 0f

            while (segmentStart < pathPoints.size - 1) {
                val segmentDistance = (pathPoints[segmentStart + 1] - pathPoints[segmentStart]).getDistance()
                if (currentLength <= segmentDistance) {
                    // Found the segment
                    val segmentProgress = currentLength / segmentDistance
                    val particlePos = Offset(
                        pathPoints[segmentStart].x + (pathPoints[segmentStart + 1].x - pathPoints[segmentStart].x) * segmentProgress,
                        pathPoints[segmentStart].y + (pathPoints[segmentStart + 1].y - pathPoints[segmentStart].y) * segmentProgress
                    )

                    // Fade based on position in the trail
                    val alpha = 0.2f + 0.8f * (1f - i.toFloat() / particleCount)

                    drawCircle(
                        color = particleColor.copy(alpha = alpha),
                        radius = particleSizePx * (0.5f + 0.5f * (1f - i.toFloat() / particleCount)),
                        center = particlePos
                    )
                    break
                }

                currentLength -= segmentDistance
                segmentStart++
            }
        }
    }
}
