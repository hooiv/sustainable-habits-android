package com.example.myapplication.core.ui.animation

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom blur implementation that doesn't rely on the actual blur modifier
 * This simulates a blur effect by drawing the content with a shadow and transparency
 */
fun Modifier.customBlur(radius: Dp) = this.then(
    Modifier
        .graphicsLayer {
            this.alpha = 0.99f
            this.shadowElevation = radius.toPx() / 2
        }
        .drawBehind {
            // Draw a semi-transparent overlay to simulate blur
            drawRect(
                color = Color.White.copy(alpha = 0.15f),
                size = size
            )
        }
)

/**
 * Alternative blur implementation that creates a glow effect
 * Useful for creating glowing UI elements
 */
fun Modifier.glowEffect(
    radius: Dp,
    color: Color = Color.White.copy(alpha = 0.5f)
) = this.then(
    Modifier
        .shadow(
            elevation = radius,
            spotColor = color
        )
        .drawBehind {
            // Draw a glow effect
            drawCircle(
                color = color,
                radius = size.minDimension / 2 + radius.toPx() / 2,
                alpha = 0.3f
            )
        }
)

/**
 * Creates a soft shadow effect that simulates blur
 */
fun Modifier.softShadowEffect(
    radius: Dp,
    color: Color = Color.Black.copy(alpha = 0.2f)
) = this.then(
    Modifier
        .shadow(
            elevation = radius / 2,
            spotColor = color
        )
        .drawBehind {
            // Draw a semi-transparent overlay
            drawRect(
                color = color.copy(alpha = 0.1f),
                size = size
            )
        }
)

/**
 * Creates a text glow effect specifically for text elements
 */
fun Modifier.textGlowEffect(
    radius: Dp,
    color: Color
) = this.then(
    Modifier
        .graphicsLayer {
            this.alpha = 0.99f
        }
        .drawBehind {
            // Draw multiple layers with decreasing opacity
            val steps = 3
            for (i in 0 until steps) {
                val currentRadius = radius.toPx() * (i + 1) / steps
                val currentAlpha = 0.3f * (steps - i) / steps

                drawRect(
                    color = color.copy(alpha = currentAlpha),
                    topLeft = Offset(-currentRadius, -currentRadius),
                    size = Size(
                        width = size.width + currentRadius * 2,
                        height = size.height + currentRadius * 2
                    )
                )
            }
        }
)
