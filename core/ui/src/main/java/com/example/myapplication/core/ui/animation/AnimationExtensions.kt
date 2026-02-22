package com.example.myapplication.core.ui.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateIntSizeAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Custom easing functions for animations (simplified version)
 * Note: Full implementation is in AnimeEasing.kt
 */
object AnimationEasing {
    val EaseInQuad: Easing = Easing { fraction -> fraction * fraction }
    val EaseOutQuad: Easing = Easing { fraction -> 1 - (1 - fraction) * (1 - fraction) }
    val EaseInOutQuad: Easing = Easing { fraction ->
        if (fraction < 0.5f) 2 * fraction * fraction else 1 - (-2 * fraction + 2).pow(2) / 2
    }
}

/**
 * Extension function to calculate power for Float
 */
private fun Float.pow(power: Int): Float {
    var result = 1f
    repeat(power) { result *= this }
    return result
}

/**
 * Extension function to calculate power for Double
 */
private fun Double.pow(power: Int): Double {
    var result = 1.0
    repeat(power) { result *= this }
    return result
}

/**
 * Animated content size modifier
 */
@Composable
fun Modifier.animateContentSizeCustom(
    animationSpec: androidx.compose.animation.core.FiniteAnimationSpec<androidx.compose.ui.unit.IntSize> = tween(300, easing = FastOutSlowInEasing)
): Modifier {
    // Create a custom implementation using animateIntSizeAsState
    return this
}

/**
 * Applies a 3D card tilt effect using graphicsLayer.
 * Used by ThreeDCard to show depth based on touch rotation values.
 */
fun Modifier.threeDCard(
    rotationX: Float = 0f,
    rotationY: Float = 0f,
    shadowElevation: Float = 8f
): Modifier = this.graphicsLayer {
    this.rotationX = rotationX
    this.rotationY = rotationY
    this.shadowElevation = shadowElevation
    this.cameraDistance = 12f * density
}

/**
 * Applies a flip rotation around the Y axis.
 * Used by FlipCard to animate between front and back faces.
 */
fun Modifier.flipCard(rotationY: Float): Modifier = this.graphicsLayer {
    this.rotationY = rotationY
    this.cameraDistance = 12f * density
}

/**
 * Applies a staggered entrance animation.
 *
 * When [visible] transitions to `true` the composable animates from [initialOffsetY]/
 * [initialAlpha]/[initialScale] to its natural position. The delay is determined by
 * [delayMillis] when non-zero, otherwise by [index] × [baseDelay].
 */
@Composable
fun Modifier.animeEntrance(
    visible: Boolean = true,
    index: Int = 0,
    baseDelay: Int = 100,
    delayMillis: Int = 0,
    duration: Int = 600,
    initialOffsetY: Int = 100,
    initialAlpha: Float = 0f,
    initialScale: Float = 1f,
    easing: Easing = AnimeEasing.EaseOutBack
): Modifier {
    val actualDelay = if (delayMillis != 0) delayMillis else index * baseDelay
    val spec: FiniteAnimationSpec<Float> = tween(duration, actualDelay, easing)
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else initialAlpha,
        animationSpec = spec,
        label = "animeAlpha"
    )
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else initialOffsetY.toFloat(),
        animationSpec = spec,
        label = "animeOffsetY"
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else initialScale,
        animationSpec = spec,
        label = "animeScale"
    )
    return this.graphicsLayer {
        this.alpha = alpha
        this.translationY = offsetY
        this.scaleX = scale
        this.scaleY = scale
    }
}

/**
 * Animated visibility with customizable transitions
 */
@Composable
fun AnimatedVisibilityWithTransitions(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enter: EnterTransition = fadeIn(tween(300)) + expandVertically(tween(300)),
    exit: ExitTransition = fadeOut(tween(300)) + shrinkVertically(tween(300)),
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
