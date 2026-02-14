package com.example.myapplication.features.neural

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.myapplication.core.data.model.NeuralNode
import com.example.myapplication.core.data.model.NeuralNodeType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.ui.animation.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.*

// Using NeuralNode and NeuralNodeType from data.model package

/**
 * A component that simulates a neural interface for habit formation visualization
 */
@Composable
fun NeuralInterfaceSimulation(
    modifier: Modifier = Modifier,
    viewModel: NeuralInterfaceViewModel,
    habitId: String? = null
) {
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Collect state from ViewModel
    val nodes by viewModel.nodes.collectAsState()
    val selectedNodeId by viewModel.selectedNodeId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()

    // Local UI state
    var draggedNodeId by remember { mutableStateOf<String?>(null) }
    var isCreatingConnection by remember { mutableStateOf(false) }
    var connectionStartNodeId by remember { mutableStateOf<String?>(null) }
    var connectionEndPosition by remember { mutableStateOf<Offset?>(null) }

    // Animation states
    val pulseAnimation = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = AnimeEasing.EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Load neural network for the habit
    LaunchedEffect(habitId) {
        habitId?.let {
            viewModel.loadNeuralNetworkForHabit(it)
        }
    }

    // Show error message if any
    errorMessage?.let { error ->
        LaunchedEffect(error) {
            // Show error message (e.g., using a Snackbar)
            delay(3000)
            viewModel.clearError()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        // Check if we tapped on a node
                        val tappedNode = nodes.find { node ->
                            val distance = (node.position - offset).getDistance()
                            distance < 50f
                        }

                        if (tappedNode != null) {
                            // Select node
                            viewModel.selectNode(tappedNode.id)

                            // Activate node
                            viewModel.activateNode(tappedNode.id)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        } else {
                            // Deselect
                            viewModel.selectNode(null)
                        }
                    },
                    onLongPress = { offset ->
                        // Create a new node at this position
                        viewModel.addNode(
                            type = NeuralNodeType.values().random(),
                            position = offset,
                            label = "Node ${nodes.size + 1}"
                        )
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDoubleTap = { offset ->
                        // Start creating a connection
                        val tappedNode = nodes.find { node ->
                            val distance = (node.position - offset).getDistance()
                            distance < 50f
                        }

                        if (tappedNode != null) {
                            isCreatingConnection = true
                            connectionStartNodeId = tappedNode.id
                            connectionEndPosition = offset
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        // Check if we're dragging a node
                        val draggedNode = nodes.find { node ->
                            val distance = (node.position - offset).getDistance()
                            distance < 50f
                        }

                        if (draggedNode != null) {
                            draggedNodeId = draggedNode.id
                        } else if (isCreatingConnection) {
                            connectionEndPosition = offset
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()

                        if (draggedNodeId != null) {
                            // Move the dragged node
                            val node = nodes.find { it.id == draggedNodeId }
                            if (node != null) {
                                // Update node position in local state for smooth dragging
                                // We'll update the database when drag ends
                                val newPosition = node.position + dragAmount
                                viewModel.updateNodePosition(node.id, newPosition)
                            }
                        } else if (isCreatingConnection) {
                            // Update the end position of the connection being created
                            connectionEndPosition = connectionEndPosition?.plus(dragAmount)
                        }
                    },
                    onDragEnd = {
                        if (isCreatingConnection && connectionStartNodeId != null && connectionEndPosition != null) {
                            // Check if we're connecting to another node
                            val endNode = nodes.find { node ->
                                val distance = (node.position - connectionEndPosition!!).getDistance()
                                distance < 50f
                            }

                            if (endNode != null && endNode.id != connectionStartNodeId) {
                                // Create connection
                                viewModel.addConnection(connectionStartNodeId!!, endNode.id)
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }

                            isCreatingConnection = false
                            connectionStartNodeId = null
                            connectionEndPosition = null
                        }

                        draggedNodeId = null
                    },
                    onDragCancel = {
                        draggedNodeId = null
                        isCreatingConnection = false
                        connectionStartNodeId = null
                        connectionEndPosition = null
                    }
                )
            }
    ) {
        // Draw neural network
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw connections
            for (node in nodes) {
                for (connectedNodeId in node.connections) {
                    val connectedNode = nodes.find { it.id == connectedNodeId }
                    if (connectedNode != null) {
                        // Calculate connection strength based on activation
                        val connectionStrength = (node.activationLevel + connectedNode.activationLevel) / 2
                        val connectionColor = lerp(
                            Color.Gray.copy(alpha = 0.3f),
                            Color.Cyan.copy(alpha = 0.8f),
                            connectionStrength
                        )
                        val connectionWidth = 2f + 3f * connectionStrength

                        // Draw connection line
                        drawLine(
                            color = connectionColor,
                            start = node.position,
                            end = connectedNode.position,
                            strokeWidth = connectionWidth
                        )

                        // Draw activation pulse traveling along connection
                        if (connectionStrength > 0.1f) {
                            val direction = connectedNode.position - node.position
                            val distance = direction.getDistance()
                            val normalizedDirection = direction / distance

                            // Calculate pulse position (moves from source to target)
                            val pulsePosition = node.position + normalizedDirection * distance * (0.5f + 0.5f * sin(pulseScale * PI.toFloat()))

                            drawCircle(
                                color = Color.Cyan.copy(alpha = 0.8f * connectionStrength),
                                radius = 5f + 3f * connectionStrength,
                                center = pulsePosition
                            )
                        }
                    }
                }
            }

            // Draw connection being created
            if (isCreatingConnection && connectionStartNodeId != null && connectionEndPosition != null) {
                val startNode = nodes.find { it.id == connectionStartNodeId }
                if (startNode != null) {
                    drawLine(
                        color = Color.Yellow.copy(alpha = 0.7f),
                        start = startNode.position,
                        end = connectionEndPosition!!,
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }
            }

            // Draw nodes
            for (node in nodes) {
                val isSelected = node.id == selectedNodeId
                val isDragged = node.id == draggedNodeId

                // Node color based on type and activation
                val baseColor = when (node.type) {
                    NeuralNodeType.INPUT -> Color.Green
                    NeuralNodeType.HIDDEN -> Color.Blue
                    NeuralNodeType.OUTPUT -> Color.Red
                    NeuralNodeType.BIAS -> Color.Yellow
                    NeuralNodeType.RECURRENT -> Color.Magenta
                }

                // Blend with activation color
                val nodeColor = lerp(
                    baseColor.copy(alpha = 0.5f),
                    Color.White,
                    node.activationLevel
                )

                // Node size based on selection and activation
                val nodeSize = 30f * (1f + 0.3f * node.activationLevel) * (if (isSelected) 1.2f else 1f) * (if (isDragged) 1.1f else 1f)

                // Draw glow effect for active nodes
                if (node.activationLevel > 0.1f) {
                    drawCircle(
                        color = nodeColor.copy(alpha = 0.3f * node.activationLevel),
                        radius = nodeSize * 2f,
                        center = node.position
                    )

                    drawCircle(
                        color = nodeColor.copy(alpha = 0.2f * node.activationLevel),
                        radius = nodeSize * 3f,
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
                    color = if (isSelected) Color.White else Color.Gray.copy(alpha = 0.7f),
                    radius = nodeSize,
                    center = node.position,
                    style = Stroke(width = if (isSelected) 3f else 1f)
                )

                // Draw node type indicator
                when (node.type) {
                    NeuralNodeType.INPUT -> {
                        // Draw triangle
                        val triangleSize = nodeSize * 0.5f
                        val trianglePath = Path().apply {
                            moveTo(node.position.x, node.position.y - triangleSize)
                            lineTo(node.position.x - triangleSize, node.position.y + triangleSize)
                            lineTo(node.position.x + triangleSize, node.position.y + triangleSize)
                            close()
                        }
                        drawPath(
                            path = trianglePath,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    NeuralNodeType.OUTPUT -> {
                        // Draw circle
                        drawCircle(
                            color = Color.White.copy(alpha = 0.8f),
                            radius = nodeSize * 0.3f,
                            center = node.position
                        )
                    }
                    NeuralNodeType.HIDDEN -> {
                        // Draw cross
                        val crossSize = nodeSize * 0.4f
                        drawLine(
                            color = Color.White.copy(alpha = 0.8f),
                            start = Offset(node.position.x - crossSize, node.position.y),
                            end = Offset(node.position.x + crossSize, node.position.y),
                            strokeWidth = 2f
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.8f),
                            start = Offset(node.position.x, node.position.y - crossSize),
                            end = Offset(node.position.x, node.position.y + crossSize),
                            strokeWidth = 2f
                        )
                    }
                    NeuralNodeType.BIAS -> {
                        // Draw plus
                        val plusSize = nodeSize * 0.4f
                        drawLine(
                            color = Color.Black.copy(alpha = 0.8f),
                            start = Offset(node.position.x - plusSize, node.position.y),
                            end = Offset(node.position.x + plusSize, node.position.y),
                            strokeWidth = 3f
                        )
                        drawLine(
                            color = Color.Black.copy(alpha = 0.8f),
                            start = Offset(node.position.x, node.position.y - plusSize),
                            end = Offset(node.position.x, node.position.y + plusSize),
                            strokeWidth = 3f
                        )
                    }
                    NeuralNodeType.RECURRENT -> {
                        // Draw circular arrow
                        val arrowRadius = nodeSize * 0.4f
                        val arrowPath = Path().apply {
                            addArc(
                                oval = androidx.compose.ui.geometry.Rect(
                                    left = node.position.x - arrowRadius,
                                    top = node.position.y - arrowRadius,
                                    right = node.position.x + arrowRadius,
                                    bottom = node.position.y + arrowRadius
                                ),
                                startAngleDegrees = 0f,
                                sweepAngleDegrees = 270f
                            )
                        }
                        drawPath(
                            path = arrowPath,
                            color = Color.White.copy(alpha = 0.8f),
                            style = Stroke(width = 2f)
                        )
                    }
                }
            }
        }

        // Controls
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            IconButton(
                onClick = {
                    // TODO: Implement clear all nodes functionality
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear All",
                    tint = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            IconButton(
                onClick = {
                    // TODO: Implement generate random neural network functionality
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Generate Network",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            IconButton(
                onClick = {
                    // Activate random input nodes
                    val inputNodes = nodes.filter { it.type == NeuralNodeType.INPUT }
                    if (inputNodes.isNotEmpty()) {
                        val randomInput = inputNodes.random()
                        viewModel.activateNode(randomInput.id)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Stimulate Network",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Node info for selected node
        selectedNodeId?.let { id ->
            val selectedNode = nodes.find { it.id == id }
            if (selectedNode != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = selectedNode.label ?: "Node",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Type: ${selectedNode.type.name}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "Activation: ${(selectedNode.activationLevel * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "Connections: ${selectedNode.connections.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    // Delete node
                                    viewModel.deleteNode(selectedNode.id)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Delete")
                            }

                            Button(
                                onClick = {
                                    // Activate node
                                    viewModel.activateNode(selectedNode.id)
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            ) {
                                Text("Activate")
                            }
                        }
                    }
                }
            }
        }
    }
}
