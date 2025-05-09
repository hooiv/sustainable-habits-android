package com.example.myapplication.ui.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

/**
 * A 3D scene container inspired by Three.js
 * This composable creates a container where 3D-like effects can be applied
 * with enhanced interactivity and visual effects
 */
@Composable
fun ThreeJSScene(
    modifier: Modifier = Modifier,
    rotationEnabled: Boolean = true,
    initialRotationX: Float = 0f,
    initialRotationY: Float = 0f,
    initialRotationZ: Float = 0f,
    cameraDistance: Float = 8f,
    enableParallax: Boolean = false,
    enableShadows: Boolean = true,
    enableZoom: Boolean = true,
    enableTapInteraction: Boolean = true,
    backgroundColor: Color? = null,
    onTap: () -> Unit = {},
    content: @Composable (Modifier) -> Unit
) {
    var rotationX by remember { mutableStateOf(initialRotationX) }
    var rotationY by remember { mutableStateOf(initialRotationY) }
    var rotationZ by remember { mutableStateOf(initialRotationZ) }
    var scale by remember { mutableStateOf(1f) }
    var isInteracting by remember { mutableStateOf(false) }

    // For parallax effect
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // For tap animation
    var isTapped by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Auto-rotation animation if enabled and not currently interacting
    if (rotationEnabled && !isInteracting) {
        val infiniteTransition = rememberInfiniteTransition(label = "autoRotate")
        val autoRotateY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(20000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "autoRotateY"
        )

        rotationY = autoRotateY
    }

    // Tap animation
    val tapScale by animateFloatAsState(
        targetValue = if (isTapped) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tapScale"
    )

    // Shadow elevation based on interaction
    val shadowElevation by animateFloatAsState(
        targetValue = if (isInteracting && enableShadows) 16f else if (enableShadows) 8f else 0f,
        animationSpec = tween(300),
        label = "shadowElevation"
    )

    // Create the container with background if provided
    val containerModifier = if (backgroundColor != null) {
        modifier
            .shadow(
                elevation = shadowElevation.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
    } else {
        modifier
    }

    Box(
        modifier = containerModifier
            .scale(tapScale * scale)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isInteracting = true },
                    onDragEnd = { isInteracting = false },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        rotationY += dragAmount.x * 0.5f
                        rotationX += dragAmount.y * 0.5f

                        // For parallax effect
                        if (enableParallax) {
                            offsetX = (rotationY / 360f) * 30f
                            offsetY = (rotationX / 90f) * 20f
                        }

                        // Keep rotation within bounds
                        rotationX = rotationX.coerceIn(-90f, 90f)
                        rotationY %= 360f
                    }
                )
            }
            .pointerInput(enableZoom) {
                if (enableZoom) {
                    detectTapGestures(
                        onDoubleTap = {
                            // Toggle between normal and zoomed view
                            scale = if (scale > 1f) 1f else 1.5f
                        }
                    )
                }
            }
            .pointerInput(enableTapInteraction) {
                if (enableTapInteraction) {
                    detectTapGestures(
                        onTap = {
                            coroutineScope.launch {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                isTapped = true
                                delay(150)
                                isTapped = false
                                onTap()
                            }
                        }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Add subtle ambient light effect
        Canvas(modifier = Modifier.matchParentSize()) {
            // Create a subtle gradient based on rotation to simulate lighting
            val lightAngle = (rotationY % 360) / 360f
            val gradientColors = listOf(
                Color.White.copy(alpha = 0.1f + 0.1f * sin(lightAngle * 2 * PI.toFloat())),
                Color.White.copy(alpha = 0.05f)
            )

            drawRect(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(size.width * lightAngle, 0f),
                    end = Offset(size.width * (1f - lightAngle), size.height)
                )
            )
        }

        // Apply 3D transformations to content
        val sceneModifier = Modifier
            .graphicsLayer {
                this.rotationX = rotationX
                this.rotationY = rotationY
                this.rotationZ = rotationZ
                this.translationX = offsetX
                this.translationY = offsetY
                this.cameraDistance = cameraDistance * density
                this.transformOrigin = TransformOrigin(0.5f, 0.5f)
            }

        content(sceneModifier)
    }
}

/**
 * Creates a 3D cube with customizable faces and realistic lighting effects
 */
@Composable
fun ThreeJSCube(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    frontColor: Color = Color.Red,
    backColor: Color = Color.Green,
    leftColor: Color = Color.Blue,
    rightColor: Color = Color.Yellow,
    topColor: Color = Color.Cyan,
    bottomColor: Color = Color.Magenta,
    rotationX: Float = 0f,
    rotationY: Float = 0f,
    rotationZ: Float = 0f,
    enableGlow: Boolean = true,
    enableTexture: Boolean = false,
    onClick: () -> Unit = {}
) {
    // Calculate half size for positioning faces
    val halfSize = size / 2
    val density = LocalDensity.current
    val halfSizePx = with(density) { halfSize.toPx() }

    // Animation for subtle breathing effect
    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = AnimeEasing.EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheScale"
    )

    // Glow effect animation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = AnimeEasing.EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Interactive state
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "pressScale"
    )

    // Main container
    Box(
        modifier = modifier
            .size(size)
            .scale(scale * breatheScale)
            .graphicsLayer {
                this.rotationX = rotationX
                this.rotationY = rotationY
                this.rotationZ = rotationZ
                this.cameraDistance = 12f * density
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            }
    ) {
        // Glow effect behind the cube
        if (enableGlow) {
            Box(
                modifier = Modifier
                    .size(size * 1.2f)
                    .align(Alignment.Center)
                    .graphicsLayer {
                        alpha = glowAlpha
                        shadowElevation = 20f
                        shape = RoundedCornerShape(halfSize)
                        clip = true
                    }
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                frontColor.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Create all 6 faces of the cube with proper 3D positioning
        // We'll use Canvas for more control over the rendering

        // Front face
        Canvas(
            modifier = Modifier
                .size(size)
                .align(Alignment.Center)
        ) {
            // Calculate face positions based on rotation
            val rotY = (rotationY % 360 + 360) % 360
            val rotX = (rotationX % 360 + 360) % 360

            // Front face (visible when rotY is between -90 and 90 degrees)
            if (rotY < 90 || rotY > 270) {
                val opacity = cos(Math.toRadians(rotY.toDouble())).toFloat().coerceIn(0f, 1f)
                drawRect(
                    color = frontColor.copy(alpha = opacity),
                    size = Size(size.toPx(), size.toPx()),
                    topLeft = Offset(0f, 0f)
                )

                // Add texture if enabled
                if (enableTexture) {
                    // Draw a grid pattern
                    val gridSize = size.toPx() / 5
                    val gridColor = Color.White.copy(alpha = 0.1f)

                    for (i in 1..4) {
                        // Horizontal lines
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, i * gridSize),
                            end = Offset(size.toPx(), i * gridSize),
                            strokeWidth = 1f
                        )

                        // Vertical lines
                        drawLine(
                            color = gridColor,
                            start = Offset(i * gridSize, 0f),
                            end = Offset(i * gridSize, size.toPx()),
                            strokeWidth = 1f
                        )
                    }
                }
            }

            // Back face (visible when rotY is between 90 and 270 degrees)
            if (rotY > 90 && rotY < 270) {
                val opacity = cos(Math.toRadians(rotY.toDouble() - 180)).toFloat().coerceIn(0f, 1f)
                drawRect(
                    color = backColor.copy(alpha = opacity),
                    size = Size(size.toPx(), size.toPx()),
                    topLeft = Offset(0f, 0f)
                )
            }

            // Right face (visible when rotY is between 0 and 180 degrees)
            if (rotY > 0 && rotY < 180) {
                val opacity = sin(Math.toRadians(rotY.toDouble())).toFloat().coerceIn(0f, 1f)

                // Position the right face with perspective
                translate(left = size.toPx() - (opacity * 10f)) {
                    drawRect(
                        color = rightColor.copy(alpha = opacity),
                        size = Size(halfSizePx * opacity, size.toPx()),
                        topLeft = Offset(0f, 0f)
                    )
                }
            }

            // Left face (visible when rotY is between 180 and 360 degrees)
            if (rotY > 180 || rotY < 0) {
                val opacity = sin(Math.toRadians(rotY.toDouble() - 180)).toFloat().coerceIn(0f, 1f)

                // Position the left face with perspective
                translate(left = -halfSizePx * opacity) {
                    drawRect(
                        color = leftColor.copy(alpha = opacity),
                        size = Size(halfSizePx * opacity, size.toPx()),
                        topLeft = Offset(0f, 0f)
                    )
                }
            }

            // Top face (visible when rotX is between 0 and 180 degrees)
            if (rotX > 0 && rotX < 180) {
                val opacity = sin(Math.toRadians(rotX.toDouble())).toFloat().coerceIn(0f, 1f)

                // Position the top face with perspective
                translate(top = -halfSizePx * opacity) {
                    drawRect(
                        color = topColor.copy(alpha = opacity),
                        size = Size(size.toPx(), halfSizePx * opacity),
                        topLeft = Offset(0f, 0f)
                    )
                }
            }

            // Bottom face (visible when rotX is between 180 and 360 degrees)
            if (rotX > 180 || rotX < 0) {
                val opacity = sin(Math.toRadians(rotX.toDouble() - 180)).toFloat().coerceIn(0f, 1f)

                // Position the bottom face with perspective
                translate(top = size.toPx() - (opacity * 10f)) {
                    drawRect(
                        color = bottomColor.copy(alpha = opacity),
                        size = Size(size.toPx(), halfSizePx * opacity),
                        topLeft = Offset(0f, 0f)
                    )
                }
            }
        }

        // Add subtle shadow for depth
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    shadowElevation = 8f
                    alpha = 0.1f
                }
        )
    }
}

/**
 * Creates an advanced particle system similar to Three.js particle effects
 * with interactive behaviors and enhanced visual effects
 */
@Composable
fun ParticleSystem(
    modifier: Modifier = Modifier,
    particleCount: Int = 50,
    particleSize: Dp = 4.dp,
    particleColor: Color = Color.White,
    maxSpeed: Float = 2f,
    fadeDistance: Float = 0.8f,
    particleShape: ParticleShape = ParticleShape.CIRCLE,
    interactionEnabled: Boolean = false,
    particleEffect: ParticleEffect = ParticleEffect.FLOAT,
    colorVariation: Boolean = false,
    glowEffect: Boolean = false
) {
    val density = LocalDensity.current
    val particleSizePx = with(density) { particleSize.toPx() }

    // For mouse/touch interaction
    var touchPosition by remember { mutableStateOf<Offset?>(null) }
    var lastTouchTime by remember { mutableStateOf(0L) }
    val touchActive = remember { mutableStateOf(false) }

    // Generate random particles with enhanced properties
    val particles = remember {
        List(particleCount) {
            val baseColor = if (colorVariation) {
                // Create color variations based on the base color
                val hue = particleColor.toHsv()[0]
                val saturation = particleColor.toHsv()[1]
                val value = particleColor.toHsv()[2]

                // Vary the hue slightly
                val newHue = (hue + Random.nextFloat() * 20 - 10 + 360) % 360
                Color.hsv(newHue, saturation, value)
            } else {
                particleColor
            }

            ThreeJSParticle(
                x = Random.nextFloat() * 2 - 1, // -1 to 1
                y = Random.nextFloat() * 2 - 1, // -1 to 1
                z = Random.nextFloat() * 2 - 1, // -1 to 1
                speedX = (Random.nextFloat() - 0.5f) * maxSpeed,
                speedY = (Random.nextFloat() - 0.5f) * maxSpeed,
                speedZ = (Random.nextFloat() - 0.5f) * maxSpeed,
                size = particleSizePx * (0.5f + Random.nextFloat() * 0.5f), // Random size variation
                color = baseColor,
                originalX = Random.nextFloat() * 2 - 1,
                originalY = Random.nextFloat() * 2 - 1,
                originalZ = Random.nextFloat() * 2 - 1,
                rotationSpeed = Random.nextFloat() * 2f,
                rotationAngle = Random.nextFloat() * 360f,
                pulsePhase = Random.nextFloat() * 2 * PI.toFloat()
            )
        }.toMutableStateList()
    }

    // Animation to update particles
    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()

        while (true) {
            val currentTime = System.currentTimeMillis()
            val elapsedTime = (currentTime - startTime) / 1000f
            val deltaTime = 0.016f // ~60fps

            delay(16) // ~60fps

            for (i in particles.indices) {
                val particle = particles[i]

                when (particleEffect) {
                    ParticleEffect.FLOAT -> {
                        // Standard floating movement
                        particle.x += particle.speedX / 100
                        particle.y += particle.speedY / 100
                        particle.z += particle.speedZ / 100
                    }
                    ParticleEffect.WAVE -> {
                        // Wave-like movement
                        particle.x += particle.speedX / 100
                        particle.y = particle.originalY + sin(elapsedTime * 2 + particle.pulsePhase) * 0.2f
                        particle.z += particle.speedZ / 100
                    }
                    ParticleEffect.VORTEX -> {
                        // Spiral/vortex movement
                        val radius = sqrt(particle.x * particle.x + particle.y * particle.y)
                        val angle = atan2(particle.y, particle.x) + deltaTime * particle.rotationSpeed
                        particle.x = radius * cos(angle)
                        particle.y = radius * sin(angle)

                        // Slowly pull particles toward center
                        if (radius > 0.1f) {
                            particle.x *= 0.999f
                            particle.y *= 0.999f
                        } else {
                            // Reset particles that get too close to center
                            val newRadius = 0.8f + Random.nextFloat() * 0.2f
                            val newAngle = Random.nextFloat() * 2 * PI.toFloat()
                            particle.x = newRadius * cos(newAngle)
                            particle.y = newRadius * sin(newAngle)
                        }

                        particle.z += particle.speedZ / 200
                    }
                    ParticleEffect.PULSE -> {
                        // Pulsing movement from origin
                        val time = elapsedTime + particle.pulsePhase
                        val pulseRate = 0.5f
                        val pulseAmount = (sin(time * pulseRate) + 1) / 2 // 0 to 1

                        // Move between original position and current position
                        particle.x = particle.originalX * pulseAmount + particle.x * (1 - pulseAmount)
                        particle.y = particle.originalY * pulseAmount + particle.y * (1 - pulseAmount)
                        particle.z = particle.originalZ * pulseAmount + particle.z * (1 - pulseAmount)

                        // Add slight drift
                        particle.x += particle.speedX / 500
                        particle.y += particle.speedY / 500
                        particle.z += particle.speedZ / 500
                    }
                }

                // Handle interaction with touch/mouse
                if (interactionEnabled && touchActive.value && touchPosition != null) {
                    val canvasWidth = 1000f  // Placeholder, will be replaced with actual canvas size
                    val canvasHeight = 1000f // Placeholder

                    // Convert touch position to particle space (-1 to 1)
                    val touchX = (touchPosition!!.x / canvasWidth) * 2 - 1
                    val touchY = (touchPosition!!.y / canvasHeight) * 2 - 1

                    // Calculate distance to touch point (in 2D space)
                    val dx = touchX - particle.x
                    val dy = touchY - particle.y
                    val distance = sqrt(dx * dx + dy * dy)

                    // Apply force if within influence radius
                    if (distance < 0.5f) {
                        val force = 0.01f * (1 - distance / 0.5f)
                        particle.speedX += dx * force
                        particle.speedY += dy * force

                        // Limit max speed
                        val currentSpeed = sqrt(particle.speedX * particle.speedX + particle.speedY * particle.speedY)
                        if (currentSpeed > maxSpeed) {
                            val speedFactor = maxSpeed / currentSpeed
                            particle.speedX *= speedFactor
                            particle.speedY *= speedFactor
                        }
                    }
                }

                // Update rotation for non-circular particles
                particle.rotationAngle += particle.rotationSpeed

                // Apply damping to speed
                particle.speedX *= 0.99f
                particle.speedY *= 0.99f
                particle.speedZ *= 0.99f

                // Wrap around if out of bounds
                if (particle.x < -1f) particle.x = 1f
                if (particle.x > 1f) particle.x = -1f
                if (particle.y < -1f) particle.y = 1f
                if (particle.y > 1f) particle.y = -1f
                if (particle.z < -1f) particle.z = 1f
                if (particle.z > 1f) particle.z = -1f

                particles[i] = particle
            }

            // Reset touch interaction after a delay
            if (touchActive.value && System.currentTimeMillis() - lastTouchTime > 100) {
                touchActive.value = false
            }
        }
    }

    Box(
        modifier = modifier
            .pointerInput(interactionEnabled) {
                if (interactionEnabled) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            touchPosition = offset
                            lastTouchTime = System.currentTimeMillis()
                            touchActive.value = true
                        },
                        onDrag = { change, _ ->
                            touchPosition = change.position
                            lastTouchTime = System.currentTimeMillis()
                            touchActive.value = true
                        },
                        onDragEnd = {
                            lastTouchTime = System.currentTimeMillis()
                        }
                    )
                }
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2
            val centerY = canvasHeight / 2

            // Sort particles by Z for proper depth rendering
            val sortedParticles = particles.sortedBy { it.z }

            for (particle in sortedParticles) {
                // Calculate screen position
                val screenX = centerX + (particle.x * centerX)
                val screenY = centerY + (particle.y * centerY)

                // Calculate size and opacity based on Z
                val zFactor = (particle.z + 1) / 2 // Convert -1..1 to 0..1
                val displaySize = particle.size * (0.5f + zFactor * 0.5f)
                val opacity = 0.2f + (1 - (particle.z / fadeDistance).coerceIn(0f, 1f)) * 0.8f

                // Apply glow effect if enabled
                if (glowEffect) {
                    // Draw a larger, more transparent circle for glow
                    drawCircle(
                        color = particle.color.copy(alpha = opacity * 0.3f),
                        radius = displaySize * 2.5f,
                        center = Offset(screenX, screenY),
                        blendMode = BlendMode.Plus
                    )

                    // Draw a medium glow
                    drawCircle(
                        color = particle.color.copy(alpha = opacity * 0.5f),
                        radius = displaySize * 1.5f,
                        center = Offset(screenX, screenY),
                        blendMode = BlendMode.Plus
                    )
                }

                // Draw the particle with the appropriate shape
                when (particleShape) {
                    ParticleShape.CIRCLE -> {
                        drawCircle(
                            color = particle.color.copy(alpha = opacity),
                            radius = displaySize,
                            center = Offset(screenX, screenY)
                        )
                    }
                    ParticleShape.SQUARE -> {
                        rotate(particle.rotationAngle, Offset(screenX, screenY)) {
                            drawRect(
                                color = particle.color.copy(alpha = opacity),
                                topLeft = Offset(screenX - displaySize, screenY - displaySize),
                                size = Size(displaySize * 2, displaySize * 2)
                            )
                        }
                    }
                    ParticleShape.TRIANGLE -> {
                        rotate(particle.rotationAngle, Offset(screenX, screenY)) {
                            val path = Path().apply {
                                moveTo(screenX, screenY - displaySize * 1.5f)
                                lineTo(screenX - displaySize, screenY + displaySize)
                                lineTo(screenX + displaySize, screenY + displaySize)
                                close()
                            }
                            drawPath(
                                path = path,
                                color = particle.color.copy(alpha = opacity)
                            )
                        }
                    }
                    ParticleShape.STAR -> {
                        rotate(particle.rotationAngle, Offset(screenX, screenY)) {
                            val outerRadius = displaySize * 1.5f
                            val innerRadius = displaySize * 0.6f
                            val path = Path().apply {
                                // Create a 5-pointed star
                                for (i in 0 until 10) {
                                    val radius = if (i % 2 == 0) outerRadius else innerRadius
                                    val angle = (i * 36) * (PI / 180f)
                                    val x = screenX + radius * cos(angle).toFloat()
                                    val y = screenY + radius * sin(angle).toFloat()

                                    if (i == 0) {
                                        moveTo(x, y)
                                    } else {
                                        lineTo(x, y)
                                    }
                                }
                                close()
                            }
                            drawPath(
                                path = path,
                                color = particle.color.copy(alpha = opacity)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Enum defining different particle shapes
 */
enum class ParticleShape {
    CIRCLE, SQUARE, TRIANGLE, STAR
}

/**
 * Enum defining different particle movement effects
 */
enum class ParticleEffect {
    FLOAT, WAVE, VORTEX, PULSE
}

/**
 * Data class to represent a particle in 3D space with enhanced properties
 */
private data class ThreeJSParticle(
    var x: Float,
    var y: Float,
    var z: Float,
    var speedX: Float,
    var speedY: Float,
    var speedZ: Float,
    val size: Float,
    val color: Color = Color.White,
    val originalX: Float = 0f,
    val originalY: Float = 0f,
    val originalZ: Float = 0f,
    val rotationSpeed: Float = 0f,
    var rotationAngle: Float = 0f,
    val pulsePhase: Float = 0f
)

/**
 * Extension function to convert Color to HSV array
 */
private fun Color.toHsv(): FloatArray {
    val hsv = FloatArray(3)
    android.graphics.Color.RGBToHSV(
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt(),
        hsv
    )
    return hsv
}

/**
 * Create a color from HSV values
 */
private fun Color.Companion.hsv(hue: Float, saturation: Float, value: Float): Color {
    val color = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))
    return Color(color)
}
