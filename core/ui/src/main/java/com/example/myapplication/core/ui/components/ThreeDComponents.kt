package com.example.myapplication.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.myapplication.core.ui.animation.flipCard
import com.example.myapplication.core.ui.animation.threeDCard
import kotlin.math.abs

/**
 * A card component that uses 3D-like transformations similar to three.js effects.
 * The card responds to touch with a 3D tilt effect.
 */
@Composable
fun ThreeDCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    val rotationX by animateFloatAsState(
        targetValue = if (isPressed) 0f else offsetY * 0.6f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rotationX"
    )
    
    val rotationY by animateFloatAsState(
        targetValue = if (isPressed) 0f else -offsetX * 0.6f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rotationY"
    )
    
    val elevation by animateFloatAsState(
        targetValue = if (isPressed) 4f else 8f,
        label = "elevation"
    )
    
    Card(
        modifier = modifier
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDragCancel = {
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount).coerceIn(-20f, 20f)
                    }
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .threeDCard(
                rotationX = rotationX,
                rotationY = rotationY,
                shadowElevation = elevation
            ),
        colors = CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp)
    ) {
        content()
    }
}

/**
 * A flip card component that has two sides and can be flipped with animation.
 */
@Composable
fun FlipCard(
    modifier: Modifier = Modifier,
    isFlipped: Boolean,
    frontContent: @Composable () -> Unit,
    backContent: @Composable () -> Unit,
    onClick: () -> Unit = {}
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "flip"
    )
    
    Box(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Box(
            modifier = Modifier
                .flipCard(
                    rotationY = rotation
                )
                .fillMaxSize()
        ) {
            if (rotation <= 90f) {
                Box(modifier = Modifier.fillMaxSize()) {
                    frontContent()
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .flipCard(rotationY = 180f)
                ) {
                    backContent()
                }
            }
        }
    }
}

