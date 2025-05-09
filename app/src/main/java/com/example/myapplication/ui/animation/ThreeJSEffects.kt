package com.example.myapplication.ui.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

/**
 * A 3D scene container inspired by Three.js
 * This composable creates a container where 3D-like effects can be applied
 */
@Composable
fun ThreeJSScene(
    modifier: Modifier = Modifier,
    rotationEnabled: Boolean = true,
    initialRotationX: Float = 0f,
    initialRotationY: Float = 0f,
    initialRotationZ: Float = 0f,
    cameraDistance: Float = 8f,
    content: @Composable (Modifier) -> Unit
) {
    var rotationX by remember { mutableStateOf(initialRotationX) }
    var rotationY by remember { mutableStateOf(initialRotationY) }
    var rotationZ by remember { mutableStateOf(initialRotationZ) }
    
    // Auto-rotation animation if enabled
    if (rotationEnabled) {
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
    
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    rotationY += dragAmount.x * 0.5f
                    rotationX += dragAmount.y * 0.5f
                    
                    // Keep rotation within bounds
                    rotationX = rotationX.coerceIn(-90f, 90f)
                    rotationY %= 360f
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val sceneModifier = Modifier.graphicsLayer {
            this.rotationX = rotationX
            this.rotationY = rotationY
            this.rotationZ = rotationZ
            this.cameraDistance = cameraDistance * density
            this.transformOrigin = TransformOrigin(0.5f, 0.5f)
        }
        
        content(sceneModifier)
    }
}

/**
 * Creates a 3D cube with customizable faces
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
    rotationZ: Float = 0f
) {
    val sizeInPx = with(LocalDensity.current) { size.toPx() }
    val halfSize = sizeInPx / 2
    
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                this.rotationX = rotationX
                this.rotationY = rotationY
                this.rotationZ = rotationZ
                this.cameraDistance = 12f * density
            }
    ) {
        // Front face
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    translationZ = halfSize
                }
                .background(frontColor)
        )
        
        // Back face
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    translationZ = -halfSize
                    rotationY = 180f
                }
                .background(backColor)
        )
        
        // Left face
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    translationX = -halfSize
                    rotationY = -90f
                }
                .background(leftColor)
        )
        
        // Right face
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    translationX = halfSize
                    rotationY = 90f
                }
                .background(rightColor)
        )
        
        // Top face
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    translationY = -halfSize
                    rotationX = 90f
                }
                .background(topColor)
        )
        
        // Bottom face
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    translationY = halfSize
                    rotationX = -90f
                }
                .background(bottomColor)
        )
    }
}

/**
 * Creates a particle system similar to Three.js particle effects
 */
@Composable
fun ParticleSystem(
    modifier: Modifier = Modifier,
    particleCount: Int = 50,
    particleSize: Dp = 4.dp,
    particleColor: Color = Color.White,
    maxSpeed: Float = 2f,
    fadeDistance: Float = 0.8f
) {
    val density = LocalDensity.current
    val particleSizePx = with(density) { particleSize.toPx() }
    
    // Generate random particles
    val particles = remember {
        List(particleCount) {
            Particle(
                x = Random.nextFloat() * 2 - 1, // -1 to 1
                y = Random.nextFloat() * 2 - 1, // -1 to 1
                z = Random.nextFloat() * 2 - 1, // -1 to 1
                speedX = (Random.nextFloat() - 0.5f) * maxSpeed,
                speedY = (Random.nextFloat() - 0.5f) * maxSpeed,
                speedZ = (Random.nextFloat() - 0.5f) * maxSpeed,
                size = particleSizePx * (0.5f + Random.nextFloat() * 0.5f) // Random size variation
            )
        }.toMutableStateList()
    }
    
    // Animation to update particles
    LaunchedEffect(Unit) {
        while (true) {
            delay(16) // ~60fps
            for (i in particles.indices) {
                val particle = particles[i]
                
                // Update position
                particle.x += particle.speedX / 100
                particle.y += particle.speedY / 100
                particle.z += particle.speedZ / 100
                
                // Wrap around if out of bounds
                if (particle.x < -1f) particle.x = 1f
                if (particle.x > 1f) particle.x = -1f
                if (particle.y < -1f) particle.y = 1f
                if (particle.y > 1f) particle.y = -1f
                if (particle.z < -1f) particle.z = 1f
                if (particle.z > 1f) particle.z = -1f
                
                particles[i] = particle
            }
        }
    }
    
    Canvas(modifier = modifier) {
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
            
            drawCircle(
                color = particleColor.copy(alpha = opacity),
                radius = displaySize,
                center = Offset(screenX, screenY)
            )
        }
    }
}

/**
 * Data class to represent a particle in 3D space
 */
private data class Particle(
    var x: Float,
    var y: Float,
    var z: Float,
    val speedX: Float,
    val speedY: Float,
    val speedZ: Float,
    val size: Float
)
