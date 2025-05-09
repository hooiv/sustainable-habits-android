package com.example.myapplication.features.neural

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.HabitCompletion
import com.example.myapplication.data.model.NeuralPrediction
import com.example.myapplication.data.model.PredictionType
import java.util.*

/**
 * Visualizes habit patterns and neural network predictions
 */
@Composable
fun HabitPatternVisualization(
    completions: List<HabitCompletion>,
    predictions: List<NeuralPrediction>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Habit Pattern Analysis",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Time of day heatmap
        TimeOfDayHeatmap(
            completions = completions,
            predictions = predictions,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(8.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Day of week distribution
        DayOfWeekDistribution(
            completions = completions,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(8.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Streak visualization
        StreakVisualization(
            completions = completions,
            predictions = predictions,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(8.dp)
        )
    }
}

/**
 * Visualizes habit completions by time of day
 */
@Composable
fun TimeOfDayHeatmap(
    completions: List<HabitCompletion>,
    predictions: List<NeuralPrediction>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Time of Day Pattern",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val hourWidth = width / 24
                
                // Draw hour markers
                for (hour in 0..24) {
                    val x = hour * hourWidth
                    
                    // Draw vertical lines for each 6 hours
                    if (hour % 6 == 0) {
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.5f),
                            start = Offset(x, 0f),
                            end = Offset(x, height),
                            strokeWidth = 1f
                        )
                        
                        // Draw hour labels
                        if (hour < 24) {
                            drawContext.canvas.nativeCanvas.drawText(
                                "${hour}:00",
                                x + 4,
                                height - 4,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.GRAY
                                    textSize = 10.dp.toPx()
                                }
                            )
                        }
                    }
                }
                
                // Count completions by hour
                val hourCounts = IntArray(24) { 0 }
                completions.forEach { completion ->
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = completion.completionDate
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    hourCounts[hour]++
                }
                
                // Find max count for normalization
                val maxCount = hourCounts.maxOrNull() ?: 1
                
                // Draw completion density
                for (hour in 0 until 24) {
                    val normalizedCount = hourCounts[hour].toFloat() / maxCount
                    val alpha = (normalizedCount * 0.8f + 0.2f).coerceIn(0f, 1f)
                    
                    drawRect(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                        topLeft = Offset(hour * hourWidth, 0f),
                        size = Size(hourWidth, height * 0.7f)
                    )
                }
                
                // Draw optimal time prediction if available
                val optimalTimePrediction = predictions
                    .filter { it.predictionType == PredictionType.OPTIMAL_TIME }
                    .maxByOrNull { it.timestamp }
                
                optimalTimePrediction?.let { prediction ->
                    val optimalHour = (prediction.probability * 24).toInt().coerceIn(0, 23)
                    val optimalX = optimalHour * hourWidth + hourWidth / 2
                    
                    // Draw marker for optimal time
                    drawCircle(
                        color = MaterialTheme.colorScheme.tertiary,
                        radius = 8.dp.toPx(),
                        center = Offset(optimalX, height * 0.85f)
                    )
                    
                    // Draw label
                    drawContext.canvas.nativeCanvas.drawText(
                        "Optimal",
                        optimalX - 20,
                        height,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.rgb(
                                (MaterialTheme.colorScheme.tertiary.red * 255).toInt(),
                                (MaterialTheme.colorScheme.tertiary.green * 255).toInt(),
                                (MaterialTheme.colorScheme.tertiary.blue * 255).toInt()
                            )
                            textSize = 10.dp.toPx()
                            isFakeBoldText = true
                        }
                    )
                }
            }
        }
    }
}

/**
 * Visualizes habit completions by day of week
 */
@Composable
fun DayOfWeekDistribution(
    completions: List<HabitCompletion>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Day of Week Distribution",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val dayWidth = width / 7
                
                // Day names
                val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                
                // Count completions by day of week
                val dayCounts = IntArray(7) { 0 }
                completions.forEach { completion ->
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = completion.completionDate
                    // Convert to 0-6 (Monday-Sunday)
                    val dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
                    dayCounts[dayOfWeek]++
                }
                
                // Find max count for normalization
                val maxCount = dayCounts.maxOrNull() ?: 1
                
                // Draw day bars
                for (day in 0 until 7) {
                    val normalizedCount = dayCounts[day].toFloat() / maxCount
                    val barHeight = height * 0.7f * normalizedCount
                    
                    // Draw bar
                    drawRect(
                        color = MaterialTheme.colorScheme.secondary,
                        topLeft = Offset(day * dayWidth + dayWidth * 0.1f, height * 0.7f - barHeight),
                        size = Size(dayWidth * 0.8f, barHeight)
                    )
                    
                    // Draw day label
                    drawContext.canvas.nativeCanvas.drawText(
                        dayNames[day],
                        day * dayWidth + dayWidth / 2 - 10,
                        height - 4,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 10.dp.toPx()
                        }
                    )
                    
                    // Draw count
                    if (dayCounts[day] > 0) {
                        drawContext.canvas.nativeCanvas.drawText(
                            "${dayCounts[day]}",
                            day * dayWidth + dayWidth / 2 - 5,
                            height * 0.7f - barHeight - 4,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.GRAY
                                textSize = 10.dp.toPx()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Visualizes streak patterns and predictions
 */
@Composable
fun StreakVisualization(
    completions: List<HabitCompletion>,
    predictions: List<NeuralPrediction>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Streak Prediction",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Get streak continuation prediction
            val streakPrediction = predictions
                .filter { it.predictionType == PredictionType.STREAK_CONTINUATION }
                .maxByOrNull { it.timestamp }
            
            if (streakPrediction != null) {
                val probability = streakPrediction.probability
                
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    
                    // Draw progress bar
                    drawRect(
                        color = Color.Gray.copy(alpha = 0.3f),
                        topLeft = Offset(0f, height * 0.4f),
                        size = Size(width, height * 0.2f)
                    )
                    
                    drawRect(
                        color = when {
                            probability > 0.7f -> Color(0xFF4CAF50) // Green
                            probability > 0.4f -> Color(0xFFFFC107) // Yellow
                            else -> Color(0xFFF44336) // Red
                        },
                        topLeft = Offset(0f, height * 0.4f),
                        size = Size(width * probability, height * 0.2f)
                    )
                    
                    // Draw percentage
                    drawContext.canvas.nativeCanvas.drawText(
                        "${(probability * 100).toInt()}%",
                        width * probability + 8,
                        height * 0.5f + 4,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 12.dp.toPx()
                            isFakeBoldText = true
                        }
                    )
                }
                
                // Add text description
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = when {
                            probability > 0.7f -> "High chance of continuing your streak!"
                            probability > 0.4f -> "Moderate chance of continuing your streak"
                            else -> "You may need extra motivation to continue your streak"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                // No prediction available
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No streak prediction available yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
