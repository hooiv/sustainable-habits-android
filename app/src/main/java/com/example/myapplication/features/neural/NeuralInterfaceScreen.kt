package com.example.myapplication.features.neural

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.model.Habit
import com.example.myapplication.data.model.NeuralPrediction
import com.example.myapplication.data.model.PredictionType
import java.util.Date

/**
 * Screen for the Neural Interface feature
 */
@Composable
fun NeuralInterfaceScreen(
    habit: Habit,
    onBackClick: () -> Unit,
    viewModel: NeuralInterfaceViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Text(
                text = "Neural Interface",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Habit info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Streak",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = habit.streak.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            text = "Frequency",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = habit.frequency.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = "${habit.goalProgress}/${habit.goal}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Info card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(end = 16.dp)
                )

                Text(
                    text = "This neural network visualizes how your habit forms in your brain. Tap nodes to activate them, drag to move, and double-tap to create connections.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // Neural interface and predictions
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            // Tabs for Neural Network, Predictions, Training, Patterns, and Advanced
            var selectedTab by remember { mutableStateOf(0) }
            val tabs = listOf("Neural Network", "Predictions", "Training", "Patterns", "Advanced")

            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content based on selected tab
            when (selectedTab) {
                0 -> {
                    // Neural Network tab
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        // Show loading indicator
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        // Show error message
                        errorMessage?.let { error ->
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(16.dp)
                            )
                        }

                        // Show neural interface
                        NeuralInterfaceSimulation(
                            modifier = Modifier.fillMaxSize(),
                            viewModel = viewModel,
                            habitId = habit.id
                        )
                    }
                }
                1 -> {
                    // Predictions tab
                    val predictions by viewModel.predictions.collectAsState()
                    val networkStatus by viewModel.networkStatus.collectAsState()

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        if (predictions.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No predictions available yet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { viewModel.trainNetwork() },
                                    enabled = networkStatus != NetworkStatus.Training
                                ) {
                                    Text("Train Network to Generate Predictions")
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // Group predictions by type
                                val predictionsByType = predictions.groupBy { it.predictionType }

                                predictionsByType.forEach { (type, typePredictions) ->
                                    // Get the latest prediction of this type
                                    val latestPrediction = typePredictions.maxByOrNull { it.timestamp }

                                    if (latestPrediction != null) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp)
                                            ) {
                                                Text(
                                                    text = when (type) {
                                                        PredictionType.COMPLETION_LIKELIHOOD -> "Completion Likelihood"
                                                        PredictionType.STREAK_CONTINUATION -> "Streak Continuation"
                                                        PredictionType.HABIT_FORMATION -> "Habit Formation"
                                                        PredictionType.HABIT_ABANDONMENT -> "Habit Abandonment Risk"
                                                        PredictionType.OPTIMAL_TIME -> "Optimal Time"
                                                        PredictionType.DIFFICULTY_CHANGE -> "Difficulty Change"
                                                    },
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                // Probability indicator
                                                LinearProgressIndicator(
                                                    progress = latestPrediction.probability,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(8.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "${(latestPrediction.probability * 100).toInt()}%",
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = FontWeight.Bold
                                                    )

                                                    Text(
                                                        text = "Confidence: ${(latestPrediction.confidence * 100).toInt()}%",
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Text(
                                                    text = when (type) {
                                                        PredictionType.COMPLETION_LIKELIHOOD ->
                                                            "You are ${(latestPrediction.probability * 100).toInt()}% likely to complete this habit today."
                                                        PredictionType.STREAK_CONTINUATION ->
                                                            "Your streak has a ${(latestPrediction.probability * 100).toInt()}% chance of continuing this week."
                                                        PredictionType.OPTIMAL_TIME ->
                                                            "The optimal time for this habit is in the ${if (latestPrediction.probability > 0.5f) "morning" else "evening"}."
                                                        else -> ""
                                                    },
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { viewModel.trainNetwork() },
                                    enabled = networkStatus != NetworkStatus.Training,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Retrain Network")
                                }
                            }
                        }

                        // Show loading indicator during training
                        if (networkStatus == NetworkStatus.Training) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator()

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Training Neural Network...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Training tab
                    val trainingSessions by viewModel.trainingSessions.collectAsState()
                    val currentSession by viewModel.currentTrainingSession.collectAsState()
                    val trainingEpochs by viewModel.trainingEpochs.collectAsState()
                    val networkStatus by viewModel.networkStatus.collectAsState()

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Training controls
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Neural Network Training",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    var epochs by remember { mutableStateOf("100") }

                                    OutlinedTextField(
                                        value = epochs,
                                        onValueChange = {
                                            if (it.isEmpty() || it.toIntOrNull() != null) {
                                                epochs = it
                                            }
                                        },
                                        label = { Text("Training Epochs") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            val epochsInt = epochs.toIntOrNull() ?: 100
                                            viewModel.trainNetwork(epochsInt)
                                        },
                                        enabled = networkStatus != NetworkStatus.Training,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Start Training")
                                    }
                                }
                            }

                            // Training history
                            if (trainingSessions.isNotEmpty()) {
                                Text(
                                    text = "Training History",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                trainingSessions.sortedByDescending { it.startTime }.forEach { session ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Session ${session.id.take(8)}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )

                                                Text(
                                                    text = "Status: ${session.status}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = "Epochs: ${session.epochs}",
                                                style = MaterialTheme.typography.bodySmall
                                            )

                                            if (session.finalAccuracy != null) {
                                                Text(
                                                    text = "Final Accuracy: ${(session.finalAccuracy * 100).toInt()}%",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }

                                            if (session.finalLoss != null) {
                                                Text(
                                                    text = "Final Loss: ${String.format("%.4f", session.finalLoss)}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }

                                            Text(
                                                text = "Started: ${Date(session.startTime).toString().substringBefore("GMT")}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }

                            // Training epochs visualization
                            if (currentSession != null && trainingEpochs.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Training Progress",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                // Simple line chart for accuracy and loss
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(8.dp)
                                ) {
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

                                        // Draw accuracy curve
                                        if (trainingEpochs.size > 1) {
                                            val path = Path()
                                            val pointCount = trainingEpochs.size

                                            trainingEpochs.forEachIndexed { index, epoch ->
                                                val x = padding + (width - 2 * padding) * index / (pointCount - 1)
                                                val y = height - padding - (height - 2 * padding) * epoch.accuracy

                                                if (index == 0) {
                                                    path.moveTo(x, y)
                                                } else {
                                                    path.lineTo(x, y)
                                                }
                                            }

                                            drawPath(
                                                path = path,
                                                color = MaterialTheme.colorScheme.primary,
                                                style = Stroke(width = 2f)
                                            )
                                        }

                                        // Draw loss curve
                                        if (trainingEpochs.size > 1) {
                                            val path = Path()
                                            val pointCount = trainingEpochs.size
                                            val maxLoss = trainingEpochs.maxOfOrNull { it.loss } ?: 1f

                                            trainingEpochs.forEachIndexed { index, epoch ->
                                                val x = padding + (width - 2 * padding) * index / (pointCount - 1)
                                                val normalizedLoss = (epoch.loss / maxLoss).coerceIn(0f, 1f)
                                                val y = padding + (height - 2 * padding) * normalizedLoss

                                                if (index == 0) {
                                                    path.moveTo(x, y)
                                                } else {
                                                    path.lineTo(x, y)
                                                }
                                            }

                                            drawPath(
                                                path = path,
                                                color = MaterialTheme.colorScheme.error,
                                                style = Stroke(width = 2f)
                                            )
                                        }
                                    }

                                    // Legend
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .background(MaterialTheme.colorScheme.primary)
                                            )

                                            Spacer(modifier = Modifier.width(4.dp))

                                            Text(
                                                text = "Accuracy",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .background(MaterialTheme.colorScheme.error)
                                            )

                                            Spacer(modifier = Modifier.width(4.dp))

                                            Text(
                                                text = "Loss",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Show loading indicator during training
                        if (networkStatus == NetworkStatus.Training) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator()

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Training Neural Network...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Patterns tab
                    val completions by viewModel.habitCompletions.collectAsState()
                    val predictions by viewModel.predictions.collectAsState()

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (completions.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No habit data available yet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Complete this habit a few times to see pattern analysis",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        } else {
                            HabitPatternVisualization(
                                completions = completions,
                                predictions = predictions,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                4 -> {
                    // Advanced tab
                    val recommendations by viewModel.recommendations.collectAsState()
                    val featureImportance by viewModel.featureImportance.collectAsState()
                    val explanation by viewModel.explanation.collectAsState()
                    val reinforcementAction by viewModel.reinforcementAction.collectAsState()
                    val testResults by viewModel.testResults.collectAsState()
                    val currentVariant by viewModel.currentVariant.collectAsState()

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Personalized recommendations
                            RecommendationsCard(
                                recommendations = recommendations,
                                onRecommendationAction = { recommendation, followed ->
                                    viewModel.processRecommendationAction(recommendation, followed)
                                }
                            )

                            // Explainable AI
                            FeatureImportanceCard(
                                featureImportance = featureImportance,
                                explanation = explanation
                            )

                            // Reinforcement learning action
                            ReinforcementActionCard(
                                action = reinforcementAction,
                                actionDescription = reinforcementAction?.let {
                                    viewModel.getActionDescription(it)
                                } ?: "No recommendation available yet",
                                onFeedback = { feedback ->
                                    viewModel.provideReinforcementFeedback(feedback)
                                }
                            )

                            // A/B testing results
                            ABTestingResultsCard(
                                testResults = testResults,
                                currentVariant = currentVariant,
                                onSwitchVariant = { variant ->
                                    viewModel.switchModelVariant(variant)
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Export/Import models section
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Model Sharing",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Share your trained model with friends or import models from others to improve predictions.",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Button(
                                            onClick = { viewModel.exportModel() }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = null,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Text("Export Model")
                                        }

                                        Button(
                                            onClick = { viewModel.importModel() }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = null,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Text("Import Model")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    val importedCount = viewModel.importedModelCount.collectAsState().value
                                    if (importedCount > 0) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Button(
                                                onClick = { viewModel.aggregateModels() }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Merge,
                                                    contentDescription = null,
                                                    modifier = Modifier.padding(end = 8.dp)
                                                )
                                                Text("Aggregate $importedCount Imported Models")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
