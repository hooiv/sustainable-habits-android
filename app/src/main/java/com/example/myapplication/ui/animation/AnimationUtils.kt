package com.example.myapplication.ui.animation

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Creates a 3D card effect with rotation and elevation
 */
fun Modifier.threeDCard(
    rotationX: Float = 0f,
    rotationY: Float = 0f,
    rotationZ: Float = 0f,
    shadowElevation: Float = 8f
) = this.then(
    graphicsLayer {
        this.rotationX = rotationX
        this.rotationY = rotationY
        this.rotationZ = rotationZ
        this.shadowElevation = shadowElevation
        this.transformOrigin = TransformOrigin(0.5f, 0.5f)
        this.cameraDistance = 12f * density
    }
)

/**
 * Creates a flip card effect around the Y axis
 */
fun Modifier.flipCard(
    rotationY: Float = 0f,
    cameraDistance: Float = 8f
) = this.then(
    graphicsLayer {
        this.rotationY = rotationY
        this.cameraDistance = cameraDistance * density
    }
)

/**
 * Creates a bouncing animation effect similar to anime.js
 */
fun Modifier.loadingBounceEffect(
    enabled: Boolean = true,
    amplitude: Float = 20f,
    frequency: Float = 1.5f
): Modifier = composed {
    if (!enabled) return@composed this

    var animatedOffset by remember { mutableStateOf(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val offsetAnimation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (1000 / frequency).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bounce"
    )

    LaunchedEffect(offsetAnimation.value) {
        // Create a bouncing sine wave effect
        animatedOffset = sin(offsetAnimation.value * 2 * PI.toFloat()) * amplitude
    }

    this.offset { IntOffset(0, animatedOffset.roundToInt()) }
}

/**
 * Creates a floating effect similar to anime.js
 */
fun Modifier.floatingEffect(
    enabled: Boolean = true,
    amplitude: Float = 5f,
    frequency: Float = 0.5f
): Modifier = composed {
    if (!enabled) return@composed this

    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -amplitude,
        targetValue = amplitude,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (1000 / frequency).toInt(),
                easing = EaseInOutQuad
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    this.offset(y = offsetY.dp)
}

/**
 * Creates a pulsing animation effect
 */
fun Modifier.pulseEffect(
    pulseEnabled: Boolean = true,
    pulseMagnitude: Float = 0.1f,
    pulseFrequency: Float = 1f
): Modifier = composed {
    if (!pulseEnabled) return@composed this

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f + pulseMagnitude,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (1000 / pulseFrequency).toInt(),
                easing = EaseInOutQuad
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    this.scale(scale)
}

/**
 * Anime.js-like entrance animation with configurable parameters
 */
fun Modifier.animeEntrance(
    visible: Boolean,
    initialOffsetX: Int = 0,
    initialOffsetY: Int = 0,
    initialScale: Float = 1f,
    initialAlpha: Float = 1f,
    delayMillis: Int = 0,
    durationMillis: Int = 500
): Modifier = composed {
    var animatedOffset by remember { mutableStateOf(Pair(initialOffsetX, initialOffsetY)) }
    var animatedScale by remember { mutableStateOf(initialScale) }
    var animatedAlpha by remember { mutableStateOf(initialAlpha) }

    LaunchedEffect(visible) {
        if (delayMillis > 0) {
            delay(delayMillis.toLong())
        }

        if (visible) {
            animate(
                initialValue = initialOffsetX.toFloat(),
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = FastOutSlowInEasing
                )
            ) { value, _ ->
                animatedOffset = Pair(value.toInt(), animatedOffset.second)
            }

            animate(
                initialValue = initialOffsetY.toFloat(),
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = FastOutSlowInEasing
                )
            ) { value, _ ->
                animatedOffset = Pair(animatedOffset.first, value.toInt())
            }

            animate(
                initialValue = initialScale,
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = FastOutSlowInEasing
                )
            ) { value, _ ->
                animatedScale = value
            }

            animate(
                initialValue = initialAlpha,
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = FastOutSlowInEasing
                )
            ) { value, _ ->
                animatedAlpha = value
            }
        } else {
            animatedOffset = Pair(initialOffsetX, initialOffsetY)
            animatedScale = initialScale
            animatedAlpha = initialAlpha
        }
    }

    this
        .graphicsLayer {
            translationX = animatedOffset.first.toFloat()
            translationY = animatedOffset.second.toFloat()
            scaleX = animatedScale
            scaleY = animatedScale
            alpha = animatedAlpha
        }
}

/**
 * Shimmer effect for loading states
 */
fun Modifier.shimmerEffect(
    enabled: Boolean = true
): Modifier = composed {
    // Skip the CompositingStrategy for compatibility
    if (enabled) {
        this.graphicsLayer {
            // Using a simpler approach instead of CompositingStrategy
            alpha = 0.99f
        }
    } else {
        this
    }
}

// Animation utility functions

/**
 * Calculate a delay for staggered animations
 * @param index The index of the item
 * @param baseDelay The base delay between items
 * @param maxDelay Maximum delay cap
 */
fun staggeredDelay(index: Int, baseDelay: Int, maxDelay: Int): Int {
    return (index * baseDelay).coerceAtMost(maxDelay)
}

// Custom Easing functions
val EaseInOutQuad = Easing { fraction ->
    val fractionTimesTwo = fraction * 2
    return@Easing if (fractionTimesTwo < 1) {
        0.5f * fractionTimesTwo * fractionTimesTwo
    } else {
        val adjustedFraction = fractionTimesTwo - 1
        -0.5f * (adjustedFraction * (adjustedFraction - 2) - 1)
    }
}

val EaseInOutExpo = Easing { fraction ->
    when {
        fraction == 0f -> 0f
        fraction == 1f -> 1f
        fraction < 0.5f -> 0.5f * (2f.pow(20f * fraction - 10f))
        else -> 0.5f * (2f - (2f.pow(-20f * fraction + 10f)))
    }
}

val EaseOutElastic = Easing { fraction ->
    if (fraction == 0f) {
        0f
    } else if (fraction == 1f) {
        1f
    } else {
        val c4 = (2 * PI) / 3
        (2.0.pow(-10.0 * fraction) * sin((fraction * 10 - 0.75) * c4) + 1).toFloat()
    }
}

// AnimatedVisibility with standard animations
@Composable
fun AnimatedVisibilityWithSlide(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enter: androidx.compose.animation.EnterTransition = slideInHorizontally() + expandVertically() + fadeIn(),
    exit: androidx.compose.animation.ExitTransition = slideOutHorizontally() + shrinkVertically() + fadeOut(),
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = enter,
        exit = exit
    ) {
        content()
    }
}

/**
 * Creates an animated gradient brush with moving colors
 */
@Composable
fun animatedGradient(
    colors: List<Color>,
    animationDuration: Int = 3000
): Brush {
    val infiniteTransition = rememberInfiniteTransition(label = "gradientTransition")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradientAnimation"
    )
    
    return Brush.linearGradient(
        colors = colors,
        start = Offset(offset - 1000f, offset - 1000f),
        end = Offset(offset, offset)
    )
}