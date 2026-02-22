package com.hooiv.habitflow.core.ui.animation

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

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
 * Returns the delay in milliseconds for a staggered list item animation.
 * The returned value is capped at [maxDelayMs] so deeply nested items don't wait forever.
 */
fun staggeredDelay(index: Int, baseDelayMs: Int, maxDelayMs: Int): Int =
    (index * baseDelayMs).coerceAtMost(maxDelayMs)

/**
 * Applies a subtle vertical bounce/pulse animation, typically used on loading placeholder items.
 * When [enabled] is false the modifier is a no-op.
 */
@Composable
fun Modifier.loadingBounceEffect(enabled: Boolean = true): Modifier {
    if (!enabled) return this
    val infiniteTransition = rememberInfiniteTransition(label = "loadingBounce")
    val translationY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounceY"
    )
    return this.graphicsLayer { this.translationY = translationY }
}

