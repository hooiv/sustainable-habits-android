package com.example.myapplication.data.repository

import com.example.myapplication.data.database.NeuralNetworkDao
import com.example.myapplication.data.ml.NeuralNetworkTrainer
import com.example.myapplication.data.model.*
import com.example.myapplication.features.neural.NeuralNode as UINeuralNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import androidx.compose.ui.geometry.Offset

/**
 * Repository for managing neural networks related to habits
 */
@Singleton
class NeuralNetworkRepository @Inject constructor(
    private val neuralNetworkDao: NeuralNetworkDao,
    private val habitRepository: HabitRepository,
    private val neuralNetworkTrainer: NeuralNetworkTrainer
) {
    /**
     * Get the neural network for a specific habit
     */
    fun getHabitNeuralNetwork(habitId: String): Flow<HabitNeuralNetwork?> {
        return neuralNetworkDao.getHabitNeuralNetwork(habitId)
            .flowOn(Dispatchers.IO)
    }

    /**
     * Get all neural nodes for a specific neural network
     */
    fun getNodesForNetwork(networkId: String): Flow<List<NeuralNode>> {
        return neuralNetworkDao.getNodesForNetwork(networkId)
            .flowOn(Dispatchers.IO)
    }

    /**
     * Get all connections for a specific neural network
     */
    fun getConnectionsForNetwork(networkId: String): Flow<List<NeuralConnection>> {
        return neuralNetworkDao.getConnectionsForNetwork(networkId)
            .flowOn(Dispatchers.IO)
    }

    /**
     * Convert database neural nodes to UI neural nodes
     */
    fun getUINodesForHabit(habitId: String): Flow<List<UINeuralNode>> {
        return neuralNetworkDao.getHabitNeuralNetworkWithNodesAndConnections(habitId)
            .map { networkWithNodesAndConnections ->
                if (networkWithNodesAndConnections == null) {
                    emptyList()
                } else {
                    val nodes = networkWithNodesAndConnections.nodes
                    val connections = networkWithNodesAndConnections.connections

                    // Create a map of node connections
                    val nodeConnections = connections.groupBy { it.sourceNodeId }

                    // Convert database nodes to UI nodes
                    nodes.map { node ->
                        UINeuralNode(
                            id = node.id,
                            type = when (node.type) {
                                NeuralNodeType.INPUT -> com.example.myapplication.features.neural.NeuralNodeType.INPUT
                                NeuralNodeType.HIDDEN -> com.example.myapplication.features.neural.NeuralNodeType.HIDDEN
                                NeuralNodeType.OUTPUT -> com.example.myapplication.features.neural.NeuralNodeType.OUTPUT
                                NeuralNodeType.BIAS -> com.example.myapplication.features.neural.NeuralNodeType.BIAS
                                NeuralNodeType.RECURRENT -> com.example.myapplication.features.neural.NeuralNodeType.RECURRENT
                            },
                            position = Offset(node.positionX, node.positionY),
                            connections = nodeConnections[node.id]?.map { it.targetNodeId } ?: mutableListOf(),
                            activationLevel = node.activationLevel,
                            label = node.label
                        )
                    }
                }
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Create a new neural network for a habit
     */
    suspend fun createNeuralNetworkForHabit(habitId: String): String = withContext(Dispatchers.IO) {
        val networkId = UUID.randomUUID().toString()
        val network = HabitNeuralNetwork(
            id = networkId,
            habitId = habitId,
            name = "Habit Neural Network",
            createdAt = System.currentTimeMillis()
        )

        neuralNetworkDao.insertHabitNeuralNetwork(network)

        // Create default nodes
        createDefaultNodesForNetwork(networkId, habitId)

        return@withContext networkId
    }

    /**
     * Create default nodes for a new neural network
     */
    private suspend fun createDefaultNodesForNetwork(networkId: String, habitId: String) {
        val habit = habitRepository.getHabitById(habitId).map { it }.flowOn(Dispatchers.IO)

        // Create input nodes
        val triggerNode = NeuralNode(
            id = UUID.randomUUID().toString(),
            networkId = networkId,
            type = NeuralNodeType.INPUT,
            label = "Habit Trigger",
            positionX = 100f,
            positionY = 100f,
            activationLevel = 0.0f
        )

        val environmentNode = NeuralNode(
            id = UUID.randomUUID().toString(),
            networkId = networkId,
            type = NeuralNodeType.INPUT,
            label = "Environment",
            positionX = 100f,
            positionY = 250f,
            activationLevel = 0.0f
        )

        // Create hidden nodes
        val motivationNode = NeuralNode(
            id = UUID.randomUUID().toString(),
            networkId = networkId,
            type = NeuralNodeType.HIDDEN,
            label = "Motivation",
            positionX = 300f,
            positionY = 150f,
            activationLevel = 0.0f
        )

        val difficultyNode = NeuralNode(
            id = UUID.randomUUID().toString(),
            networkId = networkId,
            type = NeuralNodeType.HIDDEN,
            label = "Difficulty",
            positionX = 300f,
            positionY = 300f,
            activationLevel = 0.0f
        )

        // Create output node
        val completionNode = NeuralNode(
            id = UUID.randomUUID().toString(),
            networkId = networkId,
            type = NeuralNodeType.OUTPUT,
            label = "Habit Completion",
            positionX = 500f,
            positionY = 200f,
            activationLevel = 0.0f
        )

        // Insert all nodes
        val nodes = listOf(triggerNode, environmentNode, motivationNode, difficultyNode, completionNode)
        neuralNetworkDao.insertNodes(nodes)

        // Create connections
        val connections = listOf(
            NeuralConnection(
                id = UUID.randomUUID().toString(),
                networkId = networkId,
                sourceNodeId = triggerNode.id,
                targetNodeId = motivationNode.id,
                weight = 0.7f
            ),
            NeuralConnection(
                id = UUID.randomUUID().toString(),
                networkId = networkId,
                sourceNodeId = triggerNode.id,
                targetNodeId = difficultyNode.id,
                weight = 0.3f
            ),
            NeuralConnection(
                id = UUID.randomUUID().toString(),
                networkId = networkId,
                sourceNodeId = environmentNode.id,
                targetNodeId = motivationNode.id,
                weight = 0.5f
            ),
            NeuralConnection(
                id = UUID.randomUUID().toString(),
                networkId = networkId,
                sourceNodeId = motivationNode.id,
                targetNodeId = completionNode.id,
                weight = 0.8f
            ),
            NeuralConnection(
                id = UUID.randomUUID().toString(),
                networkId = networkId,
                sourceNodeId = difficultyNode.id,
                targetNodeId = completionNode.id,
                weight = -0.6f
            )
        )

        neuralNetworkDao.insertConnections(connections)
    }

    /**
     * Update a neural node
     */
    suspend fun updateNode(node: UINeuralNode) = withContext(Dispatchers.IO) {
        val dbNode = neuralNetworkDao.getNodeById(node.id)
        if (dbNode != null) {
            val updatedNode = dbNode.copy(
                positionX = node.position.x,
                positionY = node.position.y,
                activationLevel = node.activationLevel,
                label = node.label
            )
            neuralNetworkDao.updateNode(updatedNode)
        }
    }

    /**
     * Add a new node to the network
     */
    suspend fun addNode(
        networkId: String,
        type: com.example.myapplication.features.neural.NeuralNodeType,
        position: Offset,
        label: String?
    ): String = withContext(Dispatchers.IO) {
        val nodeId = UUID.randomUUID().toString()
        val node = NeuralNode(
            id = nodeId,
            networkId = networkId,
            type = when (type) {
                com.example.myapplication.features.neural.NeuralNodeType.INPUT -> NeuralNodeType.INPUT
                com.example.myapplication.features.neural.NeuralNodeType.HIDDEN -> NeuralNodeType.HIDDEN
                com.example.myapplication.features.neural.NeuralNodeType.OUTPUT -> NeuralNodeType.OUTPUT
                com.example.myapplication.features.neural.NeuralNodeType.BIAS -> NeuralNodeType.BIAS
                com.example.myapplication.features.neural.NeuralNodeType.RECURRENT -> NeuralNodeType.RECURRENT
            },
            label = label,
            positionX = position.x,
            positionY = position.y,
            activationLevel = 0.0f
        )

        neuralNetworkDao.insertNode(node)
        return@withContext nodeId
    }

    /**
     * Add a connection between nodes
     */
    suspend fun addConnection(
        networkId: String,
        sourceNodeId: String,
        targetNodeId: String,
        weight: Float = 0.5f
    ): String = withContext(Dispatchers.IO) {
        val connectionId = UUID.randomUUID().toString()
        val connection = NeuralConnection(
            id = connectionId,
            networkId = networkId,
            sourceNodeId = sourceNodeId,
            targetNodeId = targetNodeId,
            weight = weight
        )

        neuralNetworkDao.insertConnection(connection)
        return@withContext connectionId
    }

    /**
     * Delete a node and its connections
     */
    suspend fun deleteNode(nodeId: String) = withContext(Dispatchers.IO) {
        neuralNetworkDao.deleteNode(nodeId)
        neuralNetworkDao.deleteConnectionsByNodeId(nodeId)
    }

    /**
     * Delete a connection
     */
    suspend fun deleteConnection(sourceNodeId: String, targetNodeId: String) = withContext(Dispatchers.IO) {
        neuralNetworkDao.deleteConnectionByNodes(sourceNodeId, targetNodeId)
    }

    /**
     * Activate a node
     */
    suspend fun activateNode(nodeId: String, activationLevel: Float = 1.0f) = withContext(Dispatchers.IO) {
        val node = neuralNetworkDao.getNodeById(nodeId)
        if (node != null) {
            val updatedNode = node.copy(activationLevel = activationLevel)
            neuralNetworkDao.updateNode(updatedNode)
        }
    }

    /**
     * Propagate activation through the network
     */
    suspend fun propagateActivation(networkId: String) = withContext(Dispatchers.IO) {
        val nodes = neuralNetworkDao.getNodesForNetwork(networkId).map { it }
        val connections = neuralNetworkDao.getConnectionsForNetwork(networkId).map { it }

        // Get active nodes
        val activeNodes = nodes.filter { it.activationLevel > 0.1f }

        // For each active node, propagate activation to connected nodes
        for (node in activeNodes) {
            // Find outgoing connections
            val outgoingConnections = connections.filter { it.sourceNodeId == node.id }

            // Propagate activation to target nodes
            for (connection in outgoingConnections) {
                val targetNode = nodes.find { it.id == connection.targetNodeId }
                if (targetNode != null) {
                    val newActivation = (targetNode.activationLevel + node.activationLevel * connection.weight).coerceIn(0f, 1f)
                    val updatedNode = targetNode.copy(activationLevel = newActivation)
                    neuralNetworkDao.updateNode(updatedNode)
                }
            }

            // Decay current node's activation
            val decayedActivation = (node.activationLevel * 0.9f).coerceAtLeast(0f)
            val updatedNode = node.copy(activationLevel = decayedActivation)
            neuralNetworkDao.updateNode(updatedNode)
        }
    }

    /**
     * Get training sessions for a neural network
     */
    fun getTrainingSessionsForNetwork(networkId: String): Flow<List<NeuralTrainingSession>> {
        return neuralNetworkDao.getTrainingSessionsForNetwork(networkId)
            .flowOn(Dispatchers.IO)
    }

    /**
     * Get training epochs for a session
     */
    fun getEpochsForSession(sessionId: String): Flow<List<NeuralTrainingEpoch>> {
        return neuralNetworkDao.getEpochsForSession(sessionId)
            .flowOn(Dispatchers.IO)
    }

    /**
     * Get predictions for a habit
     */
    fun getPredictionsForHabit(habitId: String): Flow<List<NeuralPrediction>> {
        return neuralNetworkDao.getPredictionsForHabit(habitId)
            .flowOn(Dispatchers.IO)
    }

    /**
     * Get latest prediction of a specific type for a habit
     */
    fun getLatestPredictionForHabit(habitId: String, predictionType: PredictionType): Flow<NeuralPrediction?> {
        return neuralNetworkDao.getLatestPredictionForHabit(habitId, predictionType)
            .flowOn(Dispatchers.IO)
    }

    /**
     * Train the neural network
     */
    suspend fun trainNetwork(networkId: String, epochs: Int = 100): String = withContext(Dispatchers.IO) {
        return@withContext neuralNetworkTrainer.startTraining(networkId, epochs)
    }
}
