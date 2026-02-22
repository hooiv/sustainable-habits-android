package com.example.myapplication.core.data.database

import androidx.room.*
import com.example.myapplication.core.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NeuralNetworkDao {

    // --- Networks ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNetwork(network: HabitNeuralNetwork)

    @Transaction
    @Query("SELECT * FROM habit_neural_networks WHERE habitId = :habitId LIMIT 1")
    fun getNetworkWithNodesAndConnectionsByHabitId(habitId: String): Flow<NetworkWithNodesAndConnections?>

    // --- Nodes ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: NeuralNode)

    @Update
    suspend fun updateNode(node: NeuralNode)

    @Query("SELECT * FROM neural_nodes WHERE id = :nodeId")
    suspend fun getNodeById(nodeId: String): NeuralNode?

    @Query("SELECT * FROM neural_nodes WHERE networkId = :networkId")
    fun getNodesForNetwork(networkId: String): Flow<List<NeuralNode>>

    @Query("DELETE FROM neural_nodes WHERE id = :nodeId")
    suspend fun deleteNodeById(nodeId: String)

    // --- Connections ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnection(connection: NeuralConnection)

    @Query("SELECT * FROM neural_connections WHERE networkId = :networkId")
    suspend fun getConnectionsForNetwork(networkId: String): List<NeuralConnection>

    @Query("DELETE FROM neural_connections WHERE sourceNodeId = :sourceNodeId AND targetNodeId = :targetNodeId")
    suspend fun deleteConnectionBySourceAndTarget(sourceNodeId: String, targetNodeId: String)

    // --- Activations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivation(activation: NeuralActivation)

    // --- Training sessions ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainingSession(session: NeuralTrainingSession)

    @Update
    suspend fun updateTrainingSession(session: NeuralTrainingSession)

    @Query("SELECT * FROM neural_training_sessions WHERE networkId = :networkId ORDER BY startTime DESC")
    fun getTrainingSessionsForNetwork(networkId: String): Flow<List<NeuralTrainingSession>>

    // --- Training epochs ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainingEpoch(epoch: NeuralTrainingEpoch)

    @Query("SELECT * FROM neural_training_epochs WHERE sessionId = :sessionId ORDER BY epochNumber ASC")
    fun getEpochsForSession(sessionId: String): Flow<List<NeuralTrainingEpoch>>

    // --- Predictions ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: NeuralPrediction)

    @Query("SELECT * FROM neural_predictions WHERE habitId = :habitId ORDER BY timestamp DESC")
    fun getPredictionsForHabit(habitId: String): Flow<List<NeuralPrediction>>
}
