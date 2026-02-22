package com.example.myapplication.core.data.repository

import com.example.myapplication.core.data.database.NeuralNetworkDao
import com.example.myapplication.core.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sin

/**
 * Repository for neural network data backed by Room.
 */
@Singleton
class NeuralNetworkRepository @Inject constructor(
    private val dao: NeuralNetworkDao
) {

    fun getHabitNeuralNetwork(habitId: String): Flow<NetworkWithNodesAndConnections?> =
        dao.getNetworkWithNodesAndConnectionsByHabitId(habitId)

    /** Creates a new neural network for a habit and returns its ID. */
    suspend fun createNeuralNetworkForHabit(habitId: String): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        dao.insertNetwork(
            HabitNeuralNetwork(
                id = id,
                habitId = habitId,
                name = "Network for $habitId",
                createdAt = now
            )
        )
        return id
    }

    fun getNodesForNetwork(networkId: String): Flow<List<NeuralNode>> =
        dao.getNodesForNetwork(networkId)

    /** Records an activation event for the given node. */
    suspend fun activateNode(nodeId: String) {
        val node = dao.getNodeById(nodeId) ?: return
        val activated = node.copy(activationLevel = 1f)
        dao.updateNode(activated)
        dao.insertActivation(
            NeuralActivation(
                id = UUID.randomUUID().toString(),
                nodeId = nodeId,
                timestamp = System.currentTimeMillis(),
                activationLevel = 1f
            )
        )
    }

    /** Propagates activation through the network (simple feed-forward pass). */
    suspend fun propagateActivation(networkId: String) {
        val connections = dao.getConnectionsForNetwork(networkId)
        for (conn in connections) {
            if (!conn.enabled) continue
            val source = dao.getNodeById(conn.sourceNodeId) ?: continue
            val target = dao.getNodeById(conn.targetNodeId) ?: continue
            val newActivation = (source.activationLevel * conn.weight + target.bias)
                .let { sigmoid(it) }
            dao.updateNode(target.copy(activationLevel = newActivation))
        }
    }

    /** Adds a node to the network and returns the new node's ID. */
    suspend fun addNode(
        networkId: String,
        type: NeuralNodeType,
        positionX: Float,
        positionY: Float,
        label: String? = null
    ): String {
        val id = UUID.randomUUID().toString()
        dao.insertNode(
            NeuralNode(
                id = id,
                networkId = networkId,
                type = type,
                label = label,
                positionX = positionX,
                positionY = positionY
            )
        )
        return id
    }

    suspend fun updateNode(nodeId: String, positionX: Float, positionY: Float) {
        val node = dao.getNodeById(nodeId) ?: return
        dao.updateNode(node.copy(positionX = positionX, positionY = positionY))
    }

    /** Adds a connection between two nodes. */
    suspend fun addConnection(networkId: String, sourceNodeId: String, targetNodeId: String) {
        dao.insertConnection(
            NeuralConnection(
                id = UUID.randomUUID().toString(),
                networkId = networkId,
                sourceNodeId = sourceNodeId,
                targetNodeId = targetNodeId,
                weight = (Math.random() * 2 - 1).toFloat() // random weight in [-1, 1]
            )
        )
    }

    suspend fun deleteNode(nodeId: String) = dao.deleteNodeById(nodeId)

    suspend fun deleteConnection(sourceNodeId: String, targetNodeId: String) =
        dao.deleteConnectionBySourceAndTarget(sourceNodeId, targetNodeId)

    fun getTrainingSessionsForNetwork(networkId: String): Flow<List<NeuralTrainingSession>> =
        dao.getTrainingSessionsForNetwork(networkId)

    fun getEpochsForSession(sessionId: String): Flow<List<NeuralTrainingEpoch>> =
        dao.getEpochsForSession(sessionId)

    fun getPredictionsForHabit(habitId: String): Flow<List<NeuralPrediction>> =
        dao.getPredictionsForHabit(habitId)

    /**
     * Runs a simulated training session and returns the new session ID.
     * A lightweight simulation is used since on-device ML training is out of scope.
     */
    suspend fun trainNetwork(networkId: String, epochs: Int): String {
        val sessionId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        dao.insertTrainingSession(
            NeuralTrainingSession(
                id = sessionId,
                networkId = networkId,
                startTime = now,
                epochs = epochs,
                learningRate = 0.01f,
                status = TrainingStatus.IN_PROGRESS
            )
        )

        var loss = 1f
        var accuracy = 0f
        for (epoch in 1..epochs) {
            loss *= 0.95f
            accuracy = 1f - loss
            dao.insertTrainingEpoch(
                NeuralTrainingEpoch(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    epochNumber = epoch,
                    loss = loss,
                    accuracy = accuracy,
                    timestamp = now + epoch * 10L
                )
            )
        }

        dao.updateTrainingSession(
            NeuralTrainingSession(
                id = sessionId,
                networkId = networkId,
                startTime = now,
                endTime = System.currentTimeMillis(),
                epochs = epochs,
                learningRate = 0.01f,
                finalLoss = loss,
                finalAccuracy = accuracy,
                status = TrainingStatus.COMPLETED
            )
        )

        return sessionId
    }

    // --- Helpers ---

    private fun sigmoid(x: Float): Float = (1f / (1f + Math.exp(-x.toDouble()))).toFloat()
}
