package com.example.myapplication.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// import androidx.compose.ui.draw.blur - replaced with custom implementation
import com.example.myapplication.core.ui.animation.softShadowEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.myapplication.core.ui.animation.animatedGradient
import kotlin.math.PI
import kotlin.math.sin

/**
 * Creates a parallax background effect similar to those used in anime.js demos
 * with layered elements that move at different speeds.
 */
@Composable
fun EnhancedParallaxBackground(
    modifier: Modifier = Modifier,
    backgroundColors: List<Color>,
    particleColor: Color = Color.White.copy(alpha = 0.5f),
    content: @Composable () -> Unit
) {
    // Animation for the floating particles
    val infiniteTransition = rememberInfiniteTransition(label = "parallax")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offsetX"
    )

    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offsetY"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Animated gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(animatedGradient(colors = backgroundColors, animationDuration = 10000))
        )
        // Add overlay gradient for contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.15f))
                    )
                )
        )

        // First layer of floating particles (slower)
        ParticleLayer(
            count = 30,
            maxSize = 5.dp,
            color = particleColor,
            offsetX = offsetX * 0.3f,
            offsetY = offsetY * 0.2f
        )

        // Second layer of floating particles (medium speed)
        ParticleLayer(
            count = 20,
            maxSize = 8.dp,
            color = particleColor.copy(alpha = 0.3f),
            offsetX = offsetX * 0.5f,
            offsetY = offsetY * 0.4f
        )

        // Third layer of floating particles (faster)
        ParticleLayer(
            count = 10,
            maxSize = 12.dp,
            color = particleColor.copy(alpha = 0.2f),
            offsetX = offsetX * 0.8f,
            offsetY = offsetY * 0.6f
        )

        // Content
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

/**
 * A layer of animated particles for the parallax effect
 */
@Composable
private fun ParticleLayer(
    count: Int,
    maxSize: Dp,
    color: Color,
    offsetX: Float,
    offsetY: Float
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val screenHeight = LocalConfiguration.current.screenHeightDp

    Box(modifier = Modifier.fillMaxSize()) {
        repeat(count) { index ->
            val particleSize = (3 + (index % 5)) * maxSize.value / 10
            val x = ((index * 37) % screenWidth)
            val y = ((index * 53) % screenHeight)

            // Different speeds based on particle size
            val particleOffsetX = offsetX * (0.5f + (particleSize / maxSize.value) * 0.5f)
            val particleOffsetY = offsetY * (0.5f + (particleSize / maxSize.value) * 0.5f)

            Box(
                modifier = Modifier
                    .size(particleSize.dp)
                    .graphicsLayer {
                        translationX = (particleOffsetX + x) % screenWidth
                        translationY = (particleOffsetY + y) % screenHeight
                        alpha = 0.3f + (particleSize / maxSize.value) * 0.7f
                    }
                    .softShadowEffect(radius = (particleSize / 2).dp) // Add blur for a soft look
                    .background(
                        color = color,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            ) {}
        }
    }
}

/**
 * Creates a 3D-like tilting effect based on device motion or finger position
 */
@Composable
fun TiltEffect(
    modifier: Modifier = Modifier,
    maxTiltDegrees: Float = 10f,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val screenWidth = LocalConfiguration.current.screenWidthDp.toFloat()
    val screenHeight = LocalConfiguration.current.screenHeightDp.toFloat()

    // Animate the tilt values for smooth transitions
    val animatedRotationX by animateFloatAsState(
        targetValue = (offsetY / screenHeight * 2 - 1) * maxTiltDegrees,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "animatedRotationX"
    )

    val animatedRotationY by animateFloatAsState(
        targetValue = -(offsetX / screenWidth * 2 - 1) * maxTiltDegrees,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "animatedRotationY"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event: PointerEvent = awaitPointerEvent()
                        // Get position of the first pointer
                        val position = event.changes.firstOrNull()?.position
                        if (position != null) {
                            offsetX = position.x
                            offsetY = position.y
                        }

                        // Make sure to consume the event
                        event.changes.forEach { it.consume() }
                    }
                }
            }
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    rotationX = animatedRotationX
                    rotationY = animatedRotationY
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                }
        ) {
            content()
        }
    }
}

/**
 * Creates a floating 3D card with depth perception and tilt effect
 */
@Composable
fun FloatingCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")

    // Create subtle floating animation
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutBack),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    // Create subtle rotation animation
    val animatedRotationZ by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutBack),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotationZ"
    )
    // Add scale animation for more depth
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutBack),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                translationY = offsetY
                rotationZ = animatedRotationZ
                this.shadowElevation = elevation.toPx()
                this.scaleX = scale
                this.scaleY = scale
            }
            .background(
                color = Color.White.copy(alpha = 0.05f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
            )
    ) {
        content()
    }
}

// Animation easing
val EaseInOutBack = Easing { fraction ->
    val s = 1.70158f
    val fractionTimesTwo = fraction * 2
    return@Easing if (fractionTimesTwo < 1) {
        0.5f * (fractionTimesTwo * fractionTimesTwo * ((s + 1) * fractionTimesTwo - s))
    } else {
        val adjustedFraction = fractionTimesTwo - 2
        0.5f * (adjustedFraction * adjustedFraction * ((s + 1) * adjustedFraction + s) + 2)
    }
}

val EaseInOutQuad = Easing { fraction ->
    val fractionTimesTwo = fraction * 2
    return@Easing if (fractionTimesTwo < 1) {
        0.5f * fractionTimesTwo * fractionTimesTwo
    } else {
        val adjustedFraction = fractionTimesTwo - 1
        -0.5f * (adjustedFraction * (adjustedFraction - 2) - 1)
    }
}
