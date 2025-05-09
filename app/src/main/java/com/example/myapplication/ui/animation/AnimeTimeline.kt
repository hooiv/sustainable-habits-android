package com.example.myapplication.ui.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Anime.js inspired easing functions
 */
object AnimeEasing {
    val Linear = Easing { it }
    
    val EaseInSine = Easing { fraction ->
        1 - cos((fraction * PI) / 2).toFloat()
    }
    
    val EaseOutSine = Easing { fraction ->
        sin((fraction * PI) / 2).toFloat()
    }
    
    val EaseInOutSine = Easing { fraction ->
        -(cos(PI * fraction).toFloat() - 1) / 2
    }
    
    val EaseInQuad = Easing { fraction ->
        fraction * fraction
    }
    
    val EaseOutQuad = Easing { fraction ->
        1 - (1 - fraction) * (1 - fraction)
    }
    
    val EaseInOutQuad = Easing { fraction ->
        if (fraction < 0.5f) 2 * fraction * fraction else 1 - (-2 * fraction + 2).pow(2) / 2
    }
    
    val EaseInCubic = Easing { fraction ->
        fraction.pow(3)
    }
    
    val EaseOutCubic = Easing { fraction ->
        1 - (1 - fraction).pow(3)
    }
    
    val EaseInOutCubic = Easing { fraction ->
        if (fraction < 0.5f) 4 * fraction.pow(3) else 1 - (-2 * fraction + 2).pow(3) / 2
    }
    
    val EaseInQuart = Easing { fraction ->
        fraction.pow(4)
    }
    
    val EaseOutQuart = Easing { fraction ->
        1 - (1 - fraction).pow(4)
    }
    
    val EaseInOutQuart = Easing { fraction ->
        if (fraction < 0.5f) 8 * fraction.pow(4) else 1 - (-2 * fraction + 2).pow(4) / 2
    }
    
    val EaseInExpo = Easing { fraction ->
        if (fraction == 0f) 0f else 2.0.pow((10 * fraction - 10).toDouble()).toFloat()
    }
    
    val EaseOutExpo = Easing { fraction ->
        if (fraction == 1f) 1f else 1 - 2.0.pow((-10 * fraction).toDouble()).toFloat()
    }
    
    val EaseInOutExpo = Easing { fraction ->
        when {
            fraction == 0f -> 0f
            fraction == 1f -> 1f
            fraction < 0.5f -> 2.0.pow((20 * fraction - 10).toDouble()).toFloat() / 2
            else -> (2 - 2.0.pow((-20 * fraction + 10).toDouble()).toFloat()) / 2
        }
    }
    
    val EaseInElastic = Easing { fraction ->
        if (fraction == 0f) 0f
        else if (fraction == 1f) 1f
        else {
            val c4 = (2 * PI) / 3
            -2.0.pow((10 * fraction - 10).toDouble()).toFloat() * 
                sin(((fraction * 10 - 10.75) * c4).toFloat())
        }
    }
    
    val EaseOutElastic = Easing { fraction ->
        if (fraction == 0f) 0f
        else if (fraction == 1f) 1f
        else {
            val c4 = (2 * PI) / 3
            2.0.pow((-10 * fraction).toDouble()).toFloat() * 
                sin(((fraction * 10 - 0.75) * c4).toFloat()) + 1
        }
    }
    
    val EaseInOutElastic = Easing { fraction ->
        val c5 = (2 * PI) / 4.5
        when {
            fraction == 0f -> 0f
            fraction == 1f -> 1f
            fraction < 0.5f -> {
                -(2.0.pow((20 * fraction - 10).toDouble()).toFloat() * 
                    sin(((20 * fraction - 11.125) * c5).toFloat())) / 2
            }
            else -> {
                (2.0.pow((-20 * fraction + 10).toDouble()).toFloat() * 
                    sin(((20 * fraction - 11.125) * c5).toFloat())) / 2 + 1
            }
        }
    }
    
    val EaseInBack = Easing { fraction ->
        val c1 = 1.70158f
        val c3 = c1 + 1
        c3 * fraction.pow(3) - c1 * fraction.pow(2)
    }
    
    val EaseOutBack = Easing { fraction ->
        val c1 = 1.70158f
        val c3 = c1 + 1
        1 + c3 * (fraction - 1).pow(3) + c1 * (fraction - 1).pow(2)
    }
    
    val EaseInOutBack = Easing { fraction ->
        val c1 = 1.70158f
        val c2 = c1 * 1.525f
        
        if (fraction < 0.5f) {
            ((2 * fraction).pow(2) * ((c2 + 1) * 2 * fraction - c2)) / 2
        } else {
            ((2 * fraction - 2).pow(2) * ((c2 + 1) * (fraction * 2 - 2) + c2) + 2) / 2
        }
    }
    
    val EaseInBounce = Easing { fraction ->
        1 - bounceOut(1 - fraction)
    }
    
    val EaseOutBounce = Easing { fraction ->
        bounceOut(fraction)
    }
    
    val EaseInOutBounce = Easing { fraction ->
        if (fraction < 0.5f) {
            (1 - bounceOut(1 - 2 * fraction)) / 2
        } else {
            (1 + bounceOut(2 * fraction - 1)) / 2
        }
    }
    
    private fun bounceOut(x: Float): Float {
        val n1 = 7.5625f
        val d1 = 2.75f
        
        return when {
            x < 1f / d1 -> n1 * x * x
            x < 2f / d1 -> n1 * (x - 1.5f / d1) * (x - 1.5f / d1) + 0.75f
            x < 2.5f / d1 -> n1 * (x - 2.25f / d1) * (x - 2.25f / d1) + 0.9375f
            else -> n1 * (x - 2.625f / d1) * (x - 2.625f / d1) + 0.984375f
        }
    }
}

/**
 * A modifier that applies anime.js-like staggered entrance animations
 */
fun Modifier.animeEntrance(
    visible: Boolean,
    index: Int = 0,
    baseDelay: Int = 100,
    duration: Int = 500,
    initialOffsetY: Int = 50,
    initialAlpha: Float = 0f,
    initialScale: Float = 0.8f,
    easing: Easing = AnimeEasing.EaseOutElastic
): Modifier = composed {
    var animationPlayed by remember { mutableStateOf(false) }
    val delay = index * baseDelay
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (visible && animationPlayed) 1f else initialAlpha,
        animationSpec = tween(
            durationMillis = duration,
            delayMillis = delay,
            easing = easing
        ),
        label = "alpha"
    )
    
    val animatedOffsetY by animateIntAsState(
        targetValue = if (visible && animationPlayed) 0 else initialOffsetY,
        animationSpec = tween(
            durationMillis = duration,
            delayMillis = delay,
            easing = easing
        ),
        label = "offsetY"
    )
    
    val animatedScale by animateFloatAsState(
        targetValue = if (visible && animationPlayed) 1f else initialScale,
        animationSpec = tween(
            durationMillis = duration,
            delayMillis = delay,
            easing = easing
        ),
        label = "scale"
    )
    
    LaunchedEffect(visible) {
        if (visible && !animationPlayed) {
            animationPlayed = true
        }
    }
    
    this
        .graphicsLayer {
            alpha = animatedAlpha
            scaleX = animatedScale
            scaleY = animatedScale
            translationY = animatedOffsetY.toFloat()
        }
}

/**
 * A modifier that applies anime.js-like morphing animations
 */
fun Modifier.animeMorph(
    targetShape: Float = 1f,
    duration: Int = 1000,
    easing: Easing = AnimeEasing.EaseInOutElastic
): Modifier = composed {
    val animatedShape by animateFloatAsState(
        targetValue = targetShape,
        animationSpec = tween(
            durationMillis = duration,
            easing = easing
        ),
        label = "shape"
    )
    
    this.graphicsLayer {
        this.shape = RoundedCornerShape(
            topStart = (animatedShape * 50).dp,
            topEnd = ((1 - animatedShape) * 50).dp,
            bottomStart = ((1 - animatedShape) * 50).dp,
            bottomEnd = (animatedShape * 50).dp
        )
        clip = true
    }
}
