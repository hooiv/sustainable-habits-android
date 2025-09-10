package com.example.myapplication.ui.animation

import androidx.compose.animation.core.* // Keep for general animation utilities
import androidx.compose.animation.core.animateValue // Explicit import for animateValue
import androidx.compose.animation.core.AnimationVector4D // Explicit import
import androidx.compose.animation.core.TwoWayConverter // Explicit import
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
import androidx.compose.ui.graphics.toArgb // Import for toArgb
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.colorspace.ColorSpaces // Import for ColorSpaces
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
    magnitude: Float = 20f
): Modifier = composed {
    if (!enabled) return@composed this

    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = magnitude,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounceOffset"
    )

    this.offset(y = offsetY.dp)
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
    pulseMagnitude: Float = 0.1f
): Modifier = composed {
    if (!pulseEnabled) return@composed this

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f + pulseMagnitude,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    this.scale(scale)
}

/**
 * Anime.js-like entrance animation with configurable parameters
 */
fun Modifier.animeEntrance(
    visible: Boolean,
    initialOffsetY: Int = 100,
    initialAlpha: Float = 0f,
    initialScale: Float = 0.8f,
    delayMillis: Int = 0
): Modifier = composed {
    // TODO: Implement delayMillis if needed, AnimatedVisibility doesn't directly support a start delay for enter animation in this manner.
    // Consider using LaunchedEffect with delay for more complex scenarios or a custom animation approach.

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { initialOffsetY },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(
            initialAlpha = initialAlpha,
            animationSpec = tween(300)
        ) + androidx.compose.animation.scaleIn(
            initialScale = initialScale,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = fadeOut() + slideOutVertically()
    ) {
        // Content for AnimatedVisibility.
        // This block is part of AnimatedVisibility's content lambda.
        // The Modifier returned by animeEntrance applies to the AnimatedVisibility itself.
    }
    return@composed Modifier
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

// Custom TwoWayConverter for Color
val ColorToVectorConverter = TwoWayConverter<Color, AnimationVector4D>(
    convertToVector = { color ->
        val colorArgb = color.toArgb()
        AnimationVector4D(
            ((colorArgb shr 16) and 0xFF) / 255f, // Red
            ((colorArgb shr 8) and 0xFF) / 255f,  // Green
            (colorArgb and 0xFF) / 255f,          // Blue
            ((colorArgb shr 24) and 0xFF) / 255f  // Alpha
        )
    },
    convertFromVector = { vector ->
        Color(
            red = vector.v1,
            green = vector.v2,
            blue = vector.v3,
            alpha = vector.v4
        )
    }
)

/**
 * Creates an animated gradient with color animation
 */
@Composable
fun animatedGradient(
    colors: List<Color>,
    animationDuration: Int = 3000
): Brush {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient_transition")
    val safeColors = if (colors.size < 2) listOf(colors.firstOrNull() ?: Color.White, Color.White) else colors

    val colorStates = mutableListOf<State<Color>>()
    for (i in safeColors.indices) {
        colorStates.add(
            infiniteTransition.animateValue(
                initialValue = safeColors[i],
                targetValue = safeColors[(i + 1) % safeColors.size],
                typeConverter = ColorToVectorConverter, // Use custom converter
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = animationDuration, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "colorAnimation_$i"
            )
        )
    }

    val animatedColorValues = colorStates.map { it.value }
    return Brush.verticalGradient(animatedColorValues)
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
