package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * Entity representing a neural network for a habit
 */
@Entity(
    tableName = "habit_neural_networks",
    indices = [Index("habitId")]
)
data class HabitNeuralNetwork(
    @PrimaryKey
    val id: String,
    val habitId: String,
    val name: String,
    val description: String? = null,
    val createdAt: Long,
    val updatedAt: Long = createdAt
)

/**
 * Enum for neural node types
 */
enum class NeuralNodeType {
    INPUT,
    HIDDEN,
    OUTPUT,
    BIAS,
    RECURRENT
}

/**
 * Entity representing a node in a neural network
 */
@Entity(
    tableName = "neural_nodes",
    foreignKeys = [
        ForeignKey(
            entity = HabitNeuralNetwork::class,
            parentColumns = ["id"],
            childColumns = ["networkId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("networkId")]
)
data class NeuralNode(
    @PrimaryKey
    val id: String,
    val networkId: String,
    val type: NeuralNodeType,
    val label: String? = null,
    val positionX: Float,
    val positionY: Float,
    val activationLevel: Float = 0f,
    val bias: Float = 0f,
    val metadata: String? = null // JSON string for additional properties
)

/**
 * Entity representing a connection between nodes in a neural network
 */
@Entity(
    tableName = "neural_connections",
    foreignKeys = [
        ForeignKey(
            entity = HabitNeuralNetwork::class,
            parentColumns = ["id"],
            childColumns = ["networkId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = NeuralNode::class,
            parentColumns = ["id"],
            childColumns = ["sourceNodeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = NeuralNode::class,
            parentColumns = ["id"],
            childColumns = ["targetNodeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("networkId"),
        Index("sourceNodeId"),
        Index("targetNodeId")
    ]
)
data class NeuralConnection(
    @PrimaryKey
    val id: String,
    val networkId: String,
    val sourceNodeId: String,
    val targetNodeId: String,
    val weight: Float,
    val enabled: Boolean = true
)

/**
 * Entity representing an activation record in a neural network
 */
@Entity(
    tableName = "neural_activations",
    foreignKeys = [
        ForeignKey(
            entity = NeuralNode::class,
            parentColumns = ["id"],
            childColumns = ["nodeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("nodeId")]
)
data class NeuralActivation(
    @PrimaryKey
    val id: String,
    val nodeId: String,
    val timestamp: Long,
    val activationLevel: Float,
    val source: String? = null // What triggered this activation
)

/**
 * Data class representing a neural network with its nodes and connections
 */
data class NetworkWithNodesAndConnections(
    val network: HabitNeuralNetwork,
    @Relation(
        parentColumn = "id",
        entityColumn = "networkId"
    )
    val nodes: List<NeuralNode>,
    @Relation(
        parentColumn = "id",
        entityColumn = "networkId"
    )
    val connections: List<NeuralConnection>
)

/**
 * Data class representing a node with its connections
 */
data class NodeWithConnections(
    val node: NeuralNode,
    @Relation(
        parentColumn = "id",
        entityColumn = "sourceNodeId"
    )
    val outgoingConnections: List<NeuralConnection>,
    @Relation(
        parentColumn = "id",
        entityColumn = "targetNodeId"
    )
    val incomingConnections: List<NeuralConnection>
)

/**
 * Data class representing a neural network training session
 */
@Entity(
    tableName = "neural_training_sessions",
    foreignKeys = [
        ForeignKey(
            entity = HabitNeuralNetwork::class,
            parentColumns = ["id"],
            childColumns = ["networkId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("networkId")]
)
data class NeuralTrainingSession(
    @PrimaryKey
    val id: String,
    val networkId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val epochs: Int = 0,
    val learningRate: Float = 0.01f,
    val finalLoss: Float? = null,
    val finalAccuracy: Float? = null,
    val status: TrainingStatus = TrainingStatus.PENDING
)

/**
 * Enum for training status
 */
enum class TrainingStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * Entity representing a training epoch
 */
@Entity(
    tableName = "neural_training_epochs",
    foreignKeys = [
        ForeignKey(
            entity = NeuralTrainingSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class NeuralTrainingEpoch(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val epochNumber: Int,
    val loss: Float,
    val accuracy: Float,
    val timestamp: Long
)

/**
 * Data class representing a prediction made by a neural network
 */
@Entity(
    tableName = "neural_predictions",
    foreignKeys = [
        ForeignKey(
            entity = HabitNeuralNetwork::class,
            parentColumns = ["id"],
            childColumns = ["networkId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("networkId"),
        Index("habitId")
    ]
)
data class NeuralPrediction(
    @PrimaryKey
    val id: String,
    val networkId: String,
    val habitId: String,
    val timestamp: Long,
    val predictionType: PredictionType,
    val probability: Float,
    val confidence: Float,
    val metadata: String? = null // JSON string for additional properties
)

/**
 * Enum for prediction types
 */
enum class PredictionType {
    COMPLETION_LIKELIHOOD,
    STREAK_CONTINUATION,
    HABIT_FORMATION,
    HABIT_ABANDONMENT,
    OPTIMAL_TIME,
    DIFFICULTY_CHANGE
}
