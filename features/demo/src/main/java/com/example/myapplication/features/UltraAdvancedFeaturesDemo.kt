package com.example.myapplication.features

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.model.HabitFrequency
import com.example.myapplication.core.data.model.BiometricReading
import com.example.myapplication.core.data.model.BiometricType
import com.example.myapplication.core.data.model.BiometricTrend
import com.example.myapplication.features.neural.NeuralNode
import com.example.myapplication.core.data.model.NeuralNodeType
import com.example.myapplication.core.data.model.Offset3D
import com.example.myapplication.core.data.model.Rotation3D
import com.example.myapplication.core.data.model.SpatialObject
import com.example.myapplication.core.data.model.SpatialObjectType
import com.example.myapplication.features.ml.PredictionType
import com.example.myapplication.features.ml.HabitPrediction
import com.example.myapplication.features.ml.PredictionFactorDetail
import com.example.myapplication.core.data.model.VoiceCommand
import com.example.myapplication.core.data.model.VoiceIntent
import com.example.myapplication.core.data.model.VoiceEntity
import com.example.myapplication.core.data.model.EntityType
import com.example.myapplication.core.ui.components.SwipeGestureArea
import com.example.myapplication.features.biometric.*
import com.example.myapplication.features.ml.*
import com.example.myapplication.features.neural.*
import com.example.myapplication.features.quantum.*
import com.example.myapplication.features.spatial.*
import com.example.myapplication.features.voice.*
import com.example.myapplication.core.ui.animation.*
import java.util.Date
import java.util.*
import kotlin.math.*

/**
 * A demo screen showcasing ultra-advanced features
 */
@Composable
fun UltraAdvancedFeaturesDemo(
    onClose: () -> Unit,
    onNavigateToFeature: (String) -> Unit = {}
) {
    // State for current demo section
    var currentSection by remember { mutableStateOf(0) }
    val sectionTitles = listOf(
        "Neural Interface",
        "Predictive ML",
        "Biometrics",
        "Spatial Computing",
        "Voice & NLP",
        "Quantum Visualization"
    )

    // Create sample data
    val sampleHabits = remember {
        listOf(
            Habit(
                id = "1",
                name = "Morning Meditation",
                description = "10 minutes of mindfulness meditation",
                category = "Mindfulness",
                frequency = HabitFrequency.DAILY,
                streak = 12,
                goal = 10,
                goalProgress = 8,
                reminderTime = "07:00"
            ),
            Habit(
                id = "2",
                name = "Read Books",
                description = "Read for personal development",
                category = "Learning",
                frequency = HabitFrequency.DAILY,
                streak = 5,
                goal = 30,
                goalProgress = 15,
                reminderTime = "21:00"
            ),
            Habit(
                id = "3",
                name = "Exercise",
                description = "30 minutes of physical activity",
                category = "Fitness",
                frequency = HabitFrequency.DAILY,
                streak = 20,
                goal = 30,
                goalProgress = 25,
                reminderTime = "18:00"
            ),
            Habit(
                id = "4",
                name = "Drink Water",
                description = "Stay hydrated throughout the day",
                category = "Health",
                frequency = HabitFrequency.DAILY,
                streak = 30,
                goal = 8,
                goalProgress = 6,
                reminderTime = null
            ),
            Habit(
                id = "5",
                name = "Journal",
                description = "Write daily reflections",
                category = "Mindfulness",
                frequency = HabitFrequency.DAILY,
                streak = 8,
                goal = 1,
                goalProgress = 1,
                reminderTime = "22:00"
            )
        )
    }

    // Create sample neural nodes
    val sampleNeuralNodes = remember {
        val nodes = listOf(
            NeuralNode(
                type = NeuralNodeType.INPUT,
                position = Offset(100f, 100f),
                label = "Habit Trigger",
                activationLevel = 0.8f
            ),
            NeuralNode(
                type = NeuralNodeType.INPUT,
                position = Offset(100f, 250f),
                label = "Environment",
                activationLevel = 0.6f
            ),
            NeuralNode(
                type = NeuralNodeType.HIDDEN,
                position = Offset(300f, 150f),
                label = "Motivation",
                activationLevel = 0.7f
            ),
            NeuralNode(
                type = NeuralNodeType.HIDDEN,
                position = Offset(300f, 300f),
                label = "Difficulty",
                activationLevel = 0.4f
            ),
            NeuralNode(
                type = NeuralNodeType.OUTPUT,
                position = Offset(500f, 200f),
                label = "Habit Completion",
                activationLevel = 0.5f
            )
        )

        // Add connections
        nodes[0].connections.add(nodes[2].id)
        nodes[0].connections.add(nodes[3].id)
        nodes[1].connections.add(nodes[2].id)
        nodes[2].connections.add(nodes[4].id)
        nodes[3].connections.add(nodes[4].id)

        nodes
    }

    // Create sample ML predictions
    val samplePredictions = remember {
        listOf(
            HabitPrediction(
                habitId = "1",
                habitName = "Morning Meditation",
                predictionType = PredictionType.COMPLETION_LIKELIHOOD,
                probability = 0.87f,
                timeframe = "Today",
                confidenceInterval = Pair(0.82f, 0.92f),
                factors = listOf(
                    PredictionFactorDetail("Previous completion pattern", 0.6f, 0.9f),
                    PredictionFactorDetail("Sleep quality", 0.3f, 0.8f),
                    PredictionFactorDetail("Morning schedule", -0.1f, 0.7f)
                )
            ),
            HabitPrediction(
                habitId = "2",
                habitName = "Read Books",
                predictionType = PredictionType.STREAK_CONTINUATION,
                probability = 0.65f,
                timeframe = "Next 7 days",
                confidenceInterval = Pair(0.58f, 0.72f),
                factors = listOf(
                    PredictionFactorDetail("Current streak momentum", 0.4f, 0.85f),
                    PredictionFactorDetail("Evening availability", -0.2f, 0.75f),
                    PredictionFactorDetail("Content interest level", 0.5f, 0.9f)
                )
            ),
            HabitPrediction(
                habitId = "3",
                habitName = "Exercise",
                predictionType = PredictionType.OPTIMAL_TIME,
                probability = 0.92f,
                timeframe = "Today",
                confidenceInterval = Pair(0.88f, 0.96f),
                factors = listOf(
                    PredictionFactorDetail("Historical completion times", 0.7f, 0.95f),
                    PredictionFactorDetail("Weather forecast", 0.2f, 0.8f),
                    PredictionFactorDetail("Energy level pattern", 0.5f, 0.85f)
                )
            )
        )
    }

    // Create sample biometric readings
    val sampleBiometricReadings = remember {
        listOf(
            BiometricReading(
                type = BiometricType.HEART_RATE,
                value = 72f,
                unit = "BPM",
                normalRange = Pair(60f, 100f),
                trend = BiometricTrend.STABLE,
                relatedHabitIds = listOf("3")
            ),
            BiometricReading(
                type = BiometricType.SLEEP_QUALITY,
                value = 85f,
                unit = "points",
                normalRange = Pair(70f, 100f),
                trend = BiometricTrend.INCREASING,
                relatedHabitIds = listOf("1", "5")
            ),
            BiometricReading(
                type = BiometricType.STRESS_LEVEL,
                value = 42f,
                unit = "points",
                normalRange = Pair(0f, 50f),
                trend = BiometricTrend.DECREASING,
                relatedHabitIds = listOf("1", "4")
            ),
            BiometricReading(
                type = BiometricType.ENERGY_LEVEL,
                value = 78f,
                unit = "points",
                normalRange = Pair(60f, 100f),
                trend = BiometricTrend.FLUCTUATING,
                relatedHabitIds = listOf("3", "4")
            )
        )
    }

    // Create sample sleep data
    val sampleSleepData = remember {
        mapOf(
            "Deep" to 1.5f,
            "Light" to 4.2f,
            "REM" to 1.8f,
            "Awake" to 0.5f
        )
    }

    // Create sample heart rate history
    val sampleHeartRateHistory = remember {
        val calendar = Calendar.getInstance()
        List(12) { index ->
            calendar.add(Calendar.HOUR, -2)
            Pair(calendar.time, 65 + (index % 3) * 5 + Random().nextInt(10))
        }.reversed()
    }

    // Create sample spatial objects
    val sampleSpatialObjects = remember {
        sampleHabits.mapIndexed { index, habit ->
            // Calculate position in a spiral pattern
            val angle = index * 30f * (PI / 180f)
            val radius = 100f + index * 20f
            val x = radius * cos(angle).toFloat()
            val y = radius * sin(angle).toFloat()

            // Determine object type based on habit properties
            val objectType = when {
                habit.streak > 10 -> SpatialObjectType.STREAK_TOWER
                habit.unlockedBadges.isNotEmpty() -> SpatialObjectType.ACHIEVEMENT_STAR
                habit.goal > 1 -> SpatialObjectType.GOAL_PYRAMID
                habit.category != null -> SpatialObjectType.CATEGORY_CUBE
                habit.reminderTime != null -> SpatialObjectType.REMINDER_CLOCK
                else -> SpatialObjectType.HABIT_SPHERE
            }

            // Determine color based on habit category
            val color = when (habit.category) {
                "Health" -> Color(0xFF4CAF50)
                "Fitness" -> Color(0xFFF44336)
                "Learning" -> Color(0xFF2196F3)
                "Productivity" -> Color(0xFFFF9800)
                "Mindfulness" -> Color(0xFF9C27B0)
                else -> Color(0xFF3F51B5)
            }

            SpatialObject(
                type = objectType,
                position = Offset3D(x, y, 0f),
                rotation = Rotation3D(0f, 0f, 0f),
                scale = 0.5f + (habit.streak.coerceAtMost(10) / 10f) * 0.5f,
                color = color.value.toLong(),
                label = habit.name,
                relatedHabitId = habit.id
            )
        }
    }

    // Sample voice command
    var sampleVoiceCommand by remember {
        mutableStateOf(
            VoiceCommand(
                text = "Create a new habit to drink water every day",
                intent = VoiceIntent.CREATE_HABIT,
                confidence = 0.92f,
                entities = listOf(
                    VoiceEntity(
                        type = EntityType.HABIT_NAME,
                        value = "drink water",
                        confidence = 0.95f
                    ),
                    VoiceEntity(
                        type = EntityType.FREQUENCY,
                        value = "every day",
                        confidence = 0.98f
                    )
                )
            )
        )
    }

    // Main content with SwipeGestureArea for navigation
    SwipeGestureArea(
        onSwipeLeft = {
            if (currentSection < sectionTitles.size - 1) {
                currentSection++
            }
        },
        onSwipeRight = {
            if (currentSection > 0) {
                currentSection--
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onClose,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Text(
                    text = "Ultra-Advanced Features",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Section tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sectionTitles.size) { index ->
                    val isSelected = index == currentSection

                    Button(
                        onClick = { currentSection = index },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Text(sectionTitles[index])
                    }
                }
            }

            // Section content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                when (currentSection) {
                    0 -> {
                        // Neural Interface Demo
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "Neural Interface Simulation",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = "This neural network simulates how habit triggers connect to habit formation in your brain. Tap nodes to activate them, drag to move, and double-tap to create connections.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            NeuralInterfaceSimulation(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(500.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                initialNodes = sampleNeuralNodes
                            )
                        }
                    }
                    1 -> {
                        // Predictive ML Demo
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Text(
                                text = "Predictive Habit Modeling",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = "Our advanced machine learning models predict your habit completion patterns and provide personalized insights.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            MLModelTrainingVisualizer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp)
                                    .height(300.dp)
                            )

                            HabitPredictionList(
                                predictions = samplePredictions,
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            )
                        }
                    }
                    2 -> {
                        // Biometrics Demo
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Biometric Integration",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Button(
                                    onClick = { onNavigateToFeature("biometric_integration") }
                                ) {
                                    Text("Open Full Feature")
                                }
                            }

                            Text(
                                text = "Connect your biometric data to understand how your habits affect your physical and mental wellbeing.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            HeartRateMonitor(
                                currentHeartRate = 72,
                                historyData = sampleHeartRateHistory,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp)
                            )

                            SleepQualityVisualizer(
                                sleepData = sampleSleepData,
                                sleepScore = 85,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp)
                            )

                            BiometricComponents.BiometricReadingsList(
                                readings = sampleBiometricReadings,
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            )
                        }
                    }
                    3 -> {
                        // Spatial Computing Demo
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Spatial Computing",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Button(
                                    onClick = { onNavigateToFeature("spatial_computing") }
                                ) {
                                    Text("Open Full Feature")
                                }
                            }

                            Text(
                                text = "Visualize your habits in 3D space. Drag to rotate, pinch to zoom, and explore your habit ecosystem.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            SpatialEnvironment(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(500.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                spatialObjects = sampleSpatialObjects,
                                onObjectClick = { _ -> /* Handle object click */ }
                            )
                        }
                    }
                    4 -> {
                        // Voice & NLP Demo
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Voice & Natural Language Processing",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Button(
                                    onClick = { onNavigateToFeature("voice_integration") }
                                ) {
                                    Text("Open Full Feature")
                                }
                            }

                            Text(
                                text = "Control your habit tracking with natural voice commands. Our advanced NLP understands your intent and extracts relevant information.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            com.example.myapplication.features.voice.VoiceRecognitionInterface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp),
                                onVoiceCommand = { command: VoiceCommand ->
                                    sampleVoiceCommand = command
                                }
                            )

                            com.example.myapplication.features.voice.NLPResultDisplay(
                                command = sampleVoiceCommand,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    5 -> {
                        // Quantum Visualization Demo
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Quantum-Inspired Visualization",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Button(
                                    onClick = { onNavigateToFeature("quantum_visualization") }
                                ) {
                                    Text("Open Full Feature")
                                }
                            }

                            Text(
                                text = "Experience your habits as quantum states in superposition. This visualization represents the probabilistic nature of habit formation and the interconnectedness of your habit system.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            QuantumSuperpositionVisualizer(
                                habits = sampleHabits,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp)
                            )

                            Text(
                                text = "Quantum Interference Pattern",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Text(
                                text = "This visualization shows how your habits interact with each other through quantum interference, creating emergent patterns in your behavior.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            QuantumInterferencePattern(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                waveCount = 3
                            )
                        }
                    }
                }
            }
        }
    }
}
