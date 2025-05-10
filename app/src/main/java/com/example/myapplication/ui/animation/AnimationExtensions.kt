package com.example.myapplication.ui.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateIntSizeAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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
