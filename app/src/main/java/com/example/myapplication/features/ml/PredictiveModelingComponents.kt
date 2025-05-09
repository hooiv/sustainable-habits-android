package com.example.myapplication.features.ml

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.Habit
import com.example.myapplication.ui.animation.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

/**
 * Data class representing a habit prediction
 */
data class HabitPrediction(
    val id: String = UUID.randomUUID().toString(),
    val habitId: String,
    val habitName: String,
    val predictionType: PredictionType,
    val probability: Float, // 0.0 to 1.0
    val timeframe: String,
    val factors: List<PredictionFactor>,
    val timestamp: Date = Date(),
    val confidenceInterval: Pair<Float, Float> = Pair(0f, 0f)
)

/**
 * Data class representing a factor influencing a prediction
 */
data class PredictionFactor(
    val name: String,
    val impact: Float, // -1.0 to 1.0 (negative to positive impact)
    val confidence: Float // 0.0 to 1.0
)

/**
 * Enum for different types of predictions
 */
enum class PredictionType {
    COMPLETION_LIKELIHOOD,
    STREAK_CONTINUATION,
    HABIT_FORMATION,
    HABIT_ABANDONMENT,
    OPTIMAL_TIME,
    DIFFICULTY_CHANGE
}

/**
 * A component that displays a machine learning model training visualization
 */
@Composable
fun MLModelTrainingVisualizer(
    modifier: Modifier = Modifier,
    onTrainingComplete: (Float) -> Unit = {}
) {
    var trainingProgress by remember { mutableStateOf(0f) }
    var trainingSpeed by remember { mutableStateOf(1f) }
    var trainingAccuracy by remember { mutableStateOf(0f) }
    var trainingLoss by remember { mutableStateOf(1f) }
    var epoch by remember { mutableStateOf(0) }
    var isTraining by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    // Data points for loss and accuracy curves
    val maxDataPoints = 100
    val lossData = remember { mutableStateListOf<Float>() }
    val accuracyData = remember { mutableStateListOf<Float>() }
    
    // Training simulation
    LaunchedEffect(isTraining) {
        if (isTraining) {
            // Reset data
            trainingProgress = 0f
            trainingAccuracy = 0f
            trainingLoss = 1f
            epoch = 0
            lossData.clear()
            accuracyData.clear()
            
            while (isTraining && trainingProgress < 1f) {
                delay((100 / trainingSpeed).toLong())
                
                // Update progress
                trainingProgress += 0.005f * trainingSpeed
                
                // Simulate epoch completion
                if (trainingProgress >= (epoch + 1) * 0.1f && epoch < 10) {
                    epoch++
                    
                    // Simulate accuracy improvement with diminishing returns
                    val baseAccuracyGain = 0.1f * (1f - trainingAccuracy)
                    val randomVariation = (Random().nextFloat() - 0.5f) * 0.05f
                    trainingAccuracy = (trainingAccuracy + baseAccuracyGain + randomVariation).coerceIn(0f, 0.99f)
                    
                    // Simulate loss reduction with diminishing returns
                    val baseLossReduction = 0.2f * trainingLoss
                    val lossRandomVariation = (Random().nextFloat() - 0.5f) * 0.05f
                    trainingLoss = (trainingLoss - baseLossReduction + lossRandomVariation).coerceIn(0.01f, 1f)
                    
                    // Add data points
                    lossData.add(trainingLoss)
                    accuracyData.add(trainingAccuracy)
                    
                    // Trim data if needed
                    if (lossData.size > maxDataPoints) {
                        lossData.removeAt(0)
                    }
                    if (accuracyData.size > maxDataPoints) {
                        accuracyData.removeAt(0)
                    }
                }
                
                // Complete training
                if (trainingProgress >= 1f) {
                    isTraining = false
                    onTrainingComplete(trainingAccuracy)
                }
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Training controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "ML Model Training",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                IconButton(
                    onClick = { trainingSpeed = (trainingSpeed * 0.5f).coerceAtLeast(0.5f) },
                    enabled = isTraining
                ) {
                    Icon(
                        imageVector = Icons.Default.SlowMotionVideo,
                        contentDescription = "Slow Down"
                    )
                }
                
                IconButton(
                    onClick = { trainingSpeed = (trainingSpeed * 2f).coerceAtMost(4f) },
                    enabled = isTraining
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Speed Up"
                    )
                }
                
                Button(
                    onClick = { isTraining = !isTraining },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTraining) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (isTraining) "Stop" else "Train Model")
                }
            }
        }
        
        // Training metrics
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Epoch",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = epoch.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Accuracy",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "${(trainingAccuracy * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = lerp(
                        MaterialTheme.colorScheme.error,
                        MaterialTheme.colorScheme.primary,
                        trainingAccuracy
                    )
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Loss",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = String.format("%.3f", trainingLoss),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = lerp(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.error,
                        trainingLoss
                    )
                )
            }
        }
        
        // Progress bar
        LinearProgressIndicator(
            progress = trainingProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Training visualization
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            // Draw loss and accuracy curves
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val padding = 16f
                
                // Draw grid
                val gridColor = Color.Gray.copy(alpha = 0.2f)
                val gridLines = 5
                
                for (i in 0..gridLines) {
                    val y = padding + (height - 2 * padding) * i / gridLines
                    
                    drawLine(
                        color = gridColor,
                        start = Offset(padding, y),
                        end = Offset(width - padding, y),
                        strokeWidth = 1f
                    )
                }
                
                for (i in 0..gridLines) {
                    val x = padding + (width - 2 * padding) * i / gridLines
                    
                    drawLine(
                        color = gridColor,
                        start = Offset(x, padding),
                        end = Offset(x, height - padding),
                        strokeWidth = 1f
                    )
                }
                
                // Draw loss curve
                if (lossData.size > 1) {
                    val lossPath = Path()
                    val lossPointCount = lossData.size
                    
                    lossData.forEachIndexed { index, loss ->
                        val x = padding + (width - 2 * padding) * index / (lossPointCount - 1)
                        val y = padding + (height - 2 * padding) * loss
                        
                        if (index == 0) {
                            lossPath.moveTo(x, y)
                        } else {
                            lossPath.lineTo(x, y)
                        }
                    }
                    
                    drawPath(
                        path = lossPath,
                        color = MaterialTheme.colorScheme.error,
                        style = Stroke(width = 3f)
                    )
                }
                
                // Draw accuracy curve
                if (accuracyData.size > 1) {
                    val accuracyPath = Path()
                    val accuracyPointCount = accuracyData.size
                    
                    accuracyData.forEachIndexed { index, accuracy ->
                        val x = padding + (width - 2 * padding) * index / (accuracyPointCount - 1)
                        val y = height - padding - (height - 2 * padding) * accuracy
                        
                        if (index == 0) {
                            accuracyPath.moveTo(x, y)
                        } else {
                            accuracyPath.lineTo(x, y)
                        }
                    }
                    
                    drawPath(
                        path = accuracyPath,
                        color = MaterialTheme.colorScheme.primary,
                        style = Stroke(width = 3f)
                    )
                }
                
                // Draw legend
                drawRect(
                    color = MaterialTheme.colorScheme.error,
                    topLeft = Offset(width - 100f, 20f),
                    size = Size(20f, 10f)
                )
                
                drawText(
                    textMeasurer = rememberTextMeasurer(),
                    text = "Loss",
                    topLeft = Offset(width - 70f, 20f),
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                )
                
                drawRect(
                    color = MaterialTheme.colorScheme.primary,
                    topLeft = Offset(width - 100f, 40f),
                    size = Size(20f, 10f)
                )
                
                drawText(
                    textMeasurer = rememberTextMeasurer(),
                    text = "Accuracy",
                    topLeft = Offset(width - 70f, 40f),
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                )
            }
            
            // Neural network visualization during training
            if (isTraining) {
                val infiniteTransition = rememberInfiniteTransition(label = "neuralViz")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = AnimeEasing.EaseInOutQuad),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseScale"
                )
                
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.3f)
                ) {
                    val width = size.width
                    val height = size.height
                    
                    // Draw neural network layers
                    val layers = 4
                    val nodesPerLayer = intArrayOf(5, 8, 8, 3)
                    
                    for (layer in 0 until layers) {
                        val layerX = width * (layer + 1) / (layers + 1)
                        
                        for (node in 0 until nodesPerLayer[layer]) {
                            val nodeY = height * (node + 1) / (nodesPerLayer[layer] + 1)
                            
                            // Draw node
                            drawCircle(
                                color = MaterialTheme.colorScheme.primary,
                                radius = 5f * pulseScale,
                                center = Offset(layerX, nodeY)
                            )
                            
                            // Draw connections to previous layer
                            if (layer > 0) {
                                for (prevNode in 0 until nodesPerLayer[layer - 1]) {
                                    val prevLayerX = width * layer / (layers + 1)
                                    val prevNodeY = height * (prevNode + 1) / (nodesPerLayer[layer - 1] + 1)
                                    
                                    drawLine(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        start = Offset(prevLayerX, prevNodeY),
                                        end = Offset(layerX, nodeY),
                                        strokeWidth = 1f
                                    )
                                }
                            }
                        }
                    }
                    
                    // Draw data flow animation
                    for (i in 0 until 3) {
                        val animProgress = (trainingProgress * 3 + i / 3f) % 1f
                        
                        for (layer in 0 until layers - 1) {
                            val startX = width * (layer + 1) / (layers + 1)
                            val endX = width * (layer + 2) / (layers + 1)
                            val x = startX + (endX - startX) * animProgress
                            
                            // Random position between nodes
                            val startNodeIdx = Random().nextInt(nodesPerLayer[layer])
                            val endNodeIdx = Random().nextInt(nodesPerLayer[layer + 1])
                            
                            val startY = height * (startNodeIdx + 1) / (nodesPerLayer[layer] + 1)
                            val endY = height * (endNodeIdx + 1) / (nodesPerLayer[layer + 1] + 1)
                            val y = startY + (endY - startY) * animProgress
                            
                            drawCircle(
                                color = MaterialTheme.colorScheme.tertiary,
                                radius = 3f,
                                center = Offset(x, y)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * A component that displays habit predictions with confidence intervals
 */
@Composable
fun HabitPredictionList(
    predictions: List<HabitPrediction>,
    modifier: Modifier = Modifier,
    onPredictionClick: (HabitPrediction) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "ML-Powered Habit Predictions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (predictions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Train the model to generate predictions",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(predictions) { prediction ->
                    PredictionCard(
                        prediction = prediction,
                        onClick = { onPredictionClick(prediction) }
                    )
                }
            }
        }
    }
}

/**
 * A card displaying a single habit prediction
 */
@Composable
fun PredictionCard(
    prediction: HabitPrediction,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                expanded = !expanded
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prediction type icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            color = when (prediction.predictionType) {
                                PredictionType.COMPLETION_LIKELIHOOD -> MaterialTheme.colorScheme.primary
                                PredictionType.STREAK_CONTINUATION -> MaterialTheme.colorScheme.secondary
                                PredictionType.HABIT_FORMATION -> MaterialTheme.colorScheme.tertiary
                                PredictionType.HABIT_ABANDONMENT -> MaterialTheme.colorScheme.error
                                PredictionType.OPTIMAL_TIME -> MaterialTheme.colorScheme.primaryContainer
                                PredictionType.DIFFICULTY_CHANGE -> MaterialTheme.colorScheme.secondaryContainer
                            }.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (prediction.predictionType) {
                            PredictionType.COMPLETION_LIKELIHOOD -> Icons.Default.CheckCircle
                            PredictionType.STREAK_CONTINUATION -> Icons.Default.Timeline
                            PredictionType.HABIT_FORMATION -> Icons.Default.AddCircle
                            PredictionType.HABIT_ABANDONMENT -> Icons.Default.RemoveCircle
                            PredictionType.OPTIMAL_TIME -> Icons.Default.Schedule
                            PredictionType.DIFFICULTY_CHANGE -> Icons.Default.TrendingUp
                        },
                        contentDescription = prediction.predictionType.name,
                        tint = when (prediction.predictionType) {
                            PredictionType.COMPLETION_LIKELIHOOD -> MaterialTheme.colorScheme.primary
                            PredictionType.STREAK_CONTINUATION -> MaterialTheme.colorScheme.secondary
                            PredictionType.HABIT_FORMATION -> MaterialTheme.colorScheme.tertiary
                            PredictionType.HABIT_ABANDONMENT -> MaterialTheme.colorScheme.error
                            PredictionType.OPTIMAL_TIME -> MaterialTheme.colorScheme.primary
                            PredictionType.DIFFICULTY_CHANGE -> MaterialTheme.colorScheme.secondary
                        }
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Prediction details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = prediction.habitName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = when (prediction.predictionType) {
                            PredictionType.COMPLETION_LIKELIHOOD -> "Completion Likelihood"
                            PredictionType.STREAK_CONTINUATION -> "Streak Continuation"
                            PredictionType.HABIT_FORMATION -> "Habit Formation"
                            PredictionType.HABIT_ABANDONMENT -> "Habit Abandonment Risk"
                            PredictionType.OPTIMAL_TIME -> "Optimal Time"
                            PredictionType.DIFFICULTY_CHANGE -> "Difficulty Change"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Text(
                        text = "Timeframe: ${prediction.timeframe}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                
                // Probability indicator
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = prediction.probability,
                        modifier = Modifier.fillMaxSize(),
                        color = when {
                            prediction.probability > 0.7f -> MaterialTheme.colorScheme.primary
                            prediction.probability > 0.4f -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.error
                        },
                        strokeWidth = 4.dp
                    )
                    
                    Text(
                        text = "${(prediction.probability * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Expanded content
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Confidence interval
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Confidence Interval:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "${(prediction.confidenceInterval.first * 100).toInt()}% - ${(prediction.confidenceInterval.second * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Factors
                Text(
                    text = "Contributing Factors:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                prediction.factors.forEach { factor ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Impact indicator
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(16.dp)
                                .background(
                                    color = if (factor.impact > 0) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    }
                                )
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Factor name
                        Text(
                            text = factor.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Impact value
                        Text(
                            text = "${if (factor.impact > 0) "+" else ""}${(factor.impact * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (factor.impact > 0) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
                
                // Timestamp
                Text(
                    text = "Generated: ${SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(prediction.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
