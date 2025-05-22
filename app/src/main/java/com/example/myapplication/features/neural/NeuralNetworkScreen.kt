package com.example.myapplication.features.neural

import androidx.compose.animation.core.*
import androidx.compose.animation.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.ui.components.AppBar
import kotlinx.coroutines.launch
import kotlin.math.pow

/**
 * Screen for neural network visualization and training
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuralNetworkScreen(
    navController: NavController,
    viewModel: NeuralNetworkViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    // State
    val nodes by viewModel.nodes.collectAsState()
    val connections by viewModel.connections.collectAsState()
    val isTraining by viewModel.isTraining.collectAsState()
    val trainingProgress by viewModel.trainingProgress.collectAsState()
    val trainingResult by viewModel.trainingResult.collectAsState()
    val inferenceResult by viewModel.inferenceResult.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Selected node for activation
    var selectedNodeId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            AppBar(
                title = "Neural Network",
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
                            text = "Neural Network",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Visualize and interact with a neural network that learns from your habits. " +
                                   "Tap on nodes to activate them and see how activation propagates through the network.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Neural network visualization
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        NeuralNetworkVisualization(
                            nodes = nodes,
                            connections = connections,
                            onNodeTap = { nodeId ->
                                selectedNodeId = nodeId
                                coroutineScope.launch {
                                    viewModel.activateNode(nodeId)
                                }
                            }
                        )
                    }
                }

                // Training controls
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
                            text = "Training Controls",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Training progress
                        Text(
                            text = "Training Progress: ${(trainingProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { trainingProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Training button
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.trainNetwork()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isTraining
                        ) {
                            if (isTraining) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = null
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Train Network")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Inference button
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.runInference()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isTraining,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run Inference")
                        }
                    }
                }

                // Results
                trainingResult?.let { result ->
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
                                text = "Training Results",
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

                inferenceResult?.let { result ->
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
                                text = "Inference Results",
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
            if (isTraining) {
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
                                text = "Training neural network...",
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
 * Neural network visualization component
 */
@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun NeuralNetworkVisualization(
    nodes: List<NeuralNode>,
    connections: List<NeuralConnection>,
    onNodeTap: (String) -> Unit
) {
    // Animation
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAnimation = infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Use a simpler visualization for now
        val animatedPulse = remember {
            Animatable(initialValue = 0.5f)
        }

        LaunchedEffect(Unit) {
            while (true) {
                animatedPulse.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(1000, easing = FastOutSlowInEasing)
                )
                animatedPulse.animateTo(
                    targetValue = 0.5f,
                    animationSpec = tween(1000, easing = FastOutSlowInEasing)
                )
            }
        }

        // Create a grid layout for nodes
        val nodeRows = 3
        val nodeCols = 5

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Draw nodes in a grid
            for (row in 0 until nodeRows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (col in 0 until nodeCols) {
                        // Find if there's a node at this position
                        val nodeIndex = row * nodeCols + col
                        val node = nodes.getOrNull(nodeIndex)

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (node != null) {
                                        MaterialTheme.colorScheme.primary.copy(
                                            alpha = (0.2f + 0.8f * node.activationLevel) * animatedPulse.value
                                        )
                                    } else {
                                        Color.Transparent
                                    },
                                    shape = CircleShape
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (node != null) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        Color.Transparent
                                    },
                                    shape = CircleShape
                                )
                                .clickable(enabled = node != null) {
                                    node?.let { onNodeTap(it.id) }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (node != null) {
                                Text(
                                    text = when (node.type) {
                                        com.example.myapplication.data.model.NeuralNodeType.INPUT -> "I"
                                        com.example.myapplication.data.model.NeuralNodeType.HIDDEN -> "H"
                                        com.example.myapplication.data.model.NeuralNodeType.OUTPUT -> "O"
                                        com.example.myapplication.data.model.NeuralNodeType.BIAS -> "B"
                                        com.example.myapplication.data.model.NeuralNodeType.RECURRENT -> "R"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(
                                        alpha = node.activationLevel * animatedPulse.value
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Instructions
            Text(
                text = "Tap on nodes to activate them",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
