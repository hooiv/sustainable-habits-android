package com.example.myapplication.features.ml

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

/**
 * A visualization of ML model training
 */
@Composable
fun MLModelTrainingVisualizer(
    modifier: Modifier = Modifier
) {
    // State for animation
    var isTraining by remember { mutableStateOf(false) }
    var epoch by remember { mutableStateOf(0) }
    var loss by remember { mutableStateOf(0.8f) }
    var accuracy by remember { mutableStateOf(0.2f) }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Start training animation
    LaunchedEffect(Unit) {
        isTraining = true
        coroutineScope.launch {
            repeat(20) {
                epoch = it
                loss = max(0.1f, loss - Random.nextFloat() * 0.1f)
                accuracy = min(0.95f, accuracy + Random.nextFloat() * 0.1f)
                delay(500)
            }
            isTraining = false
        }
    }
    
    // Animation for neural network nodes
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(isTraining) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }
    
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Text(
            text = "ML Model Training",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Epoch: $epoch",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Loss: ${String.format("%.2f", loss)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Accuracy: ${String.format("%.1f", accuracy * 100)}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (isTraining) "Training..." else "Training Complete",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isTraining) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                )
            }
        }
        
        // Neural network visualization
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val width = size.width
            val height = size.height
            
            // Define node positions
            val inputLayer = List(4) { i ->
                Offset(width * 0.2f, height * (0.2f + i * 0.2f))
            }
            
            val hiddenLayer = List(6) { i ->
                Offset(width * 0.5f, height * (0.1f + i * 0.15f))
            }
            
            val outputLayer = List(3) { i ->
                Offset(width * 0.8f, height * (0.25f + i * 0.25f))
            }
            
            // Draw connections
            for (input in inputLayer) {
                for (hidden in hiddenLayer) {
                    // Calculate animation offset for this connection
                    val animOffset = (input.x + input.y + hidden.x + hidden.y) % 100 / 100f
                    val animPosition = (animatedProgress.value + animOffset) % 1f
                    
                    // Interpolate position along the line
                    val x = input.x + (hidden.x - input.x) * animPosition
                    val y = input.y + (hidden.y - input.y) * animPosition
                    
                    // Draw connection line
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = input,
                        end = hidden,
                        strokeWidth = 1f
                    )
                    
                    // Draw moving particle
                    drawCircle(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        radius = 3f,
                        center = Offset(x, y)
                    )
                }
            }
            
            for (hidden in hiddenLayer) {
                for (output in outputLayer) {
                    // Calculate animation offset for this connection
                    val animOffset = (hidden.x + hidden.y + output.x + output.y) % 100 / 100f
                    val animPosition = (animatedProgress.value + animOffset) % 1f
                    
                    // Interpolate position along the line
                    val x = hidden.x + (output.x - hidden.x) * animPosition
                    val y = hidden.y + (output.y - hidden.y) * animPosition
                    
                    // Draw connection line
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = hidden,
                        end = output,
                        strokeWidth = 1f
                    )
                    
                    // Draw moving particle
                    drawCircle(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                        radius = 3f,
                        center = Offset(x, y)
                    )
                }
            }
            
            // Draw nodes
            for (node in inputLayer) {
                drawCircle(
                    color = MaterialTheme.colorScheme.primary,
                    radius = 8f,
                    center = node
                )
            }
            
            for (node in hiddenLayer) {
                drawCircle(
                    color = MaterialTheme.colorScheme.secondary,
                    radius = 8f,
                    center = node
                )
            }
            
            for (node in outputLayer) {
                drawCircle(
                    color = MaterialTheme.colorScheme.tertiary,
                    radius = 8f,
                    center = node
                )
            }
        }
    }
}
