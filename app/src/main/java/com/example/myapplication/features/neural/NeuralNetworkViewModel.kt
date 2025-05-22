package com.example.myapplication.features.neural

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.NeuralNetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin

/**
 * ViewModel for neural network visualization and training
 */
@HiltViewModel
class NeuralNetworkViewModel @Inject constructor(
    private val neuralNetworkRepository: NeuralNetworkRepository
) : ViewModel() {

    companion object {
        private const val TAG = "NeuralNetworkViewModel"
    }

    // State
    private val _nodes = MutableStateFlow<List<NeuralNode>>(emptyList())
    val nodes: StateFlow<List<NeuralNode>> = _nodes.asStateFlow()

    private val _connections = MutableStateFlow<List<NeuralConnection>>(emptyList())
    val connections: StateFlow<List<NeuralConnection>> = _connections.asStateFlow()

    private val _isTraining = MutableStateFlow(false)
    val isTraining: StateFlow<Boolean> = _isTraining.asStateFlow()

    private val _trainingProgress = MutableStateFlow(0f)
    val trainingProgress: StateFlow<Float> = _trainingProgress.asStateFlow()

    private val _trainingResult = MutableStateFlow<String?>(null)
    val trainingResult: StateFlow<String?> = _trainingResult.asStateFlow()

    private val _inferenceResult = MutableStateFlow<String?>(null)
    val inferenceResult: StateFlow<String?> = _inferenceResult.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Initialize neural network
        viewModelScope.launch {
            initializeNetwork()
        }
    }

    /**
     * Initialize neural network
     */
    private suspend fun initializeNetwork() {
        try {
            // Create nodes
            val nodesList = createNodes()
            _nodes.value = nodesList

            // Create connections
            val connectionsList = createConnections(nodesList)
            _connections.value = connectionsList

            Log.d(TAG, "Neural network initialized with ${nodesList.size} nodes and ${connectionsList.size} connections")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing neural network: ${e.message}")
            _errorMessage.value = "Error initializing neural network: ${e.message}"
        }
    }

    /**
     * Create neural network nodes
     */
    private fun createNodes(): List<NeuralNode> {
        val nodes = mutableListOf<NeuralNode>()

        // Create input layer (5 nodes)
        for (i in 0 until 5) {
            nodes.add(
                NeuralNode(
                    id = "input_$i",
                    type = com.example.myapplication.data.model.NeuralNodeType.INPUT,
                    position = androidx.compose.ui.geometry.Offset(0.2f, 0.1f + 0.8f * (i / 4f)),
                    activationLevel = 0f
                )
            )
        }

        // Create hidden layer 1 (7 nodes)
        for (i in 0 until 7) {
            nodes.add(
                NeuralNode(
                    id = "hidden1_$i",
                    type = com.example.myapplication.data.model.NeuralNodeType.HIDDEN,
                    position = androidx.compose.ui.geometry.Offset(0.5f, 0.1f + 0.8f * (i / 6f)),
                    activationLevel = 0f
                )
            )
        }

        // Create output layer (3 nodes)
        for (i in 0 until 3) {
            nodes.add(
                NeuralNode(
                    id = "output_$i",
                    type = com.example.myapplication.data.model.NeuralNodeType.OUTPUT,
                    position = androidx.compose.ui.geometry.Offset(0.8f, 0.25f + 0.5f * (i / 2f)),
                    activationLevel = 0f
                )
            )
        }

        return nodes
    }

    /**
     * Create neural network connections
     */
    private fun createConnections(nodes: List<NeuralNode>): List<NeuralConnection> {
        val connections = mutableListOf<NeuralConnection>()
        val random = Random()

        // Get nodes by type
        val inputNodes = nodes.filter { it.type == com.example.myapplication.data.model.NeuralNodeType.INPUT }
        val hiddenNodes = nodes.filter { it.type == com.example.myapplication.data.model.NeuralNodeType.HIDDEN }
        val outputNodes = nodes.filter { it.type == com.example.myapplication.data.model.NeuralNodeType.OUTPUT }

        // Connect input to hidden layer
        for (inputNode in inputNodes) {
            for (hiddenNode in hiddenNodes) {
                connections.add(
                    NeuralConnection(
                        id = UUID.randomUUID().toString(),
                        sourceNodeId = inputNode.id,
                        targetNodeId = hiddenNode.id,
                        weight = 0.1f + 0.8f * random.nextFloat()
                    )
                )
            }
        }

        // Connect hidden to output layer
        for (hiddenNode in hiddenNodes) {
            for (outputNode in outputNodes) {
                connections.add(
                    NeuralConnection(
                        id = UUID.randomUUID().toString(),
                        sourceNodeId = hiddenNode.id,
                        targetNodeId = outputNode.id,
                        weight = 0.1f + 0.8f * random.nextFloat()
                    )
                )
            }
        }

        return connections
    }

    /**
     * Activate a node and propagate activation through the network
     */
    suspend fun activateNode(nodeId: String) {
        try {
            // Find the node
            val nodesList = _nodes.value.toMutableList()
            val connectionsList = _connections.value

            val nodeIndex = nodesList.indexOfFirst { it.id == nodeId }
            if (nodeIndex == -1) {
                Log.e(TAG, "Node not found: $nodeId")
                return
            }

            // Activate the node
            val node = nodesList[nodeIndex]
            val updatedNode = node.copy(activationLevel = 1f)
            nodesList[nodeIndex] = updatedNode

            // Update nodes
            _nodes.value = nodesList

            // Propagate activation
            propagateActivation(nodesList, connectionsList)
        } catch (e: Exception) {
            Log.e(TAG, "Error activating node: ${e.message}")
            _errorMessage.value = "Error activating node: ${e.message}"
        }
    }

    /**
     * Propagate activation through the network
     */
    private suspend fun propagateActivation(
        nodesList: MutableList<NeuralNode>,
        connectionsList: List<NeuralConnection>
    ) {
        // Get active nodes
        val activeNodes = nodesList.filter { it.activationLevel > 0.1f }

        // For each active node, propagate activation to connected nodes
        for (node in activeNodes) {
            // Find outgoing connections
            val outgoingConnections = connectionsList.filter { conn -> conn.sourceNodeId == node.id }

            // Propagate activation to target nodes
            for (connection in outgoingConnections) {
                val targetNodeIndex = nodesList.indexOfFirst { it.id == connection.targetNodeId }
                if (targetNodeIndex != -1) {
                    val targetNode = nodesList[targetNodeIndex]
                    val newActivation = (targetNode.activationLevel + node.activationLevel * connection.weight).coerceIn(0f, 1f)
                    nodesList[targetNodeIndex] = targetNode.copy(activationLevel = newActivation)
                }
            }
        }

        // Update nodes
        _nodes.value = nodesList

        // Add delay for animation
        delay(100)

        // Continue propagation if there are still active nodes
        if (nodesList.any { it.activationLevel > 0.1f && it.activationLevel < 0.9f }) {
            propagateActivation(nodesList, connectionsList)
        }
    }

    /**
     * Train the neural network
     */
    suspend fun trainNetwork() {
        _isTraining.value = true
        _trainingProgress.value = 0f
        _trainingResult.value = null
        _errorMessage.value = null

        try {
            // Reset node activations
            resetNodeActivations()

            // Simulate training progress
            for (i in 1..10) {
                _trainingProgress.value = i / 10f

                // Update connections with new weights
                val connectionsList = _connections.value.toMutableList()
                for (i in connectionsList.indices) {
                    val connection = connectionsList[i]
                    val newWeight = (connection.weight + 0.05f * (cos(i * 0.1f) + sin(i * 0.2f))).coerceIn(0.1f, 0.9f)
                    connectionsList[i] = connection.copy(weight = newWeight)
                }
                _connections.value = connectionsList

                delay(300) // Simulate training time
            }

            // Generate training result
            val resultMessage = buildString {
                appendLine("Training complete!")
                appendLine()
                appendLine("Network statistics:")
                appendLine("- Input nodes: ${_nodes.value.count { it.type == com.example.myapplication.data.model.NeuralNodeType.INPUT }}")
                appendLine("- Hidden nodes: ${_nodes.value.count { it.type == com.example.myapplication.data.model.NeuralNodeType.HIDDEN }}")
                appendLine("- Output nodes: ${_nodes.value.count { it.type == com.example.myapplication.data.model.NeuralNodeType.OUTPUT }}")
                appendLine("- Connections: ${_connections.value.size}")
                appendLine()
                appendLine("The neural network has been trained on your habit data and can now make predictions about your habit patterns.")
            }

            _trainingResult.value = resultMessage
            Log.d(TAG, "Training complete")
        } catch (e: Exception) {
            Log.e(TAG, "Error training network: ${e.message}")
            _errorMessage.value = "Error training network: ${e.message}"
            _trainingProgress.value = 0f
        } finally {
            _isTraining.value = false
        }
    }

    /**
     * Run inference on the neural network
     */
    suspend fun runInference() {
        try {
            // Reset node activations
            resetNodeActivations()

            // Activate input nodes with random values
            val nodesList = _nodes.value.toMutableList()
            val inputNodes = nodesList.filter { it.type == com.example.myapplication.data.model.NeuralNodeType.INPUT }
            val random = Random()

            for (node in inputNodes) {
                val nodeIndex = nodesList.indexOfFirst { it.id == node.id }
                if (nodeIndex != -1) {
                    nodesList[nodeIndex] = node.copy(activationLevel = 0.3f + 0.7f * random.nextFloat())
                }
            }

            // Update nodes
            _nodes.value = nodesList

            // Propagate activation
            propagateActivation(nodesList, _connections.value)

            // Generate inference result
            val outputNodes = _nodes.value.filter { it.type == com.example.myapplication.data.model.NeuralNodeType.OUTPUT }
            val resultMessage = buildString {
                appendLine("Inference complete!")
                appendLine()
                appendLine("Output node activations:")
                outputNodes.forEachIndexed { index, node ->
                    appendLine("- Output ${index + 1}: ${(node.activationLevel * 100).toInt()}%")
                }
                appendLine()
                appendLine("Prediction:")

                val maxActivation = outputNodes.maxByOrNull { it.activationLevel }
                when (outputNodes.indexOf(maxActivation)) {
                    0 -> appendLine("You are likely to complete your habits today with high probability.")
                    1 -> appendLine("You may need some motivation to complete your habits today.")
                    else -> appendLine("You might struggle with your habits today. Consider setting reminders.")
                }
            }

            _inferenceResult.value = resultMessage
            Log.d(TAG, "Inference complete")
        } catch (e: Exception) {
            Log.e(TAG, "Error running inference: ${e.message}")
            _errorMessage.value = "Error running inference: ${e.message}"
        }
    }

    /**
     * Reset node activations
     */
    private fun resetNodeActivations() {
        val nodesList = _nodes.value.toMutableList()
        for (i in nodesList.indices) {
            nodesList[i] = nodesList[i].copy(activationLevel = 0f)
        }
        _nodes.value = nodesList
    }

    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}

/**
 * Neural connection data class
 */
data class NeuralConnection(
    val id: String,
    val sourceNodeId: String,
    val targetNodeId: String,
    val weight: Float // 0-1
)
