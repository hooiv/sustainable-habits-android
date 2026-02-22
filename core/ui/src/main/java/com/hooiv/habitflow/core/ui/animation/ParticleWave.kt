package com.hooiv.habitflow.core.ui.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import android.content.Context
import android.os.PowerManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * A composable that renders a wave of animated particles.
 * The particles move in a wave-like pattern, creating a subtle background animation effect.
 *
 * @param modifier The modifier to be applied to the Canvas
 * @param particleColor The color of the particles
 * @param particleCount The number of particles to render
 * @param particleSizeRange The size range of the particles (min to max)
 * @param waveHeight The height of the wave
 * @param speed The speed of the animation (lower is slower)
 */
@Composable
fun ParticleWave(
    modifier: Modifier = Modifier,
    particleColor: Color = Color.White.copy(alpha = 0.3f),
    particleCount: Int = 30,
    particleSizeRange: ClosedFloatingPointRange<Float> = 2f..6f,
    waveHeight: Float = 30f,
    speed: Float = 0.5f
) {
    // Create a list of particles with random properties
    val particles = remember {
        List(particleCount) {
            WaveParticle(
                initialX = Random.nextFloat(),
                initialY = Random.nextFloat(),
                size = particleSizeRange.start + (particleSizeRange.endInclusive - particleSizeRange.start) * Random.nextFloat(),
                phase = (Random.nextFloat() * 2 * PI).toFloat()
            )
        }
    }

    val context = LocalContext.current
    val powerManager = remember(context) { context.getSystemService(Context.POWER_SERVICE) as PowerManager }
    val isPowerSaveMode = powerManager.isPowerSaveMode

    // Animation value for the wave movement
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPowerSaveMode) 0f else 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (5000 / speed).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveTime"
    )

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        particles.forEach { particle ->
            // Calculate the position based on the wave equation
            val x = particle.initialX * canvasWidth
            val waveOffset = sin(time + particle.phase) * waveHeight
            val y = particle.initialY * canvasHeight + waveOffset

            // Draw the particle
            drawParticle(
                center = Offset(x, y),
                radius = particle.size,
                color = particleColor
            )
        }
    }
}

/**
 * Helper function to draw a particle
 */
private fun DrawScope.drawParticle(
    center: Offset,
    radius: Float,
    color: Color
) {
    drawCircle(
        color = color,
        radius = radius,
        center = center
    )
}

/**
 * Data class representing a particle in the wave
 */
private data class WaveParticle(
    val initialX: Float,
    val initialY: Float,
    val size: Float,
    val phase: Float
)
