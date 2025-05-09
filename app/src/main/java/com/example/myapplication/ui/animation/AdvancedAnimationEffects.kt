package com.example.myapplication.ui.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
 * Creates a liquid-like button with ripple effects
 */
@Composable
fun LiquidButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    rippleColor: Color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
    height: Dp = 56.dp,
    width: Dp = 200.dp
) {
    var isPressed by remember { mutableStateOf(false) }
    var rippleCenter by remember { mutableStateOf(Offset.Zero) }
    var rippleRadius by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    // Button animations
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )

    // Blob animation
    val infiniteTransition = rememberInfiniteTransition(label = "buttonBlob")
    val blobPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blobPhase"
    )

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .scale(scale)
            .clip(RoundedCornerShape(height / 2))
            .background(color)
            .drawBehind {
                // Draw blob edge effect
                val path = Path()
                val amplitude = size.height * 0.05f * (if (isPressed) 0.5f else 1f)
                val frequency = 15f

                path.moveTo(0f, 0f)
                path.lineTo(0f, size.height)

                // Bottom wavy edge
                for (x in 0..size.width.toInt() step 5) {
                    val xFloat = x.toFloat()
                    val yOffset = amplitude * sin(frequency * xFloat / size.width + blobPhase)
                    path.lineTo(xFloat, size.height + yOffset)
                }

                path.lineTo(size.width, size.height)
                path.lineTo(size.width, 0f)
                path.close()

                drawPath(
                    path = path,
                    color = color.copy(alpha = 0.7f)
                )

                // Draw ripple effect when pressed
                if (rippleRadius > 0) {
                    drawCircle(
                        color = rippleColor,
                        radius = rippleRadius,
                        center = rippleCenter,
                        blendMode = BlendMode.SrcAtop
                    )
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        isPressed = true
                        rippleCenter = offset
                        rippleRadius = 0f

                        coroutineScope.launch {
                            // Animate ripple
                            val startTime = System.currentTimeMillis()
                            val duration = 500
                            val maxRadius = max(size.width, size.height) * 1.5f

                            while (System.currentTimeMillis() - startTime < duration) {
                                val progress = (System.currentTimeMillis() - startTime) / duration.toFloat()
                                rippleRadius = progress * maxRadius
                                delay(16) // ~60fps
                            }

                            rippleRadius = 0f
                        }

                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
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
