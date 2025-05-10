package com.example.myapplication.features.neural

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * A neural network interface simulation
 */
@Composable
fun NeuralInterfaceSimulation(
    modifier: Modifier = Modifier,
    initialNodes: List<NeuralNode> = emptyList(),
    viewModel: NeuralInterfaceViewModel? = null
) {
    // State for nodes
    var nodes by remember { mutableStateOf(initialNodes) }

    // State for selected node
    var selectedNodeId by remember { mutableStateOf<String?>(null) }

    // State for animation
    val coroutineScope = rememberCoroutineScope()

    // Function to activate a node
    fun activateNode(nodeId: String) {
        nodes = nodes.map { node ->
            if (node.id == nodeId) {
                node.copy(activationLevel = 1.0f)
            } else {
                node
            }
        }

        // Propagate activation
        coroutineScope.launch {
            delay(300)
            propagateActivation()
        }
    }

    // Function to propagate activation through the network
    fun propagateActivation() {
        // Create a copy of nodes to work with
        val updatedNodes = nodes.toMutableList()

        // For each node, propagate activation to connected nodes
        for (node in nodes) {
            if (node.activationLevel > 0.1f) {
                for (connectedNodeId in node.connections) {
                    val connectedNodeIndex = updatedNodes.indexOfFirst { it.id == connectedNodeId }
                    if (connectedNodeIndex >= 0) {
                        val connectedNode = updatedNodes[connectedNodeIndex]
                        val newActivation = (connectedNode.activationLevel + node.activationLevel * 0.7f).coerceIn(0f, 1f)
                        updatedNodes[connectedNodeIndex] = connectedNode.copy(activationLevel = newActivation)
                    }
                }
            }
        }

        // Update nodes
        nodes = updatedNodes

        // Continue propagation if any node is still active
        if (updatedNodes.any { it.activationLevel > 0.1f }) {
            coroutineScope.launch {
                delay(300)
                propagateActivation()
            }
        }
    }

    // Initialize propagation
    LaunchedEffect(Unit) {
        // Define propagation function
        fun propagateActivation() {
            // Create a copy of nodes to work with
            val updatedNodes = nodes.toMutableList()

            // For each node, propagate activation to connected nodes
            for (i in nodes.indices) {
                val node = nodes[i]
                if (node.activationLevel > 0.1f) {
                    for (connectedNodeId in node.connections) {
                        val connectedNodeIndex = updatedNodes.indexOfFirst { it.id == connectedNodeId }
                        if (connectedNodeIndex >= 0) {
                            val connectedNode = updatedNodes[connectedNodeIndex]
                            val newActivation = (connectedNode.activationLevel + node.activationLevel * 0.7f).coerceIn(0f, 1f)
                            updatedNodes[connectedNodeIndex] = connectedNode.copy(activationLevel = newActivation)
                        }
                    }

                    // Decay activation of current node
                    updatedNodes[i] = node.copy(activationLevel = (node.activationLevel * 0.8f).coerceIn(0f, 1f))
                }
            }

            // Update nodes
            nodes = updatedNodes
        }
    }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            // Find tapped node
                            val tappedNode = nodes.firstOrNull { node ->
                                val distance = sqrt(
                                    (offset.x - node.position.x).pow(2) +
                                    (offset.y - node.position.y).pow(2)
                                )
                                distance < 30f
                            }

                            if (tappedNode != null) {
                                // Activate the node
                                activateNode(tappedNode.id)
                            }
                        }
                    )
                }
        ) {
            // Draw connections
            for (node in nodes) {
                for (connectedNodeId in node.connections) {
                    val connectedNode = nodes.firstOrNull { it.id == connectedNodeId }
                    if (connectedNode != null) {
                        // Calculate connection strength based on activation levels
                        val connectionStrength = (node.activationLevel + connectedNode.activationLevel) / 2f

                        // Draw connection line
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.3f + connectionStrength * 0.7f),
                            start = node.position,
                            end = connectedNode.position,
                            strokeWidth = 2f + connectionStrength * 3f
                        )
                    }
                }
            }

            // Draw nodes
            for (node in nodes) {
                val isSelected = node.id == selectedNodeId

                // Determine node color based on type and activation
                val nodeColor = when (node.type) {
                    NeuralNodeType.INPUT -> Color(0xFF4CAF50)
                    NeuralNodeType.HIDDEN -> Color(0xFF2196F3)
                    NeuralNodeType.OUTPUT -> Color(0xFFF44336)
                    NeuralNodeType.BIAS -> Color(0xFFFF9800)
                    NeuralNodeType.RECURRENT -> Color(0xFF9C27B0)
                }.copy(alpha = 0.5f + node.activationLevel * 0.5f)

                // Node size based on activation
                val nodeSize = 30f * (1f + 0.3f * node.activationLevel)

                // Draw glow effect for active nodes
                if (node.activationLevel > 0.1f) {
                    drawCircle(
                        color = nodeColor.copy(alpha = 0.3f * node.activationLevel),
                        radius = nodeSize * 2f,
                        center = node.position
                    )
                }

                // Draw node
                drawCircle(
                    color = nodeColor,
                    radius = nodeSize,
                    center = node.position
                )

                // Draw node border
                drawCircle(
                    color = Color.White,
                    radius = nodeSize,
                    center = node.position,
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}
