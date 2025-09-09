package com.example.myapplication.features.ml

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen for predictive machine learning
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictiveMLScreen(
    navController: NavController,
    viewModel: PredictiveMLViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    // State
    val predictions by viewModel.predictions.collectAsState()
    val isTraining by viewModel.isTraining.collectAsState()
    val trainingProgress by viewModel.trainingProgress.collectAsState()
    val modelAccuracy by viewModel.modelAccuracy.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Predictive ML") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshPredictions() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "Predictions") },
                    label = { Text("Predictions") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    icon = { Icon(Icons.Default.ModelTraining, contentDescription = "Training") },
                    label = { Text("Training") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.setSelectedTab(2) },
                    icon = { Icon(Icons.Default.Insights, contentDescription = "Insights") },
                    label = { Text("Insights") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Content based on selected tab
            when (selectedTab) {
                0 -> PredictionsTab(predictions, viewModel)
                1 -> TrainingTab(isTraining, trainingProgress, modelAccuracy, viewModel)
                2 -> InsightsTab(predictions, viewModel)
            }

            // Training overlay
            AnimatedVisibility(
                visible = isTraining,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .widthIn(max = 300.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Training Model",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            CircularProgressIndicator(
                                progress = { trainingProgress },
                                modifier = Modifier.size(80.dp),
                                strokeWidth = 8.dp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "${(trainingProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Please wait while the model is being trained...",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.cancelTraining() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab for displaying predictions
 */
@Composable
fun PredictionsTab(
    predictions: List<HabitPrediction>,
    viewModel: PredictiveMLViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Habit Predictions",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "ML-powered predictions about your habits",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (predictions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No predictions available",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Train the model to generate predictions",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(onClick = { viewModel.startTraining() }) {
                        Icon(
                            imageVector = Icons.Default.ModelTraining,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Train Model")
                    }
                }
            }
        } else {
            HabitPredictionList(
                predictions = predictions,
                modifier = Modifier.weight(1f),
                onPredictionClick = { prediction ->
                    // Handle prediction click
                    viewModel.showToast("Prediction: ${prediction.habitName}")
                }
            )
        }
    }
}

/**
 * Tab for model training
 */
@Composable
fun TrainingTab(
    isTraining: Boolean,
    trainingProgress: Float,
    modelAccuracy: Float,
    viewModel: PredictiveMLViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Model Training",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Train and visualize your ML model",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Model accuracy
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Model Accuracy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        progress = { modelAccuracy },
                        modifier = Modifier.size(60.dp),
                        strokeWidth = 6.dp
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "${(modelAccuracy * 100).toInt()}%",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (modelAccuracy < 0.7f) {
                                "Needs more training data"
                            } else if (modelAccuracy < 0.9f) {
                                "Good accuracy"
                            } else {
                                "Excellent accuracy"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Training visualizer
        Text(
            text = "Training Visualization",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        MLModelTrainingVisualizer(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Training controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { viewModel.startTraining() },
                modifier = Modifier.weight(1f),
                enabled = !isTraining
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Train Model")
            }

            OutlinedButton(
                onClick = { viewModel.resetModel() },
                modifier = Modifier.weight(1f),
                enabled = !isTraining
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Reset Model")
            }
        }
    }
}

/**
 * Tab for ML insights
 */
@Composable
fun InsightsTab(
    predictions: List<HabitPrediction>,
    viewModel: PredictiveMLViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "ML Insights",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Understand what factors influence your habits",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (predictions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Insights,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No insights available",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Train the model to generate insights",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Feature importance
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Feature Importance",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Feature importance bars
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FeatureImportanceBar(
                                    feature = "Time of Day",
                                    importance = 0.85f
                                )

                                FeatureImportanceBar(
                                    feature = "Day of Week",
                                    importance = 0.72f
                                )

                                FeatureImportanceBar(
                                    feature = "Location",
                                    importance = 0.64f
                                )

                                FeatureImportanceBar(
                                    feature = "Mood",
                                    importance = 0.58f
                                )

                                FeatureImportanceBar(
                                    feature = "Previous Habit",
                                    importance = 0.45f
                                )
                            }
                        }
                    }
                }

                // Prediction factors
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Key Prediction Factors",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                PredictionFactorItem(
                                    factor = PredictionFactor.TIME_OF_DAY,
                                    description = "Morning (6-9 AM) is your most productive time"
                                )

                                PredictionFactorItem(
                                    factor = PredictionFactor.DAY_OF_WEEK,
                                    description = "You're most consistent on Tuesdays and Thursdays"
                                )

                                PredictionFactorItem(
                                    factor = PredictionFactor.LOCATION,
                                    description = "Home environment increases completion by 35%"
                                )

                                PredictionFactorItem(
                                    factor = PredictionFactor.MOOD,
                                    description = "Positive mood correlates with 28% higher completion"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Feature importance bar
 */
@Composable
fun FeatureImportanceBar(
    feature: String,
    importance: Float
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = feature,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(120.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(importance)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            Text(
                text = "${(importance * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

/**
 * Prediction factor item
 */
@Composable
fun PredictionFactorItem(
    factor: PredictionFactor,
    description: String
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = when (factor) {
                PredictionFactor.TIME_OF_DAY -> Icons.Default.Schedule
                PredictionFactor.DAY_OF_WEEK -> Icons.Default.DateRange
                PredictionFactor.LOCATION -> Icons.Default.LocationOn
                PredictionFactor.MOOD -> Icons.Default.Mood
                PredictionFactor.PREVIOUS_HABIT -> Icons.Default.History
                PredictionFactor.WEATHER -> Icons.Default.WbSunny
                PredictionFactor.SOCIAL_CONTEXT -> Icons.Default.People
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 2.dp, end = 16.dp)
        )

        Column {
            Text(
                text = factor.name.replace("_", " ").lowercase()
                    .split(" ")
                    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
