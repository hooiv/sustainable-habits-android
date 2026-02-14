package com.example.myapplication.core.ui.animation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import kotlin.math.*

/**
 * Custom easing functions for animations
 */
object CustomEasing {
    /**
     * Easing that starts slow, speeds up in the middle, and slows down at the end
     */
    val EaseInOutQuad: Easing = CubicBezierEasing(0.455f, 0.03f, 0.515f, 0.955f)

    /**
     * Easing that creates a smooth sinusoidal transition
     */
    val EaseInOutSine: Easing = CubicBezierEasing(0.445f, 0.05f, 0.55f, 0.95f)

    /**
     * Easing that simulates a bounce effect at the end
     */
    val EaseOutBounce: Easing = Easing { fraction ->
        val n1 = 7.5625f
        val d1 = 2.75f
        var newFraction = fraction

        return@Easing when {
            newFraction < 1f / d1 -> n1 * newFraction * newFraction
            newFraction < 2f / d1 -> {
                newFraction -= 1.5f / d1
                n1 * newFraction * newFraction + 0.75f
            }
            newFraction < 2.5f / d1 -> {
                newFraction -= 2.25f / d1
                n1 * newFraction * newFraction + 0.9375f
            }
            else -> {
                newFraction -= 2.625f / d1
                n1 * newFraction * newFraction + 0.984375f
            }
        }
    }

    /**
     * Easing that simulates an elastic effect
     */
    val EaseOutElastic: Easing = Easing { fraction ->
        if (fraction == 0f || fraction == 1f) {
            fraction
        } else {
            val c4 = (2 * PI) / 3
            Math.pow(2.0, -10.0 * fraction) * sin((fraction * 10 - 0.75) * c4) + 1
        }.toFloat()
    }

    /**
     * Easing that starts fast and slows down at the end
     */
    val EaseOutQuart: Easing = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)

    /**
     * Easing that starts slow and speeds up at the end
     */
    val EaseInQuart: Easing = CubicBezierEasing(0.5f, 0f, 0.75f, 0f)
}
