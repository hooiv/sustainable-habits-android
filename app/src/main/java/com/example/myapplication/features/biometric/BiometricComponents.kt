package com.example.myapplication.features.biometric

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.BiometricReading
import com.example.myapplication.data.model.BiometricTrend
import com.example.myapplication.data.model.BiometricType
import com.example.myapplication.ui.animation.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

/**
 * Object containing biometric components for easier access
 */
object BiometricComponents {

    /**
     * A component that displays a list of biometric readings
     */
    @Composable
    fun BiometricReadingsList(
        readings: List<BiometricReading>,
        modifier: Modifier = Modifier,
        onReadingClick: (BiometricReading) -> Unit = {}
    ) {
        Column(
            modifier = modifier
        ) {
            Text(
                text = "Biometric Readings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (readings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No biometric readings available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(readings) { reading ->
                        BiometricReadingCard(
                            reading = reading,
                            onClick = { onReadingClick(reading) }
                        )
                    }
                }
            }
        }
    }
}



/**
 * A component that displays a heart rate monitor with animation
 */
@Composable
fun HeartRateMonitor(
    currentHeartRate: Int,
    historyData: List<Pair<Date, Int>> = emptyList(),
    modifier: Modifier = Modifier,
    onMeasure: () -> Unit = {}
) {
    var isAnimating by remember { mutableStateOf(false) }
    var pulseScale by remember { mutableStateOf(1f) }
    val coroutineScope = rememberCoroutineScope()

    // Heart beat animation
    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            val heartRateMs = 60000 / currentHeartRate.coerceAtLeast(1)

            while (isAnimating) {
                // Pulse animation
                pulseScale = 1f
                delay(50)
                pulseScale = 1.3f
                delay(100)
                pulseScale = 1f

                // Wait for next heartbeat
                delay(heartRateMs.toLong() - 150)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Heart Rate Monitor",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Heart rate display
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 4.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Pulsing heart icon
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Heart Rate",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(80.dp)
                    .scale(pulseScale)
            )

            // Glow effect
            if (isAnimating) {
                // Get color outside of Canvas
                val primaryColor = MaterialTheme.colorScheme.primary

                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(0.3f)
                ) {
                    drawCircle(
                        color = primaryColor,
                        radius = size.minDimension / 2 * pulseScale,
                        style = Stroke(width = 20f * pulseScale)
                    )
                }
            }

            // Heart rate value
            Text(
                text = currentHeartRate.toString(),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )

            Text(
                text = "BPM",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Heart rate graph
        if (historyData.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                // Create text measurer outside of Canvas
                val textMeasurer = rememberTextMeasurer()

                // Get colors outside of Canvas
                val primaryColor = MaterialTheme.colorScheme.primary
                val onSurfaceColor = MaterialTheme.colorScheme.onSurface

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val padding = 16f

                    // Find min and max values
                    val minValue = historyData.minOf { it.second }.coerceAtMost(60)
                    val maxValue = historyData.maxOf { it.second }.coerceAtLeast(100)
                    val valueRange = (maxValue - minValue).coerceAtLeast(1)

                    // Draw grid lines
                    val gridColor = Color.Gray.copy(alpha = 0.2f)
                    val gridLines = 4

                    for (i in 0..gridLines) {
                        val y = padding + (height - 2 * padding) * i / gridLines

                        drawLine(
                            color = gridColor,
                            start = Offset(padding, y),
                            end = Offset(width - padding, y),
                            strokeWidth = 1f
                        )

                        // Draw value labels
                        val value = maxValue - (valueRange * i / gridLines)
                        // Use pre-created text measurer
                        drawText(
                            textMeasurer = textMeasurer,
                            text = value.toString(),
                            topLeft = Offset(4f, y - 8f),
                            style = TextStyle(
                                color = onSurfaceColor.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        )
                    }

                    // Draw heart rate line
                    if (historyData.size > 1) {
                        val path = Path()
                        val pointCount = historyData.size

                        historyData.forEachIndexed { index, (_, heartRate) ->
                            val x = padding + (width - 2 * padding) * index / (pointCount - 1)
                            val normalizedValue = (heartRate - minValue).toFloat() / valueRange
                            val y = height - padding - (height - 2 * padding) * normalizedValue

                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }

                            // Draw points
                            drawCircle(
                                color = primaryColor,
                                radius = 3f,
                                center = Offset(x, y)
                            )
                        }

                        // Draw line
                        drawPath(
                            path = path,
                            color = primaryColor,
                            style = Stroke(width = 2f)
                        )

                        // Draw area under the line
                        val filledPath = Path().apply {
                            addPath(path)
                            lineTo(padding + (width - 2 * padding), height - padding)
                            lineTo(padding, height - padding)
                            close()
                        }

                        drawPath(
                            path = filledPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.3f),
                                    primaryColor.copy(alpha = 0.0f)
                                )
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    isAnimating = !isAnimating
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAnimating) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (isAnimating) "Stop" else "Start")
            }

            Button(
                onClick = onMeasure
            ) {
                Text("Measure")
            }
        }
    }
}

/**
 * A component that displays a sleep quality visualization
 */
@Composable
fun SleepQualityVisualizer(
    sleepData: Map<String, Float>, // Sleep stage to duration in hours
    sleepScore: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Sleep Quality Analysis",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Sleep score display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sleepScore.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = when {
                        sleepScore >= 90 -> "Excellent"
                        sleepScore >= 80 -> "Very Good"
                        sleepScore >= 70 -> "Good"
                        sleepScore >= 60 -> "Fair"
                        else -> "Poor"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Sleep Quality Score",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // Sleep stages visualization
        if (sleepData.isNotEmpty()) {
            Text(
                text = "Sleep Stages",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Calculate total sleep time
            val totalSleepHours = sleepData.values.sum()

            // Sleep stages bar chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                val width = size.width
                var currentX = 0f

                // Define colors for sleep stages
                val stageColors = mapOf(
                    "Deep" to Color(0xFF3949AB),
                    "Light" to Color(0xFF5E97F6),
                    "REM" to Color(0xFF9575CD),
                    "Awake" to Color(0xFFE57373)
                )

                // Draw bars for each sleep stage
                sleepData.forEach { (stage, hours) ->
                    val proportion = hours / totalSleepHours
                    val barWidth = width * proportion

                    drawRect(
                        color = stageColors[stage] ?: Color.Gray,
                        topLeft = Offset(currentX, 0f),
                        size = Size(barWidth, size.height)
                    )

                    currentX += barWidth
                }
            }

            // Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                sleepData.forEach { (stage, hours) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = when (stage) {
                                        "Deep" -> Color(0xFF3949AB)
                                        "Light" -> Color(0xFF5E97F6)
                                        "REM" -> Color(0xFF9575CD)
                                        "Awake" -> Color(0xFFE57373)
                                        else -> Color.Gray
                                    },
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )

                        Text(
                            text = stage,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Text(
                            text = String.format("%.1f hrs", hours),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Sleep cycle visualization
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Sleep Cycles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Create text measurer outside of Canvas
            val textMeasurer = rememberTextMeasurer()

            // Get colors outside of Canvas
            val primaryColor = MaterialTheme.colorScheme.primary
            val onSurfaceColor = MaterialTheme.colorScheme.onSurface
            val surfaceColor = MaterialTheme.colorScheme.surface
            val outlineColor = MaterialTheme.colorScheme.outline

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(surfaceColor)
                    .border(
                        width = 1.dp,
                        color = outlineColor,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                val width = size.width
                val height = size.height
                val padding = 16f

                // Ensure drawableHeight is not negative
                val drawableHeight = (height - 2 * padding).coerceAtLeast(0f)

                // Draw time labels
                for (hour in 0..8) {
                    val x = padding + (width - 2 * padding) * hour / 8

                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(x, padding),
                        end = Offset(x, height - padding),
                        strokeWidth = 1f
                    )

                    // Use pre-created text measurer
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "${hour + 10}:00",
                        topLeft = Offset(x - 12f, height - padding),
                        style = TextStyle(
                            color = onSurfaceColor.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    )
                }

                // Draw sleep stage labels
                val stages = listOf("Awake", "REM", "Light", "Deep")
                stages.forEachIndexed { index, stage ->
                    // Use drawableHeight for y calculation
                    val y = padding + drawableHeight * index / (stages.size - 1)

                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(padding, y),
                        end = Offset(width - padding, y),
                        strokeWidth = 1f
                    )

                    // Use pre-created text measurer
                    drawText(
                        textMeasurer = textMeasurer,
                        text = stage,
                        topLeft = Offset(4f, y - 8f),
                        style = TextStyle(
                            color = onSurfaceColor.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    )
                }

                // Draw sleep cycle curve
                val path = Path()
                val cycleCount = 5
                val cycleWidth = (width - 2 * padding) / cycleCount

                path.moveTo(padding, padding + drawableHeight) // Start awake, ensure y is within bounds

                for (cycle in 0 until cycleCount) {
                    val cycleStart = padding + cycle * cycleWidth

                    // Falling asleep - ensure y values use drawableHeight and padding
                    path.cubicTo(
                        cycleStart + cycleWidth * 0.1f, padding + drawableHeight - 20f.coerceAtMost(drawableHeight * 0.1f),
                        cycleStart + cycleWidth * 0.2f, padding + drawableHeight * 2 / 3,
                        cycleStart + cycleWidth * 0.3f, padding + drawableHeight * 2 / 3
                    )

                    // Deep sleep
                    path.cubicTo(
                        cycleStart + cycleWidth * 0.4f, padding + drawableHeight, // Deepest point
                        cycleStart + cycleWidth * 0.5f, padding + drawableHeight,
                        cycleStart + cycleWidth * 0.6f, padding + drawableHeight * 2 / 3
                    )

                    // REM sleep
                    path.cubicTo(
                        cycleStart + cycleWidth * 0.7f, padding + drawableHeight * 1 / 3,
                        cycleStart + cycleWidth * 0.8f, padding + drawableHeight * 1 / 3,
                        cycleStart + cycleWidth * 0.9f, padding + drawableHeight * 1 / 3
                    )

                    // Brief awakening
                    if (cycle < cycleCount - 1) {
                        path.cubicTo(
                            cycleStart + cycleWidth * 0.95f, padding, // Awake at the top
                            cycleStart + cycleWidth, padding,
                            cycleStart + cycleWidth, padding + drawableHeight * 0.5f
                        )
                    } else {
                        // Final awakening
                        path.cubicTo(
                            cycleStart + cycleWidth * 0.95f, padding,
                            cycleStart + cycleWidth, padding,
                            cycleStart + cycleWidth, padding // End at the top
                        )
                    }
                }

                // Draw the path
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}

/**
 * A card displaying a single biometric reading
 */
@Composable
fun BiometricReadingCard(
    reading: BiometricReading,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Biometric type icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        color = when (reading.type) {
                            BiometricType.HEART_RATE -> MaterialTheme.colorScheme.error
                            BiometricType.SLEEP_QUALITY -> MaterialTheme.colorScheme.tertiary
                            BiometricType.STRESS_LEVEL -> MaterialTheme.colorScheme.error
                            BiometricType.ENERGY_LEVEL -> MaterialTheme.colorScheme.primary
                            BiometricType.MOOD -> MaterialTheme.colorScheme.tertiary
                            BiometricType.STEPS -> MaterialTheme.colorScheme.primary
                            BiometricType.CALORIES_BURNED -> MaterialTheme.colorScheme.secondary
                            BiometricType.BLOOD_PRESSURE -> MaterialTheme.colorScheme.error
                            BiometricType.BLOOD_OXYGEN -> MaterialTheme.colorScheme.primary
                            BiometricType.BODY_TEMPERATURE -> MaterialTheme.colorScheme.error
                            BiometricType.RESPIRATORY_RATE -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.secondary // For any other types including FOCUS_LEVEL
                        }.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (reading.type) {
                        BiometricType.HEART_RATE -> Icons.Default.Favorite
                        BiometricType.SLEEP_QUALITY -> Icons.Default.Bedtime
                        BiometricType.STRESS_LEVEL -> Icons.Default.Psychology
                        BiometricType.ENERGY_LEVEL -> Icons.Default.BatteryChargingFull
                        BiometricType.MOOD -> Icons.Default.EmojiEmotions
                        BiometricType.STEPS -> Icons.Default.DirectionsWalk
                        BiometricType.CALORIES_BURNED -> Icons.Default.LocalFireDepartment
                        BiometricType.BLOOD_PRESSURE -> Icons.Default.Favorite
                        BiometricType.BLOOD_OXYGEN -> Icons.Default.Air
                        BiometricType.BODY_TEMPERATURE -> Icons.Default.Thermostat
                        BiometricType.RESPIRATORY_RATE -> Icons.Default.Air
                        else -> Icons.Default.Visibility // For any other types including FOCUS_LEVEL
                    },
                    contentDescription = reading.type.name,
                    tint = when (reading.type) {
                        BiometricType.HEART_RATE -> MaterialTheme.colorScheme.error
                        BiometricType.SLEEP_QUALITY -> MaterialTheme.colorScheme.tertiary
                        BiometricType.STRESS_LEVEL -> MaterialTheme.colorScheme.error
                        BiometricType.ENERGY_LEVEL -> MaterialTheme.colorScheme.primary
                        BiometricType.MOOD -> MaterialTheme.colorScheme.tertiary
                        BiometricType.STEPS -> MaterialTheme.colorScheme.primary
                        BiometricType.CALORIES_BURNED -> MaterialTheme.colorScheme.secondary
                        BiometricType.BLOOD_PRESSURE -> MaterialTheme.colorScheme.error
                        BiometricType.BLOOD_OXYGEN -> MaterialTheme.colorScheme.primary
                        BiometricType.BODY_TEMPERATURE -> MaterialTheme.colorScheme.error
                        BiometricType.RESPIRATORY_RATE -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.secondary // For any other types including FOCUS_LEVEL
                    }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Reading details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = when (reading.type) {
                        BiometricType.HEART_RATE -> "Heart Rate"
                        BiometricType.SLEEP_QUALITY -> "Sleep Quality"
                        BiometricType.STRESS_LEVEL -> "Stress Level"
                        BiometricType.ENERGY_LEVEL -> "Energy Level"
                        BiometricType.MOOD -> "Mood"
                        BiometricType.STEPS -> "Steps"
                        BiometricType.CALORIES_BURNED -> "Calories Burned"
                        BiometricType.BLOOD_PRESSURE -> "Blood Pressure"
                        BiometricType.BLOOD_OXYGEN -> "Blood Oxygen"
                        BiometricType.BODY_TEMPERATURE -> "Body Temperature"
                        BiometricType.RESPIRATORY_RATE -> "Respiratory Rate"
                        else -> "Focus Level" // For any other types including FOCUS_LEVEL
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${reading.value.toInt()} ${reading.unit}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Trend indicator
                    Icon(
                        imageVector = when (reading.trend) {
                            BiometricTrend.INCREASING -> Icons.Default.TrendingUp
                            BiometricTrend.DECREASING -> Icons.Default.TrendingDown
                            BiometricTrend.STABLE -> Icons.Default.TrendingFlat
                            BiometricTrend.FLUCTUATING -> Icons.Default.Timeline
                        },
                        contentDescription = reading.trend.name,
                        tint = when (reading.trend) {
                            BiometricTrend.INCREASING -> MaterialTheme.colorScheme.primary
                            BiometricTrend.DECREASING -> MaterialTheme.colorScheme.error
                            BiometricTrend.STABLE -> MaterialTheme.colorScheme.secondary
                            BiometricTrend.FLUCTUATING -> MaterialTheme.colorScheme.tertiary
                        },
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "Normal range: ${reading.normalRange.first.toInt()} - ${reading.normalRange.second.toInt()} ${reading.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // Status indicator
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        color = when {
                            reading.value < reading.normalRange.first -> MaterialTheme.colorScheme.error
                            reading.value > reading.normalRange.second -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
            )
        }
    }
}
