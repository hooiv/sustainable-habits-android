package com.example.myapplication.features.neural

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import com.example.myapplication.data.ml.*
import com.example.myapplication.data.model.*
import java.nio.ByteBuffer
import com.example.myapplication.data.repository.HabitRepository
import com.example.myapplication.data.repository.NeuralNetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Neural Interface feature
 */
@HiltViewModel
class NeuralInterfaceViewModel @Inject constructor(
    private val neuralNetworkRepository: NeuralNetworkRepository,
    private val habitRepository: HabitRepository,
    private val contextFeatureCollector: ContextFeatureCollector,
    private val explainableAI: ExplainableAI,
    private val reinforcementLearningAgent: ReinforcementLearningAgent,
    private val personalizedRecommendationEngine: PersonalizedRecommendationEngine,
    private val abTestingManager: ABTestingManager,
    private val federatedLearningManager: FederatedLearningManager,
    private val adaptiveLearningRateOptimizer: AdaptiveLearningRateOptimizer,
    private val modelCompressor: ModelCompressor,
    private val hyperparameterOptimizer: HyperparameterOptimizer,
    private val anomalyDetector: AnomalyDetector,
    private val multiModalLearning: MultiModalLearning,
    private val metaLearning: MetaLearning,
    private val biometricIntegration: com.example.myapplication.data.biometric.BiometricIntegration,
    private val spatialComputing: com.example.myapplication.data.spatial.SpatialComputing,
    private val voiceAndNlpProcessor: com.example.myapplication.data.nlp.VoiceAndNlpProcessor,
    private val quantumVisualizer: com.example.myapplication.data.quantum.QuantumVisualizer
) : ViewModel() {

    // State for the current habit ID
    private val _currentHabitId = MutableStateFlow<String?>(null)
    val currentHabitId: StateFlow<String?> = _currentHabitId.asStateFlow()

    // State for the current network ID
    private val _currentNetworkId = MutableStateFlow<String?>(null)
    val currentNetworkId: StateFlow<String?> = _currentNetworkId.asStateFlow()

    // State for neural nodes
    private val _nodes = MutableStateFlow<List<NeuralNode>>(emptyList())
    val nodes: StateFlow<List<NeuralNode>> = _nodes.asStateFlow()

    // State for loading status
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // State for error messages
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // State for selected node
    private val _selectedNodeId = MutableStateFlow<String?>(null)
    val selectedNodeId: StateFlow<String?> = _selectedNodeId.asStateFlow()

    // State for network status
    private val _networkStatus = MutableStateFlow<NetworkStatus>(NetworkStatus.Idle)
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    // State for training sessions
    private val _trainingSessions = MutableStateFlow<List<NeuralTrainingSession>>(emptyList())
    val trainingSessions: StateFlow<List<NeuralTrainingSession>> = _trainingSessions.asStateFlow()

    // State for current training session
    private val _currentTrainingSession = MutableStateFlow<NeuralTrainingSession?>(null)
    val currentTrainingSession: StateFlow<NeuralTrainingSession?> = _currentTrainingSession.asStateFlow()

    // State for training epochs
    private val _trainingEpochs = MutableStateFlow<List<NeuralTrainingEpoch>>(emptyList())
    val trainingEpochs: StateFlow<List<NeuralTrainingEpoch>> = _trainingEpochs.asStateFlow()

    // State for predictions
    private val _predictions = MutableStateFlow<List<NeuralPrediction>>(emptyList())
    val predictions: StateFlow<List<NeuralPrediction>> = _predictions.asStateFlow()

    // State for habit completions
    private val _habitCompletions = MutableStateFlow<List<HabitCompletion>>(emptyList())
    val habitCompletions: StateFlow<List<HabitCompletion>> = _habitCompletions.asStateFlow()

    // State for recommendations
    private val _recommendations = MutableStateFlow<List<HabitRecommendation>>(emptyList())
    val recommendations: StateFlow<List<HabitRecommendation>> = _recommendations.asStateFlow()

    // State for feature importance (explainable AI)
    private val _featureImportance = MutableStateFlow<Map<String, Float>>(emptyMap())
    val featureImportance: StateFlow<Map<String, Float>> = _featureImportance.asStateFlow()

    // State for explanation text
    private val _explanation = MutableStateFlow<String>("")
    val explanation: StateFlow<String> = _explanation.asStateFlow()

    // State for reinforcement learning action
    private val _reinforcementAction = MutableStateFlow<ReinforcementAction?>(null)
    val reinforcementAction: StateFlow<ReinforcementAction?> = _reinforcementAction.asStateFlow()

    // State for A/B testing results
    private val _testResults = MutableStateFlow<Map<String, TestResult>>(emptyMap())
    val testResults: StateFlow<Map<String, TestResult>> = _testResults.asStateFlow()

    // State for current model variant
    private val _currentVariant = MutableStateFlow<String>(ABTestingManager.VARIANT_CONTROL)
    val currentVariant: StateFlow<String> = _currentVariant.asStateFlow()

    // State for imported model count
    private val _importedModelCount = MutableStateFlow<Int>(0)
    val importedModelCount: StateFlow<Int> = _importedModelCount.asStateFlow()

    // State for model compression
    private val _compressionStats = MutableStateFlow(CompressionStats(0, 0, 0f, 0, 0f))
    val compressionStats: StateFlow<CompressionStats> = _compressionStats.asStateFlow()

    // State for hyperparameter optimization
    private val _trialResults = MutableStateFlow<List<TrialResult>>(emptyList())
    val trialResults: StateFlow<List<TrialResult>> = _trialResults.asStateFlow()

    private val _bestHyperparameters = MutableStateFlow<Hyperparameters?>(null)
    val bestHyperparameters: StateFlow<Hyperparameters?> = _bestHyperparameters.asStateFlow()

    private val _hyperparameterImportance = MutableStateFlow<Map<String, Float>>(emptyMap())
    val hyperparameterImportance: StateFlow<Map<String, Float>> = _hyperparameterImportance.asStateFlow()

    // State for anomaly detection
    private val _anomalies = MutableStateFlow<List<HabitAnomaly>>(emptyList())
    val anomalies: StateFlow<List<HabitAnomaly>> = _anomalies.asStateFlow()

    // State for multi-modal learning
    private val _hasImageFeatures = MutableStateFlow(false)
    val hasImageFeatures: StateFlow<Boolean> = _hasImageFeatures.asStateFlow()

    private val _hasTextFeatures = MutableStateFlow(false)
    val hasTextFeatures: StateFlow<Boolean> = _hasTextFeatures.asStateFlow()

    private val _hasSensorFeatures = MutableStateFlow(false)
    val hasSensorFeatures: StateFlow<Boolean> = _hasSensorFeatures.asStateFlow()

    // State for meta-learning
    private val _metaLearningProgress = MutableStateFlow(0f)
    val metaLearningProgress: StateFlow<Float> = _metaLearningProgress.asStateFlow()

    private val _adaptationProgress = MutableStateFlow(0f)
    val adaptationProgress: StateFlow<Float> = _adaptationProgress.asStateFlow()

    // State for biometric integration
    val biometricData = biometricIntegration.getBiometricData()
    val isMonitoring = biometricIntegration.isMonitoring

    // State for spatial computing
    val spatialObjects = spatialComputing.spatialObjects
    val isSpatialTrackingActive = spatialComputing.isSpatialTrackingActive

    // State for voice and NLP
    val recognizedText = voiceAndNlpProcessor.recognizedText
    val isListening = voiceAndNlpProcessor.isListening
    val isSpeaking = voiceAndNlpProcessor.isSpeaking
    val nlpIntent = voiceAndNlpProcessor.nlpIntent
    val nlpConfidence = voiceAndNlpProcessor.confidence

    // State for quantum visualization
    private val _quantumVisualization = MutableStateFlow<com.example.myapplication.data.quantum.QuantumVisualization?>(null)
    val quantumVisualization: StateFlow<com.example.myapplication.data.quantum.QuantumVisualization?> = _quantumVisualization.asStateFlow()

    /**
     * Load neural network for a habit
     */
    fun loadNeuralNetworkForHabit(habitId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _currentHabitId.value = habitId

            try {
                // Check if a neural network exists for this habit
                val network = neuralNetworkRepository.getHabitNeuralNetwork(habitId)
                    .first()

                if (network != null) {
                    // Network exists, load it
                    _currentNetworkId.value = network.id
                    loadNodesForNetwork(network.id)
                } else {
                    // Network doesn't exist, create a new one
                    val networkId = neuralNetworkRepository.createNeuralNetworkForHabit(habitId)
                    _currentNetworkId.value = networkId
                    loadNodesForNetwork(networkId)

                    // Load training sessions, predictions, and completions
                    loadTrainingSessions(networkId)
                    loadPredictions(habitId)
                    loadHabitCompletions(habitId)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load neural network: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load nodes for a neural network
     */
    private fun loadNodesForNetwork(networkId: String) {
        viewModelScope.launch {
            try {
                neuralNetworkRepository.getUINodesForHabit(_currentHabitId.value ?: return@launch)
                    .collect { nodes ->
                        _nodes.value = nodes
                    }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load nodes: ${e.message}"
            }
        }
    }

    /**
     * Select a node
     */
    fun selectNode(nodeId: String?) {
        _selectedNodeId.value = nodeId
    }

    /**
     * Activate a node
     */
    fun activateNode(nodeId: String) {
        viewModelScope.launch {
            try {
                val networkId = _currentNetworkId.value ?: return@launch

                // Activate the node in the database
                neuralNetworkRepository.activateNode(nodeId)

                // Update the UI state
                val updatedNodes = _nodes.value.map { node ->
                    if (node.id == nodeId) {
                        node.copy(activationLevel = 1.0f)
                    } else {
                        node
                    }
                }
                _nodes.value = updatedNodes

                // Start propagation simulation
                _networkStatus.value = NetworkStatus.Propagating

                // Propagate activation through the network
                neuralNetworkRepository.propagateActivation(networkId)

                // Reload nodes to reflect propagation
                loadNodesForNetwork(networkId)

                // Update network status
                _networkStatus.value = NetworkStatus.Idle
            } catch (e: Exception) {
                _errorMessage.value = "Failed to activate node: ${e.message}"
                _networkStatus.value = NetworkStatus.Error
            }
        }
    }

    /**
     * Add a new node
     */
    fun addNode(type: NeuralNodeType, position: Offset, label: String? = null) {
        viewModelScope.launch {
            try {
                val networkId = _currentNetworkId.value ?: return@launch

                // Add the node to the database
                val nodeId = neuralNetworkRepository.addNode(networkId, type, position, label)

                // Reload nodes to reflect the new node
                loadNodesForNetwork(networkId)

                // Select the new node
                selectNode(nodeId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add node: ${e.message}"
            }
        }
    }

    /**
     * Update a node's position
     */
    fun updateNodePosition(nodeId: String, position: Offset) {
        viewModelScope.launch {
            try {
                // Find the node in the current list
                val node = _nodes.value.find { it.id == nodeId } ?: return@launch

                // Update the node with the new position
                val updatedNode = node.copy(position = position)

                // Update the node in the database
                neuralNetworkRepository.updateNode(updatedNode)

                // Update the UI state
                val updatedNodes = _nodes.value.map {
                    if (it.id == nodeId) updatedNode else it
                }
                _nodes.value = updatedNodes
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update node position: ${e.message}"
            }
        }
    }

    /**
     * Add a connection between nodes
     */
    fun addConnection(sourceNodeId: String, targetNodeId: String) {
        viewModelScope.launch {
            try {
                val networkId = _currentNetworkId.value ?: return@launch

                // Add the connection to the database
                neuralNetworkRepository.addConnection(networkId, sourceNodeId, targetNodeId)

                // Reload nodes to reflect the new connection
                loadNodesForNetwork(networkId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add connection: ${e.message}"
            }
        }
    }

    /**
     * Delete a node
     */
    fun deleteNode(nodeId: String) {
        viewModelScope.launch {
            try {
                // Delete the node from the database
                neuralNetworkRepository.deleteNode(nodeId)

                // If the deleted node was selected, clear selection
                if (_selectedNodeId.value == nodeId) {
                    selectNode(null)
                }

                // Reload nodes to reflect the deletion
                val networkId = _currentNetworkId.value ?: return@launch
                loadNodesForNetwork(networkId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete node: ${e.message}"
            }
        }
    }

    /**
     * Delete a connection
     */
    fun deleteConnection(sourceNodeId: String, targetNodeId: String) {
        viewModelScope.launch {
            try {
                // Delete the connection from the database
                neuralNetworkRepository.deleteConnection(sourceNodeId, targetNodeId)

                // Reload nodes to reflect the deletion
                val networkId = _currentNetworkId.value ?: return@launch
                loadNodesForNetwork(networkId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete connection: ${e.message}"
            }
        }
    }

    /**
     * Load training sessions for a network
     */
    private fun loadTrainingSessions(networkId: String) {
        viewModelScope.launch {
            try {
                neuralNetworkRepository.getTrainingSessionsForNetwork(networkId)
                    .collect { sessions ->
                        _trainingSessions.value = sessions

                        // Load epochs for the most recent session
                        sessions.maxByOrNull { it.startTime }?.let { session ->
                            _currentTrainingSession.value = session
                            loadTrainingEpochs(session.id)
                        }
                    }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load training sessions: ${e.message}"
            }
        }
    }

    /**
     * Load training epochs for a session
     */
    private fun loadTrainingEpochs(sessionId: String) {
        viewModelScope.launch {
            try {
                neuralNetworkRepository.getEpochsForSession(sessionId)
                    .collect { epochs ->
                        _trainingEpochs.value = epochs
                    }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load training epochs: ${e.message}"
            }
        }
    }

    /**
     * Load predictions for a habit
     */
    private fun loadPredictions(habitId: String) {
        viewModelScope.launch {
            try {
                neuralNetworkRepository.getPredictionsForHabit(habitId)
                    .collect { predictions ->
                        _predictions.value = predictions
                    }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load predictions: ${e.message}"
            }
        }
    }

    /**
     * Train the neural network
     */
    fun trainNetwork(epochs: Int = 100) {
        viewModelScope.launch {
            try {
                _networkStatus.value = NetworkStatus.Training
                _errorMessage.value = null

                val networkId = _currentNetworkId.value
                if (networkId != null) {
                    // Start training
                    val sessionId = neuralNetworkRepository.trainNetwork(networkId, epochs)

                    // Load the new training session and epochs
                    loadTrainingSessions(networkId)

                    // Load predictions and completions
                    _currentHabitId.value?.let { habitId ->
                        loadPredictions(habitId)
                        loadHabitCompletions(habitId)
                    }

                    _networkStatus.value = NetworkStatus.Idle
                } else {
                    _errorMessage.value = "No neural network loaded"
                    _networkStatus.value = NetworkStatus.Error
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to train network: ${e.message}"
                _networkStatus.value = NetworkStatus.Error
            }
        }
    }

    /**
     * Load habit completions
     */
    private fun loadHabitCompletions(habitId: String) {
        viewModelScope.launch {
            try {
                habitRepository.getHabitCompletions(habitId)
                    .collect { completions ->
                        _habitCompletions.value = completions
                    }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load habit completions: ${e.message}"
            }
        }
    }

    /**
     * Get latest prediction of a specific type
     */
    fun getLatestPrediction(predictionType: PredictionType): NeuralPrediction? {
        return _predictions.value
            .filter { it.predictionType == predictionType }
            .maxByOrNull { it.timestamp }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Initialize advanced features
     */
    init {
        // Start collecting context features
        contextFeatureCollector.startCollecting()

        // Initialize A/B testing
        _currentVariant.value = abTestingManager.currentVariant.value

        // Update imported model count
        _importedModelCount.value = federatedLearningManager.getImportedModelCount()

        // Initialize adaptive learning rate
        adaptiveLearningRateOptimizer.reset()

        // Initialize model compression stats
        _compressionStats.value = CompressionStats(
            originalSize = 1024 * 100, // 100KB placeholder
            compressedSize = 1024 * 30, // 30KB placeholder
            compressionRatio = 3.33f,
            spaceSaved = 1024 * 70, // 70KB placeholder
            percentSaved = 70f
        )

        // Initialize hyperparameter importance
        _hyperparameterImportance.value = mapOf(
            "learningRate" to 0.4f,
            "hiddenLayerSizes" to 0.3f,
            "batchSize" to 0.2f,
            "dropoutRate" to 0.1f
        )

        // Subscribe to meta-learning progress
        viewModelScope.launch {
            metaLearning.metaLearningProgress.collect { progress ->
                _metaLearningProgress.value = progress
            }
        }

        viewModelScope.launch {
            metaLearning.adaptationProgress.collect { progress ->
                _adaptationProgress.value = progress
            }
        }
    }

    /**
     * Clean up resources when ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        contextFeatureCollector.stopCollecting()
    }

    /**
     * Process recommendation action
     */
    fun processRecommendationAction(recommendation: HabitRecommendation, followed: Boolean) {
        viewModelScope.launch {
            try {
                // Update recommendation in database (would be implemented in a real app)
                // For now, just update the UI state
                val updatedRecommendations = _recommendations.value.map {
                    if (it.id == recommendation.id) {
                        it.copy(isFollowed = followed)
                    } else {
                        it
                    }
                }
                _recommendations.value = updatedRecommendations
            } catch (e: Exception) {
                _errorMessage.value = "Failed to process recommendation: ${e.message}"
            }
        }
    }

    /**
     * Get action description for reinforcement learning
     */
    fun getActionDescription(action: ReinforcementAction): String {
        return reinforcementLearningAgent.getActionDescription(action)
    }

    /**
     * Provide feedback for reinforcement learning
     */
    fun provideReinforcementFeedback(feedback: Float) {
        viewModelScope.launch {
            try {
                reinforcementLearningAgent.provideFeedback(feedback)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to provide feedback: ${e.message}"
            }
        }
    }

    /**
     * Switch to a different model variant
     */
    fun switchModelVariant(variant: String) {
        viewModelScope.launch {
            try {
                abTestingManager.switchVariant(variant)
                _currentVariant.value = variant
            } catch (e: Exception) {
                _errorMessage.value = "Failed to switch variant: ${e.message}"
            }
        }
    }

    /**
     * Export the current model
     */
    fun exportModel() {
        viewModelScope.launch {
            try {
                val habitId = _currentHabitId.value ?: return@launch
                val networkId = _currentNetworkId.value ?: return@launch

                // Get habit category
                val habit = habitRepository.getHabitById(habitId).first() ?: return@launch

                // Export model
                val uri = federatedLearningManager.exportModel(habitId, habit.category, ByteBuffer.allocate(0))

                // In a real app, you would share this URI with other users
                // For now, just show a success message
                _errorMessage.value = "Model exported successfully"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to export model: ${e.message}"
            }
        }
    }

    /**
     * Import a model
     */
    fun importModel() {
        viewModelScope.launch {
            try {
                // In a real app, you would show a file picker to select a model file
                // For now, just simulate importing a model

                // Update imported model count
                _importedModelCount.value = federatedLearningManager.getImportedModelCount()

                // Show success message
                _errorMessage.value = "Model imported successfully"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to import model: ${e.message}"
            }
        }
    }

    /**
     * Aggregate imported models
     */
    fun aggregateModels() {
        viewModelScope.launch {
            try {
                val habitId = _currentHabitId.value ?: return@launch

                // Get habit category
                val habit = habitRepository.getHabitById(habitId).first() ?: return@launch

                // Aggregate models
                val success = federatedLearningManager.aggregateModels(habit.category)

                if (success) {
                    // Update imported model count
                    _importedModelCount.value = federatedLearningManager.getImportedModelCount()

                    // Show success message
                    _errorMessage.value = "Models aggregated successfully"
                } else {
                    _errorMessage.value = "No models to aggregate"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to aggregate models: ${e.message}"
            }
        }
    }

    /**
     * Compress the neural network model
     */
    fun compressModel() {
        viewModelScope.launch {
            try {
                _errorMessage.value = null

                // Create a dummy model buffer for demonstration
                val modelBuffer = ByteBuffer.allocateDirect(1024 * 100) // 100KB

                // Compress the model
                val compressedBuffer = modelCompressor.quantizeModel(modelBuffer)

                // Calculate compression statistics
                val originalSize = modelBuffer.capacity()
                val compressedSize = compressedBuffer.capacity()
                val stats = modelCompressor.getCompressionStats(originalSize, compressedSize)

                // Update state
                _compressionStats.value = stats

                // Show success message
                _errorMessage.value = "Model compressed successfully: ${stats.percentSaved.toInt()}% space saved"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to compress model: ${e.message}"
            }
        }
    }

    /**
     * Optimize hyperparameters
     */
    fun optimizeHyperparameters() {
        viewModelScope.launch {
            try {
                _errorMessage.value = null

                // Clear previous results
                _trialResults.value = emptyList()
                _bestHyperparameters.value = null

                // Define evaluation function
                val evaluateConfig: suspend (Hyperparameters) -> Float = { config ->
                    // Simulate training with these hyperparameters
                    // In a real app, this would actually train a model

                    // Add some randomness to the evaluation
                    val baseScore = 0.5f
                    val learningRateFactor = if (config.learningRate > 0.01f) 0.1f else -0.1f
                    val hiddenLayerFactor = if (config.hiddenLayerSizes.sum() > 16) 0.1f else -0.1f
                    val batchSizeFactor = if (config.batchSize > 16) 0.1f else -0.1f
                    val dropoutFactor = if (config.dropoutRate > 0.1f) 0.1f else -0.1f

                    val score = baseScore + learningRateFactor + hiddenLayerFactor + batchSizeFactor + dropoutFactor
                    val randomness = (Math.random() * 0.2 - 0.1).toFloat()

                    // Add trial result to state
                    val result = TrialResult(
                        trial = _trialResults.value.size + 1,
                        hyperparameters = config,
                        score = score + randomness
                    )

                    _trialResults.value = _trialResults.value + result

                    // Simulate delay
                    kotlinx.coroutines.delay(500)

                    return@evaluateConfig score + randomness
                }

                // Run optimization
                val bestConfig = hyperparameterOptimizer.optimizeHyperparameters(evaluateConfig)

                // Update state
                _bestHyperparameters.value = bestConfig
                _hyperparameterImportance.value = hyperparameterOptimizer.getHyperparameterImportance()

                // Show success message
                _errorMessage.value = "Hyperparameter optimization completed"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to optimize hyperparameters: ${e.message}"
            }
        }
    }

    /**
     * Detect anomalies in habit completions
     */
    fun detectAnomalies() {
        viewModelScope.launch {
            try {
                _errorMessage.value = null

                val habitId = _currentHabitId.value ?: return@launch

                // Get habit and completions
                val habit = habitRepository.getHabitById(habitId).first() ?: return@launch
                val completions = habitRepository.getHabitCompletions(habitId).first()

                if (completions.isEmpty()) {
                    _errorMessage.value = "Not enough completions to detect anomalies"
                    return@launch
                }

                // Detect anomalies
                val anomalies = anomalyDetector.detectAnomalies(habit, completions)

                // Update state
                _anomalies.value = anomalies

                // Show success message
                if (anomalies.isEmpty()) {
                    _errorMessage.value = "No anomalies detected"
                } else {
                    _errorMessage.value = "Detected ${anomalies.size} anomalies"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to detect anomalies: ${e.message}"
            }
        }
    }

    /**
     * Explain an anomaly
     */
    fun explainAnomaly(anomaly: HabitAnomaly) {
        val explanation = anomalyDetector.getAnomalyExplanation(anomaly)
        _errorMessage.value = explanation
    }

    /**
     * Capture image for multi-modal learning
     */
    fun captureImage() {
        // In a real app, this would launch the camera
        // For this demo, we'll just simulate capturing an image

        _hasImageFeatures.value = true
        _errorMessage.value = "Image captured successfully"
    }

    /**
     * Add notes for multi-modal learning
     */
    fun addNotes() {
        // In a real app, this would show a text input dialog
        // For this demo, we'll just simulate adding notes

        _hasTextFeatures.value = true
        _errorMessage.value = "Notes added successfully"
    }

    /**
     * Process multi-modal features
     */
    fun processMultiModalFeatures() {
        viewModelScope.launch {
            try {
                _errorMessage.value = null

                // In a real app, this would process actual features
                // For this demo, we'll just simulate processing

                // Simulate sensor data
                _hasSensorFeatures.value = true

                // Show success message
                _errorMessage.value = "Multi-modal features processed successfully"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to process features: ${e.message}"
            }
        }
    }

    /**
     * Start meta-learning across all habits
     */
    fun startMetaLearning() {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                _metaLearningProgress.value = 0f

                // Get all habits and their completions
                val habits = habitRepository.getAllHabits().first()

                if (habits.size < 5) {
                    _errorMessage.value = "Not enough habits for meta-learning (need at least 5)"
                    return@launch
                }

                val completionsMap = mutableMapOf<String, List<HabitCompletion>>()

                for (habit in habits) {
                    val completions = habitRepository.getHabitCompletions(habit.id).first()
                    completionsMap[habit.id] = completions
                }

                // Start meta-learning
                val success = metaLearning.metaLearn(habits, completionsMap)

                if (success) {
                    _errorMessage.value = "Meta-learning completed successfully"
                } else {
                    _errorMessage.value = "Meta-learning failed"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to start meta-learning: ${e.message}"
                _metaLearningProgress.value = 0f
            }
        }
    }

    /**
     * Adapt meta-model to current habit
     */
    fun adaptToHabit() {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                _adaptationProgress.value = 0f

                val habitId = _currentHabitId.value ?: return@launch

                // Get habit and completions
                val habit = habitRepository.getHabitById(habitId).first() ?: return@launch
                val completions = habitRepository.getHabitCompletions(habitId).first()

                if (completions.isEmpty()) {
                    _errorMessage.value = "Not enough completions for adaptation"
                    return@launch
                }

                // Adapt to habit
                val (adaptedInputToHidden, adaptedHiddenToOutput) = metaLearning.adaptToHabit(habit, completions)

                // In a real app, we would use these adapted parameters

                _errorMessage.value = "Adaptation to habit completed successfully"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to adapt to habit: ${e.message}"
                _adaptationProgress.value = 0f
            }
        }
    }
}

/**
 * Enum for network status
 */
enum class NetworkStatus {
    Idle,
    Propagating,
    Training,
    Error
}
