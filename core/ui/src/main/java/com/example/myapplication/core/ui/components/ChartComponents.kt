package com.example.myapplication.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * An animated circular progress indicator with percentage text
 */
@Composable
fun AnimatedCircularProgress(
    currentValue: Int,
    maxValue: Int,
    modifier: Modifier = Modifier,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    strokeWidth: Dp = 24.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (maxValue > 0) currentValue.toFloat() / maxValue.toFloat() else 0f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "progress"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Background circle
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = backgroundColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
                size = Size(size.width, size.height)
            )
        }
        
        // Progress arc
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = indicatorColor,
                startAngle = -90f, // Start from top
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
                size = Size(size.width, size.height)
            )
        }
        
        // Text display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Completion Rate",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * A circular progress indicator with glowing effect
 */
@Composable
fun GlowingCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    glowColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
    progressColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
    strokeWidth: Dp = 12.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "glowProgress"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Glow effect (larger circle behind)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val glowRadius = (strokeWidth * 2).toPx()
            
            // Draw background
            drawCircle(
                color = backgroundColor,
                radius = size.minDimension / 2 - strokeWidth.toPx() / 2,
                style = Stroke(width = strokeWidth.toPx())
            )
            
            // Draw progress with glow
            drawArc(
                color = glowColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = glowRadius, cap = StrokeCap.Round),
                size = Size(size.width, size.height)
            )
            
            // Draw main progress
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
                size = Size(size.width, size.height)
            )
        }
    }
}

/**
 * A custom chart component to display completion statistics
 */
@Composable
fun CompletionPieChart(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    // Animated progress
    val animatedProgress by animateFloatAsState(
        targetValue = if (total > 0) completed.toFloat() / total.toFloat() else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "pieProgress"
    )
    
    // Fetch colors from MaterialTheme in the Composable context
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Pie chart visualization
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background circle
            drawCircle(
                color = surfaceVariantColor, // Use pre-fetched color
                radius = size.minDimension / 2.5f
            )
            
            // Progress arc - draw only if there are habits
            if (total > 0) {
                val sweepAngle = 360f * animatedProgress
                
                drawArc(
                    color = primaryColor, // Use pre-fetched color
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    size = Size(size.minDimension / 1.25f, size.minDimension / 1.25f),
                    topLeft = Offset(
                        (size.width - size.minDimension / 1.25f) / 2,
                        (size.height - size.minDimension / 1.25f) / 2
                    )
                )
            }
        }
        
        // Text display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (total > 0) "${(animatedProgress * 100).toInt()}%" else "0%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$completed of $total",
                style = MaterialTheme.typography.bodyMedium,
                color = onSurfaceColor.copy(alpha = 0.7f) // Use pre-fetched color
            )
            Text(
                text = "Completed",
                style = MaterialTheme.typography.bodySmall,
                color = onSurfaceColor.copy(alpha = 0.5f), // Use pre-fetched color
                textAlign = TextAlign.Center
            )
        }
    }
}
