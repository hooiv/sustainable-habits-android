package com.example.myapplication.core.data.database

import androidx.room.*
import com.example.myapplication.core.data.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for neural network related entities
 */
@Dao
interface NeuralNetworkDao {
    // HabitNeuralNetwork operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitNeuralNetwork(network: HabitNeuralNetwork)

    @Update
    suspend fun updateHabitNeuralNetwork(network: HabitNeuralNetwork)

    @Delete
    suspend fun deleteHabitNeuralNetwork(network: HabitNeuralNetwork)

    @Query("SELECT * FROM habit_neural_networks WHERE id = :networkId")
    fun getHabitNeuralNetworkById(networkId: String): Flow<HabitNeuralNetwork?>

    @Query("SELECT * FROM habit_neural_networks WHERE habitId = :habitId")
    fun getHabitNeuralNetwork(habitId: String): Flow<HabitNeuralNetwork?>

    @Query("SELECT * FROM habit_neural_networks")
    fun getAllHabitNeuralNetworks(): Flow<List<HabitNeuralNetwork>>

    // NeuralNode operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: NeuralNode)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNodes(nodes: List<NeuralNode>)

    @Update
    suspend fun updateNode(node: NeuralNode)

    @Query("DELETE FROM neural_nodes WHERE id = :nodeId")
    suspend fun deleteNode(nodeId: String)

    @Query("SELECT * FROM neural_nodes WHERE id = :nodeId")
    suspend fun getNodeById(nodeId: String): NeuralNode?

    @Query("SELECT * FROM neural_nodes WHERE networkId = :networkId")
    fun getNodesForNetwork(networkId: String): Flow<List<NeuralNode>>

    // NeuralConnection operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnection(connection: NeuralConnection)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnections(connections: List<NeuralConnection>)

    @Update
    suspend fun updateConnection(connection: NeuralConnection)

    @Delete
    suspend fun deleteConnection(connection: NeuralConnection)

    @Query("DELETE FROM neural_connections WHERE sourceNodeId = :sourceNodeId AND targetNodeId = :targetNodeId")
    suspend fun deleteConnectionByNodes(sourceNodeId: String, targetNodeId: String)

    @Query("DELETE FROM neural_connections WHERE sourceNodeId = :nodeId OR targetNodeId = :nodeId")
    suspend fun deleteConnectionsByNodeId(nodeId: String)

    @Query("SELECT * FROM neural_connections WHERE networkId = :networkId")
    fun getConnectionsForNetwork(networkId: String): Flow<List<NeuralConnection>>

    // NeuralActivation operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivation(activation: NeuralActivation)

    @Query("SELECT * FROM neural_activations WHERE nodeId = :nodeId ORDER BY timestamp DESC")
    fun getActivationsForNode(nodeId: String): Flow<List<NeuralActivation>>

    // NeuralTrainingSession operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainingSession(session: NeuralTrainingSession)

    @Update
    suspend fun updateTrainingSession(session: NeuralTrainingSession)

    @Query("SELECT * FROM neural_training_sessions WHERE networkId = :networkId ORDER BY startTime DESC")
    fun getTrainingSessionsForNetwork(networkId: String): Flow<List<NeuralTrainingSession>>

    // NeuralTrainingEpoch operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainingEpoch(epoch: NeuralTrainingEpoch)

    @Query("SELECT * FROM neural_training_epochs WHERE sessionId = :sessionId ORDER BY epochNumber")
    fun getEpochsForSession(sessionId: String): Flow<List<NeuralTrainingEpoch>>

    // NeuralPrediction operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: NeuralPrediction)

    @Query("SELECT * FROM neural_predictions WHERE habitId = :habitId ORDER BY timestamp DESC")
    fun getPredictionsForHabit(habitId: String): Flow<List<NeuralPrediction>>

    // Complex queries with relationships
    @Transaction
    @Query("SELECT * FROM habit_neural_networks WHERE habitId = :habitId")
    fun getHabitNeuralNetworkWithNodesAndConnections(habitId: String): Flow<NetworkWithNodesAndConnections?>

    @Transaction
    @Query("SELECT * FROM neural_nodes WHERE id = :nodeId")
    fun getNodeWithConnections(nodeId: String): Flow<NodeWithConnections?>

    @Transaction
    @Query("""
        SELECT * FROM neural_predictions 
        WHERE habitId = :habitId 
        AND predictionType = :predictionType 
        ORDER BY timestamp DESC 
        LIMIT 1
    """)
    fun getLatestPredictionForHabit(habitId: String, predictionType: PredictionType): Flow<NeuralPrediction?>

    @Query("""
        SELECT * FROM neural_training_sessions 
        WHERE networkId = :networkId 
        AND status = 'COMPLETED' 
        ORDER BY endTime DESC 
        LIMIT 1
    """)
    fun getLatestCompletedTrainingSession(networkId: String): Flow<NeuralTrainingSession?>

    @Query("""
        SELECT * FROM neural_nodes 
        WHERE networkId = :networkId 
        AND activationLevel > 0.1 
        ORDER BY activationLevel DESC
    """)
    fun getActiveNodesForNetwork(networkId: String): Flow<List<NeuralNode>>

    @Query("""
        SELECT COUNT(*) FROM neural_connections 
        WHERE networkId = :networkId
    """)
    fun getConnectionCountForNetwork(networkId: String): Flow<Int>

    @Query("""
        SELECT AVG(weight) FROM neural_connections 
        WHERE networkId = :networkId
    """)
    fun getAverageWeightForNetwork(networkId: String): Flow<Float?>

    @Query("""
        SELECT * FROM neural_nodes 
        WHERE networkId = :networkId 
        AND type = :nodeType
    """)
    fun getNodesByType(networkId: String, nodeType: NeuralNodeType): Flow<List<NeuralNode>>
}
