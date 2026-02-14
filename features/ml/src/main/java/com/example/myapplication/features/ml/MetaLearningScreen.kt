package com.example.myapplication.features.ml

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.animation.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.core.ui.components.AppBar
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Screen for meta-learning
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetaLearningScreen(
    navController: NavController,
    viewModel: MetaLearningViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    // State
    val metaLearningProgress by viewModel.metaLearningProgress.collectAsState()
    val adaptationProgress by viewModel.adaptationProgress.collectAsState()
    val isMetaLearning by viewModel.isMetaLearning.collectAsState()
    val isAdapting by viewModel.isAdapting.collectAsState()
    val metaLearningResult by viewModel.metaLearningResult.collectAsState()
    val adaptationResult by viewModel.adaptationResult.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
            AppBar(
                title = "Meta-Learning",
                navController = navController
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Introduction
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Meta-Learning",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Meta-learning is the process of learning how to learn. " +
                                   "This system learns patterns across multiple habits to improve " +
                                   "its ability to adapt to new habits and provide better recommendations.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Meta-learning visualization
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        MetaLearningVisualization(
                            metaLearningProgress = metaLearningProgress,
                            adaptationProgress = adaptationProgress
                        )
                    }
                }

                // Meta-learning controls
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Meta-Learning Controls",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Meta-learning progress
                        Text(
                            text = "Meta-Learning Progress: ${(metaLearningProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { metaLearningProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Start meta-learning button
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.startMetaLearning()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isMetaLearning && !isAdapting
                        ) {
                            if (isMetaLearning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Meta-Learning")
                        }
                    }
                }

                // Adaptation controls
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Adaptation Controls",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Adaptation progress
                        Text(
                            text = "Adaptation Progress: ${(adaptationProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { adaptationProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Adapt to habit button
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.adaptToHabit()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isMetaLearning && !isAdapting && metaLearningProgress > 0.1f,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            if (isAdapting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Autorenew,
                                    contentDescription = null
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Adapt to Habit")
                        }
                    }
                }

                // Results
                metaLearningResult?.let { result ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Meta-Learning Results",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = result,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                adaptationResult?.let { result ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Adaptation Results",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = result,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Error message
                errorMessage?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Error",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Loading indicator
            if (isMetaLearning || isAdapting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .width(200.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (isMetaLearning) "Meta-learning in progress..." else "Adapting to habit...",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Meta-learning visualization component
 */
@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun MetaLearningVisualization(
    metaLearningProgress: Float,
    adaptationProgress: Float
) {
    // Animation
    val infiniteTransition = rememberInfiniteTransition()
    val animation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Use a simpler visualization for now
        val animatedProgress = remember(metaLearningProgress) {
            Animatable(initialValue = 0f)
        }

        LaunchedEffect(metaLearningProgress) {
            animatedProgress.animateTo(
                targetValue = metaLearningProgress,
                animationSpec = tween(1000)
            )
        }

        val animatedAdaptation = remember(adaptationProgress) {
            Animatable(initialValue = 0f)
        }

        LaunchedEffect(adaptationProgress) {
            animatedAdaptation.animateTo(
                targetValue = adaptationProgress,
                animationSpec = tween(1000)
            )
        }

        // Draw progress indicators
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Meta-learning progress
            Text(
                text = "Meta-Learning: ${(animatedProgress.value * 100).toInt()}%",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { animatedProgress.value },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Adaptation progress
            Text(
                text = "Adaptation: ${(animatedAdaptation.value * 100).toInt()}%",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { animatedAdaptation.value },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.secondaryContainer
            )
        }
    }
}
