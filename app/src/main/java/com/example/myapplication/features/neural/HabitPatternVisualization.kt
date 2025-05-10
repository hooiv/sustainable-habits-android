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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.HabitCompletion
import com.example.myapplication.data.model.NeuralPrediction
import com.example.myapplication.data.model.PredictionType
import java.util.*

/**
 * Helper function to draw text on Canvas using the native canvas
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawText(
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    text: String,
    topLeft: Offset,
    style: TextStyle
) {
    val paint = Paint().apply {
        color = style.color.toArgb()
        textSize = style.fontSize.toPx()
        if (style.fontWeight == FontWeight.Bold) {
            isFakeBoldText = true
        }
    }

    this.drawContext.canvas.nativeCanvas.drawText(
        text,
        topLeft.x,
        topLeft.y + style.fontSize.toPx(), // Adjust y position to account for baseline
        paint
    )
}

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
            TimeOfDayHeatmapCanvas(completions, predictions)
        }
    }
}

/**
 * Canvas implementation for time of day heatmap
 */
@Composable
private fun TimeOfDayHeatmapCanvas(
    completions: List<HabitCompletion>,
    predictions: List<NeuralPrediction>
) {
    // Create text measurer for drawing text
    val textMeasurer = rememberTextMeasurer()

    // Extract theme colors before Canvas
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

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
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "${hour}:00",
                        topLeft = Offset(x + 4, height - 14),
                        style = TextStyle(
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
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
                color = primaryColor.copy(alpha = alpha),
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
                color = tertiaryColor,
                radius = 8.dp.toPx(),
                center = Offset(optimalX, height * 0.85f)
            )

            // Draw label
            drawText(
                textMeasurer = textMeasurer,
                text = "Optimal",
                topLeft = Offset(optimalX - 20, height - 10),
                style = TextStyle(
                    color = tertiaryColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )
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
            DayOfWeekDistributionCanvas(completions)
        }
    }
}

/**
 * Canvas implementation for day of week distribution
 */
@Composable
private fun DayOfWeekDistributionCanvas(
    completions: List<HabitCompletion>
) {
    // Create text measurer for drawing text
    val textMeasurer = rememberTextMeasurer()

    // Extract theme colors before Canvas
    val secondaryColor = MaterialTheme.colorScheme.secondary

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
                color = secondaryColor,
                topLeft = Offset(day * dayWidth + dayWidth * 0.1f, height * 0.7f - barHeight),
                size = Size(dayWidth * 0.8f, barHeight)
            )

            // Draw day label
            drawText(
                textMeasurer = textMeasurer,
                text = dayNames[day],
                topLeft = Offset(day * dayWidth + dayWidth / 2 - 10, height - 14),
                style = TextStyle(
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            )

            // Draw count
            if (dayCounts[day] > 0) {
                drawText(
                    textMeasurer = textMeasurer,
                    text = "${dayCounts[day]}",
                    topLeft = Offset(day * dayWidth + dayWidth / 2 - 5, height * 0.7f - barHeight - 14),
                    style = TextStyle(
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                )
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

                // Use a separate composable for the streak visualization
                StreakVisualizationCanvas(probability)

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

/**
 * Canvas implementation for streak visualization
 */
@Composable
private fun StreakVisualizationCanvas(probability: Float) {
    // Create text measurer for drawing text
    val textMeasurer = rememberTextMeasurer()

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
        drawText(
            textMeasurer = textMeasurer,
            text = "${(probability * 100).toInt()}%",
            topLeft = Offset(width * probability + 8, height * 0.5f - 6),
            style = TextStyle(
                color = Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}