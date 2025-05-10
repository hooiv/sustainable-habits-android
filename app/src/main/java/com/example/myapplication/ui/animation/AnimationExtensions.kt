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
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Custom easing functions for animations
 */
object AnimeEasing {
    val EaseInQuad: Easing = Easing { fraction -> fraction * fraction }
    val EaseOutQuad: Easing = Easing { fraction -> 1 - (1 - fraction) * (1 - fraction) }
    val EaseInOutQuad: Easing = Easing { fraction ->
        if (fraction < 0.5f) 2 * fraction * fraction else 1 - (-2 * fraction + 2).pow(2) / 2
    }
    val EaseInCubic: Easing = Easing { fraction -> fraction.pow(3) }
    val EaseOutCubic: Easing = Easing { fraction -> 1 - (1 - fraction).pow(3) }
    val EaseInOutCubic: Easing = Easing { fraction ->
        if (fraction < 0.5f) 4 * fraction.pow(3) else 1 - (-2 * fraction + 2).pow(3) / 2
    }
    val EaseInElastic: Easing = Easing { fraction ->
        if (fraction == 0f) 0f
        else if (fraction == 1f) 1f
        else {
            val c4 = (2 * Math.PI) / 3
            -2.0.pow(-10 * fraction) * kotlin.math.sin((fraction * 10 - 0.75) * c4).toFloat() + 1
        }
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
fun Modifier.animateContentSize(
    animationSpec: androidx.compose.animation.core.FiniteAnimationSpec<androidx.compose.ui.unit.IntSize> = tween(300, easing = FastOutSlowInEasing)
) = androidx.compose.animation.animateContentSize(animationSpec)

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
