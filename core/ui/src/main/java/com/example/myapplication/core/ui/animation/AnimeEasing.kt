package com.example.myapplication.core.ui.animation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import kotlin.math.*

/**
 * A collection of easing functions inspired by anime.js for more natural and expressive animations.
 * These easing functions provide a variety of motion styles for different animation needs.
 */
object AnimeEasing {
    // Standard easing functions
    val Linear: Easing = Easing { fraction -> fraction }

    // Ease In functions
    val EaseInSine: Easing = CubicBezierEasing(0.47f, 0f, 0.745f, 0.715f)
    val EaseInQuad: Easing = CubicBezierEasing(0.55f, 0.085f, 0.68f, 0.53f)
    val EaseInCubic: Easing = CubicBezierEasing(0.55f, 0.055f, 0.675f, 0.19f)
    val EaseInQuart: Easing = CubicBezierEasing(0.895f, 0.03f, 0.685f, 0.22f)
    val EaseInQuint: Easing = CubicBezierEasing(0.755f, 0.05f, 0.855f, 0.06f)
    val EaseInExpo: Easing = CubicBezierEasing(0.95f, 0.05f, 0.795f, 0.035f)
    val EaseInCirc: Easing = CubicBezierEasing(0.6f, 0.04f, 0.98f, 0.335f)

    // Ease Out functions
    val EaseOutSine: Easing = CubicBezierEasing(0.39f, 0.575f, 0.565f, 1f)
    val EaseOutQuad: Easing = CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f)
    val EaseOutCubic: Easing = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1f)
    val EaseOutQuart: Easing = CubicBezierEasing(0.165f, 0.84f, 0.44f, 1f)
    val EaseOutQuint: Easing = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)
    val EaseOutExpo: Easing = CubicBezierEasing(0.19f, 1f, 0.22f, 1f)
    val EaseOutCirc: Easing = CubicBezierEasing(0.075f, 0.82f, 0.165f, 1f)

    // Ease In Out functions
    val EaseInOutSine: Easing = CubicBezierEasing(0.445f, 0.05f, 0.55f, 0.95f)
    val EaseInOutQuad: Easing = CubicBezierEasing(0.455f, 0.03f, 0.515f, 0.955f)
    val EaseInOutCubic: Easing = CubicBezierEasing(0.645f, 0.045f, 0.355f, 1f)
    val EaseInOutQuart: Easing = CubicBezierEasing(0.77f, 0f, 0.175f, 1f)
    val EaseInOutQuint: Easing = CubicBezierEasing(0.86f, 0f, 0.07f, 1f)
    val EaseInOutExpo: Easing = CubicBezierEasing(1f, 0f, 0f, 1f)
    val EaseInOutCirc: Easing = CubicBezierEasing(0.785f, 0.135f, 0.15f, 0.86f)

    // Special easing functions
    val EaseInBack: Easing = Easing { x ->
        val c1 = 1.70158f
        val c3 = c1 + 1f

        c3 * x * x * x - c1 * x * x
    }

    val EaseOutBack: Easing = Easing { x ->
        val c1 = 1.70158f
        val c3 = c1 + 1f

        1f + c3 * (x - 1).toDouble().pow(3.0).toFloat() + c1 * (x - 1).toDouble().pow(2.0).toFloat()
    }

    val EaseInOutBack: Easing = Easing { x ->
        val c1 = 1.70158f
        val c2 = c1 * 1.525f

        if (x < 0.5f) {
            ((2 * x.toDouble()).pow(2.0) * ((c2 + 1) * 2 * x - c2) / 2).toFloat()
        } else {
            ((2 * x - 2.0).pow(2.0) * ((c2 + 1) * (x * 2 - 2) + c2) + 2) / 2
        }.toFloat()
    }

    val EaseInElastic: Easing = Easing { x ->
        val c4 = (2 * PI) / 3

        if (x == 0f) {
            0f
        } else if (x == 1f) {
            1f
        } else {
            (-2.0.pow(10.0 * x - 10.0) * sin((x * 10.0 - 10.75) * c4)).toFloat()
        }
    }

    val EaseOutElastic: Easing = Easing { x ->
        val c4 = (2 * PI) / 3

        if (x == 0f) {
            0f
        } else if (x == 1f) {
            1f
        } else {
            (2.0.pow(-10.0 * x) * sin((x * 10.0 - 0.75) * c4) + 1).toFloat()
        }
    }

    val EaseInOutElastic: Easing = Easing { x ->
        val c5 = (2 * PI) / 4.5

        if (x == 0f) {
            0f
        } else if (x == 1f) {
            1f
        } else if (x < 0.5f) {
            (-(2.0.pow(20.0 * x - 10.0) * sin((20.0 * x - 11.125) * c5)) / 2.0).toFloat()
        } else {
            (2.0.pow(-20.0 * x + 10.0) * sin((20.0 * x - 11.125) * c5) / 2.0 + 1.0).toFloat()
        }
    }

    // Bounce easing functions
    val EaseInBounce: Easing = Easing { x ->
        1 - EaseOutBounce.transform(1 - x)
    }

    val EaseOutBounce: Easing = Easing { x ->
        val n1 = 7.5625f
        val d1 = 2.75f

        when {
            x < 1f / d1 -> n1 * x * x
            x < 2f / d1 -> n1 * (x - 1.5f / d1) * (x - 1.5f / d1) + 0.75f
            x < 2.5f / d1 -> n1 * (x - 2.25f / d1) * (x - 2.25f / d1) + 0.9375f
            else -> n1 * (x - 2.625f / d1) * (x - 2.625f / d1) + 0.984375f
        }
    }

    val EaseInOutBounce: Easing = Easing { x ->
        if (x < 0.5f)
            (1 - EaseOutBounce.transform(1 - 2 * x)) / 2
        else
            (1 + EaseOutBounce.transform(2 * x - 1)) / 2
    }

    // Material Design standard easing
    val StandardEasing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    val AccelerateEasing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
    val DecelerateEasing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
}
