package com.example.myapplication.ui.animation

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * Manages animations throughout the app, integrating Anime.js and Three.js inspired effects
 */
@Singleton
class AnimationManager @Inject constructor(
    private val context: Context
) {
    // Animation presets
    enum class AnimationPreset {
        BOUNCE,
        ELASTIC,
        SPRING,
        FADE,
        SLIDE,
        SCALE,
        WAVE,
        PULSE,
        MORPH,
        STAGGER
    }

    // Animation directions
    enum class AnimationDirection {
        IN,
        OUT,
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    /**
     * Create an animation modifier based on preset
     *
     * Note: This function returns a Modifier that should be used in a @Composable function
     */
    @Composable
    fun createAnimationModifier(
        preset: AnimationPreset,
        direction: AnimationDirection = AnimationDirection.IN,
        duration: Int = 500,
        delay: Int = 0,
        repeat: Boolean = false,
        index: Int = 0
    ): Modifier {
        // Return a Modifier that will be applied in a Composable context
        return Modifier.then(
            Modifier.composed {
                // These Composable functions will only be called when this modifier is used in a Composable
                var isVisible by remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()

                // Launch animation after delay
                LaunchedEffect(Unit) {
                    delay(delay.toLong())
                    isVisible = true
                }

                // Apply the appropriate animation based on preset
                when (preset) {
                    AnimationPreset.BOUNCE -> createBounceAnimation(isVisible, direction, duration, repeat)
                    AnimationPreset.ELASTIC -> createElasticAnimation(isVisible, direction, duration, repeat)
                    AnimationPreset.SPRING -> createSpringAnimation(isVisible, direction, duration, repeat)
                    AnimationPreset.FADE -> createFadeAnimation(isVisible, direction, duration, repeat)
                    AnimationPreset.SLIDE -> createSlideAnimation(isVisible, direction, duration, repeat)
                    AnimationPreset.SCALE -> createScaleAnimation(isVisible, direction, duration, repeat)
                    AnimationPreset.WAVE -> createWaveAnimation(isVisible, index, duration, repeat)
                    AnimationPreset.PULSE -> createPulseAnimation(isVisible, duration, repeat)
                    AnimationPreset.MORPH -> createMorphAnimation(isVisible, direction, duration, repeat)
                    AnimationPreset.STAGGER -> createStaggerAnimation(isVisible, direction, duration, index)
                }
            }
        )
    }

    /**
     * Create a bounce animation
     *
     * Note: This function should only be called from within a Composable context
     */
    @Composable
    private fun createBounceAnimation(
        isVisible: Boolean,
        direction: AnimationDirection,
        duration: Int,
        repeat: Boolean
    ): Modifier {
        // Create the animation specification
        val animationSpec: AnimationSpec<Float> = if (repeat) {
            infiniteRepeatable(
                animation = tween(
                    durationMillis = duration,
                    easing = AnimeEasing.EaseOutBounce
                ),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(
                durationMillis = duration,
                easing = AnimeEasing.EaseOutBounce
            )
        }

        // Animate the offset value
        val offset by animateFloatAsState(
            targetValue = if (isVisible) 0f else getInitialOffset(direction),
            animationSpec = animationSpec,
            label = "bounce"
        )

        // Apply the appropriate modifier based on direction
        return when (direction) {
            AnimationDirection.UP, AnimationDirection.DOWN -> Modifier.offset(y = offset.dp)
            AnimationDirection.LEFT, AnimationDirection.RIGHT -> Modifier.offset(x = offset.dp)
            else -> Modifier.scale(if (isVisible) 1f else 0.5f)
        }
    }

    /**
     * Create an elastic animation
     *
     * Note: This function should only be called from within a Composable context
     */
    @Composable
    private fun createElasticAnimation(
        isVisible: Boolean,
        direction: AnimationDirection,
        duration: Int,
        repeat: Boolean
    ): Modifier {
        // Create the animation specification
        val animationSpec: AnimationSpec<Float> = if (repeat) {
            infiniteRepeatable(
                animation = tween(
                    durationMillis = duration,
                    easing = AnimeEasing.EaseOutElastic
                ),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(
                durationMillis = duration,
                easing = AnimeEasing.EaseOutElastic
            )
        }

        // Animate the scale value
        val scale by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = animationSpec,
            label = "elastic"
        )

        // Return the scale modifier
        return Modifier.scale(scale)
    }

    /**
     * Create a spring animation
     *
     * Note: This function should only be called from within a Composable context
     */
    @Composable
    private fun createSpringAnimation(
        isVisible: Boolean,
        direction: AnimationDirection,
        duration: Int,
        repeat: Boolean
    ): Modifier {
        // Create the animation specification
        val animationSpec: AnimationSpec<Float> = if (repeat) {
            infiniteRepeatable(
                animation = tween<Float>(
                    durationMillis = duration,
                    easing = AnimeEasing.EaseOutElastic
                ),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween<Float>(
                durationMillis = duration,
                easing = AnimeEasing.EaseOutElastic
            )
        }

        // Animate the scale value
        val scale by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0.5f,
            animationSpec = animationSpec,
            label = "spring"
        )

        // Return the scale modifier
        return Modifier.scale(scale)
    }

    /**
     * Create a fade animation
     *
     * Note: This function should only be called from within a Composable context
     */
    @Composable
    private fun createFadeAnimation(
        isVisible: Boolean,
        direction: AnimationDirection,
        duration: Int,
        repeat: Boolean
    ): Modifier {
        // Create the animation specification
        val animationSpec: AnimationSpec<Float> = if (repeat) {
            infiniteRepeatable(
                animation = tween<Float>(
                    durationMillis = duration,
                    easing = AnimeEasing.EaseInOutSine
                ),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween<Float>(
                durationMillis = duration,
                easing = AnimeEasing.EaseInOutSine
            )
        }

        // Animate the alpha value
        val alpha by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = animationSpec,
            label = "fade"
        )

        // Return the alpha modifier
        return Modifier.alpha(alpha)
    }

    /**
     * Create a slide animation
     *
     * Note: This function should only be called from within a Composable context
     */
    @Composable
    private fun createSlideAnimation(
        isVisible: Boolean,
        direction: AnimationDirection,
        duration: Int,
        repeat: Boolean
    ): Modifier {
        // Create the animation specification
        val animationSpec: AnimationSpec<Float> = if (repeat) {
            infiniteRepeatable(
                animation = tween<Float>(
                    durationMillis = duration,
                    easing = AnimeEasing.EaseInOutQuad
                ),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween<Float>(
                durationMillis = duration,
                easing = AnimeEasing.EaseInOutQuad
            )
        }

        // Animate the offset value
        val offset by animateFloatAsState(
            targetValue = if (isVisible) 0f else getInitialOffset(direction),
            animationSpec = animationSpec,
            label = "slide"
        )

        // Return the appropriate modifier based on direction
        return when (direction) {
            AnimationDirection.UP, AnimationDirection.DOWN -> Modifier.offset(y = offset.dp)
            AnimationDirection.LEFT, AnimationDirection.RIGHT -> Modifier.offset(x = offset.dp)
            else -> Modifier.offset(y = offset.dp)
        }
    }

    /**
     * Create a scale animation
     *
     * Note: This function should only be called from within a Composable context
     */
    @Composable
    private fun createScaleAnimation(
        isVisible: Boolean,
        direction: AnimationDirection,
        duration: Int,
        repeat: Boolean
    ): Modifier {
        // Create the animation specification
        val animationSpec: AnimationSpec<Float> = if (repeat) {
            infiniteRepeatable(
                animation = tween<Float>(
                    durationMillis = duration,
                    easing = AnimeEasing.EaseOutQuart
                ),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween<Float>(
                durationMillis = duration,
                easing = AnimeEasing.EaseOutQuart
            )
        }

        // Animate the scale value
        val scale by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = animationSpec,
            label = "scale"
        )

        // Return the scale modifier
        return Modifier.scale(scale)
    }

    /**
     * Create a wave animation
     *
     * Note: This function should only be called from within a Composable context
     */
    @Composable
    private fun createWaveAnimation(
        isVisible: Boolean,
        index: Int,
        duration: Int,
        repeat: Boolean
    ): Modifier {
        // Create the animation specification
        val animationSpec: AnimationSpec<Float> = if (repeat) {
            infiniteRepeatable(
                animation = tween<Float>(
                    durationMillis = duration,
                    easing = AnimeEasing.EaseInOutSine
                ),
                repeatMode = RepeatMode.Restart
            )
        } else {
            tween<Float>(
                durationMillis = duration,
                easing = AnimeEasing.EaseInOutSine
            )
        }

        // Calculate phase based on index
        val phase = index * 0.2f

        // Create infinite transition for wave effect
        val infiniteTransition = rememberInfiniteTransition(label = "wave")
        val waveOffset by infiniteTransition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = AnimeEasing.EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "waveOffset"
        )

        // Calculate final offset
        val offset = if (isVisible) waveOffset * sin(phase) else 0f

        // Return the offset modifier
        return Modifier.offset(y = offset.dp)
    }

    /**
     * Create a pulse animation
     *
     * Note: This function should only be called from within a Composable context
     */
    @Composable
    private fun createPulseAnimation(
        isVisible: Boolean,
        duration: Int,
        repeat: Boolean
    ): Modifier {
        // Create infinite transition for pulse effect
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration / 2, easing = AnimeEasing.EaseInOutQuad),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )

        // Calculate final scale
        val actualScale = if (isVisible) scale else 0f

        // Return the scale modifier
        return Modifier.scale(actualScale)
    }

    /**
     * Create a morph animation
     *
     * Note: This function should only be called from within a Composable context
     */
    @Composable
    private fun createMorphAnimation(
        isVisible: Boolean,
        direction: AnimationDirection,
        duration: Int,
        repeat: Boolean
    ): Modifier {
        // Create the animation specification
        val animationSpec: AnimationSpec<Float> = if (repeat) {
            infiniteRepeatable(
                animation = tween<Float>(
                    durationMillis = duration,
                    easing = AnimeEasing.EaseInOutElastic
                ),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween<Float>(
                durationMillis = duration,
                easing = AnimeEasing.EaseInOutElastic
            )
        }

        // Animate the rotation value
        val rotation by animateFloatAsState(
            targetValue = if (isVisible) 360f else 0f,
            animationSpec = animationSpec,
            label = "morph"
        )

        // Return the graphics layer modifier
        return Modifier.graphicsLayer {
            rotationZ = rotation
        }
    }

    /**
     * Create a stagger animation
     *
     * Note: This function should only be called from within a Composable context
     */
    @Composable
    private fun createStaggerAnimation(
        isVisible: Boolean,
        direction: AnimationDirection,
        duration: Int,
        index: Int
    ): Modifier {
        // Calculate delay based on index
        val delay = index * 100 // 100ms delay between items

        // Create the animation specification with delay
        val animationSpec: AnimationSpec<Float> = tween<Float>(
            durationMillis = duration,
            delayMillis = delay,
            easing = AnimeEasing.EaseOutQuart
        )

        // Animate the alpha value
        val alpha by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = animationSpec,
            label = "staggerAlpha"
        )

        // Animate the offset value
        val offset by animateFloatAsState(
            targetValue = if (isVisible) 0f else getInitialOffset(direction),
            animationSpec = animationSpec,
            label = "staggerOffset"
        )

        // Return the appropriate modifier based on direction
        return when (direction) {
            AnimationDirection.UP, AnimationDirection.DOWN ->
                Modifier.alpha(alpha).offset(y = offset.dp)
            AnimationDirection.LEFT, AnimationDirection.RIGHT ->
                Modifier.alpha(alpha).offset(x = offset.dp)
            else ->
                Modifier.alpha(alpha).scale(if (isVisible) 1f else 0.5f)
        }
    }

    /**
     * Get initial offset based on direction
     */
    private fun getInitialOffset(direction: AnimationDirection): Float {
        return when (direction) {
            AnimationDirection.UP -> 50f
            AnimationDirection.DOWN -> -50f
            AnimationDirection.LEFT -> 50f
            AnimationDirection.RIGHT -> -50f
            else -> 0f
        }
    }
}
