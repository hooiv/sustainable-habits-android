package com.example.myapplication.core.ui.animation

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer

/**
 * A custom modifier that adds entrance animation to a composable.
 * This creates a staggered animation effect when multiple items use this modifier with different indices.
 *
 * @param visible Whether the element should be visible
 * @param index The index of the element in a sequence, used for staggered animations
 * @param baseDelay The base delay in milliseconds before the animation starts
 * @param duration The duration of the animation in milliseconds
 * @param initialOffsetX The initial X offset for the element (slide in from left/right)
 * @param initialOffsetY The initial Y offset for the element (slide in from top/bottom)
 * @param initialScale The initial scale of the element (grow from small to normal)
 * @param initialAlpha The initial alpha (opacity) of the element (fade in)
 * @param easing The easing function to use for the animation
 */
fun Modifier.animeEntrance(
    visible: Boolean,
    index: Int = 0,
    baseDelay: Int = 50,
    duration: Int = 500,
    initialOffsetX: Int = 0,
    initialOffsetY: Int = 0,
    initialScale: Float = 1f,
    initialAlpha: Float = 0f,
    easing: Easing = AnimeEasing.EaseOutCubic
): Modifier = composed {
    val transition = updateTransition(targetState = visible, label = "entrance")
    
    // Calculate delay based on index for staggered animations
    val delay = baseDelay * index
    
    // Animate alpha (opacity)
    val alpha by transition.animateFloat(
        transitionSpec = {
            tween(
                durationMillis = duration,
                delayMillis = delay,
                easing = easing
            )
        },
        label = "alpha"
    ) { if (it) 1f else initialAlpha }
    
    // Animate translation and scale
    val offset by transition.animateFloat(
        transitionSpec = {
            tween(
                durationMillis = duration,
                delayMillis = delay,
                easing = easing
            )
        },
        label = "offset"
    ) { if (it) 0f else 1f }
    
    Modifier
        .alpha(alpha)
        .graphicsLayer {
            translationX = initialOffsetX * offset
            translationY = initialOffsetY * offset
            scaleX = initialScale + (1f - initialScale) * (1f - offset)
            scaleY = initialScale + (1f - initialScale) * (1f - offset)
        }
}
