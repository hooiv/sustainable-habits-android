package com.example.myapplication.core.network.ml

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.myapplication.core.data.model.ModelVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages A/B testing for neural network architectures
 * Implements on-device testing without requiring cloud services
 */
@Singleton
class ABTestingManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "ABTesting"
        private const val PREFS_NAME = "ab_testing_prefs"
        private const val KEY_USER_GROUP = "user_group"
        private const val KEY_MODEL_VARIANT = "model_variant"
        
        // Test variants
        const val VARIANT_CONTROL = "control"
        const val VARIANT_SMALL_NETWORK = "small_network"
        const val VARIANT_LARGE_NETWORK = "large_network"
        const val VARIANT_DEEP_NETWORK = "deep_network"
        const val VARIANT_WIDE_NETWORK = "wide_network"
        
        // Network architectures
        val NETWORK_ARCHITECTURES = mapOf(
            VARIANT_CONTROL to NetworkArchitecture(
                inputSize = 10,
                hiddenLayers = listOf(8),
                outputSize = 3,
                learningRate = 0.01f
            ),
            VARIANT_SMALL_NETWORK to NetworkArchitecture(
                inputSize = 10,
                hiddenLayers = listOf(6),
                outputSize = 3,
                learningRate = 0.01f
            ),
            VARIANT_LARGE_NETWORK to NetworkArchitecture(
                inputSize = 10,
                hiddenLayers = listOf(12),
                outputSize = 3,
                learningRate = 0.01f
            ),
            VARIANT_DEEP_NETWORK to NetworkArchitecture(
                inputSize = 10,
                hiddenLayers = listOf(8, 8),
                outputSize = 3,
                learningRate = 0.01f
            ),
            VARIANT_WIDE_NETWORK to NetworkArchitecture(
                inputSize = 10,
                hiddenLayers = listOf(16),
                outputSize = 3,
                learningRate = 0.01f
            )
        )
    }
    
    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // Current test results
    private val _testResults = MutableStateFlow<Map<String, TestResult>>(emptyMap())
    val testResults: StateFlow<Map<String, TestResult>> = _testResults.asStateFlow()
    
    // Current user's variant
    private val _currentVariant = MutableStateFlow<String>(VARIANT_CONTROL)
    val currentVariant: StateFlow<String> = _currentVariant.asStateFlow()
    
    init {
        // Assign user to a test group if not already assigned
        val userGroup = preferences.getString(KEY_USER_GROUP, null)
        if (userGroup == null) {
            assignUserToGroup()
        } else {
            _currentVariant.value = preferences.getString(KEY_MODEL_VARIANT, VARIANT_CONTROL) ?: VARIANT_CONTROL
        }
        
        Log.d(TAG, "Initialized A/B testing with variant: ${_currentVariant.value}")
    }
    
    /**
     * Assign user to a test group
     */
    private fun assignUserToGroup() {
        // Randomly assign user to a group
        val random = Random()
        val groupId = UUID.randomUUID().toString()
        
        // Randomly select a variant
        val variants = NETWORK_ARCHITECTURES.keys.toList()
        val variant = variants[random.nextInt(variants.size)]
        
        // Save to preferences
        preferences.edit()
            .putString(KEY_USER_GROUP, groupId)
            .putString(KEY_MODEL_VARIANT, variant)
            .apply()
        
        _currentVariant.value = variant
        
        Log.d(TAG, "Assigned user to group: $groupId with variant: $variant")
    }
    
    /**
     * Get network architecture for current variant
     */
    fun getCurrentArchitecture(): NetworkArchitecture {
        return NETWORK_ARCHITECTURES[_currentVariant.value] ?: NETWORK_ARCHITECTURES[VARIANT_CONTROL]!!
    }
    
    /**
     * Record test result for current variant
     */
    suspend fun recordTestResult(
        accuracy: Float,
        loss: Float,
        trainingTime: Long,
        predictionAccuracy: Float
    ) = withContext(Dispatchers.IO) {
        try {
            val variant = _currentVariant.value
            
            // Create test result
            val result = TestResult(
                variant = variant,
                accuracy = accuracy,
                loss = loss,
                trainingTime = trainingTime,
                predictionAccuracy = predictionAccuracy,
                timestamp = System.currentTimeMillis()
            )
            
            // Update results
            val currentResults = _testResults.value.toMutableMap()
            currentResults[variant] = result
            _testResults.value = currentResults
            
            Log.d(TAG, "Recorded test result for variant $variant: $result")
            
            // Save result to local storage for later analysis
            saveTestResult(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error recording test result: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Save test result to local storage
     */
    private fun saveTestResult(result: TestResult) {
        try {
            // In a real implementation, this would save to a database
            // For now, we'll just save to preferences
            val resultJson = """
                {
                    "variant": "${result.variant}",
                    "accuracy": ${result.accuracy},
                    "loss": ${result.loss},
                    "trainingTime": ${result.trainingTime},
                    "predictionAccuracy": ${result.predictionAccuracy},
                    "timestamp": ${result.timestamp}
                }
            """.trimIndent()
            
            val resultsKey = "test_results_${result.variant}"
            val existingResults = preferences.getString(resultsKey, "[]")
            val newResults = existingResults?.replace("]", ", $resultJson]") ?: "[$resultJson]"
            
            preferences.edit()
                .putString(resultsKey, newResults)
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving test result: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Get best variant based on current results
     */
    fun getBestVariant(): String {
        val results = _testResults.value
        
        if (results.isEmpty()) {
            return VARIANT_CONTROL
        }
        
        // Find variant with highest prediction accuracy
        return results.maxByOrNull { it.value.predictionAccuracy }?.key ?: VARIANT_CONTROL
    }
    
    /**
     * Switch to a different variant
     */
    fun switchVariant(variant: String) {
        if (NETWORK_ARCHITECTURES.containsKey(variant)) {
            preferences.edit()
                .putString(KEY_MODEL_VARIANT, variant)
                .apply()
            
            _currentVariant.value = variant
            
            Log.d(TAG, "Switched to variant: $variant")
        } else {
            Log.e(TAG, "Invalid variant: $variant")
        }
    }
    
    /**
     * Create model version from current variant
     */
    fun createModelVersion(
        habitId: String? = null,
        category: String? = null,
        accuracy: Float? = null,
        loss: Float? = null,
        qTableSize: Int? = null
    ): ModelVersion {
        val variant = _currentVariant.value
        val architecture = getCurrentArchitecture()
        
        return ModelVersion(
            id = UUID.randomUUID().toString(),
            habitId = habitId,
            category = category,
            version = 1, // In a real app, this would be incremented
            timestamp = System.currentTimeMillis(),
            accuracy = accuracy,
            loss = loss,
            qTableSize = qTableSize,
            description = "Architecture: $variant (input=${architecture.inputSize}, " +
                    "hidden=${architecture.hiddenLayers}, output=${architecture.outputSize}, " +
                    "lr=${architecture.learningRate})",
            filePath = null // In a real app, this would point to the model file
        )
    }
}

/**
 * Represents a neural network architecture
 */
data class NetworkArchitecture(
    val inputSize: Int,
    val hiddenLayers: List<Int>,
    val outputSize: Int,
    val learningRate: Float
)

/**
 * Represents a test result
 */
data class TestResult(
    val variant: String,
    val accuracy: Float,
    val loss: Float,
    val trainingTime: Long, // in milliseconds
    val predictionAccuracy: Float,
    val timestamp: Long
)
