package com.example.myapplication.core.ui.components

import android.content.Context
import android.os.PowerManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.core.ui.R

/**
 * Button with animated gradient background inspired by anime.js effects
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradientColors: List<Color> = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary
    ),
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    val context = LocalContext.current
    val powerManager = remember(context) { context.getSystemService(Context.POWER_SERVICE) as PowerManager }
    val isPowerSaveMode = powerManager.isPowerSaveMode

    // Add animation to the gradient
    val infiniteTransition = rememberInfiniteTransition(label = "buttonGradient")
    val offset by infiniteTransition.animateFloat(
        initialValue = -1000f,
        targetValue = if (isPowerSaveMode) -1000f else 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 10000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradientAnimate"
    )
    
    // Apply the button styling
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .drawBehind {
                val animatedBrush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(offset, 0f),
                    end = Offset(offset + 1000, 1000f)
                )
                drawRect(
                    brush = animatedBrush,
                    alpha = if (enabled) 1f else 0.5f
                )
            }
            .clickable(enabled = enabled) { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor.copy(alpha = if (enabled) 1f else 0.5f),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Button with Jupiter to Fi gradient and accent color for consistent branding
 */
@Composable
fun JupiterGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GradientButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        gradientColors = listOf(
            colorResource(R.color.brand_gradient_start),
            colorResource(R.color.brand_gradient_end)
        ),
        contentColor = colorResource(R.color.brand_accent)
    )
}

/**
 * Shimmer loading placeholder for list items
 */
@Composable
fun ShimmerItem(
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    )
    
    val context = LocalContext.current
    val powerManager = remember(context) { context.getSystemService(Context.POWER_SERVICE) as PowerManager }
    val isPowerSaveMode = powerManager.isPowerSaveMode

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPowerSaveMode) 0f else 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    
    Column(modifier = modifier) {
        // Simulate a card with content
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .drawBehind {
                    val brush = Brush.linearGradient(
                        colors = shimmerColors,
                        start = Offset(translateAnimation.value - 1000f, 0f),
                        end = Offset(translateAnimation.value, 1000f)
                    )
                    drawRect(brush = brush)
                }
        )
    }
}
