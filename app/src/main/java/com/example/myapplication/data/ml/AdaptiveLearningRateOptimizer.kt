package com.example.myapplication.data.ml

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Implements adaptive learning rate optimization for neural networks
 * Uses on-device algorithms without requiring cloud services
 */
@Singleton
class AdaptiveLearningRateOptimizer @Inject constructor() {
    companion object {
        private const val TAG = "AdaptiveLR"
        
        // Default learning rate parameters
        private const val INITIAL_LEARNING_RATE = 0.01f
        private const val MIN_LEARNING_RATE = 0.0001f
        private const val MAX_LEARNING_RATE = 0.1f
        
        // Adam optimizer parameters
        private const val BETA1 = 0.9f
        private const val BETA2 = 0.999f
        private const val EPSILON = 1e-8f
    }
    
    // Current learning rate
    private val _learningRate = MutableStateFlow(INITIAL_LEARNING_RATE)
    val learningRate: StateFlow<Float> = _learningRate.asStateFlow()
    
    // Optimizer state
    private var iteration = 0
    private val momentumCache = mutableMapOf<String, Float>()
    private val velocityCache = mutableMapOf<String, Float>()
    
    // Loss history for learning rate scheduling
    private val lossHistory = mutableListOf<Float>()
    
    /**
     * Reset optimizer state
     */
    fun reset() {
        _learningRate.value = INITIAL_LEARNING_RATE
        iteration = 0
        momentumCache.clear()
        velocityCache.clear()
        lossHistory.clear()
        
        Log.d(TAG, "Reset optimizer state")
    }
    
    /**
     * Update learning rate based on loss
     */
    fun updateLearningRate(loss: Float) {
        lossHistory.add(loss)
        
        // Only update learning rate after collecting enough data
        if (lossHistory.size < 5) {
            return
        }
        
        // Calculate loss trend
        val recentLosses = lossHistory.takeLast(5)
        val isDecreasing = isLossDecreasing(recentLosses)
        val isPlateauing = isLossPlateau(recentLosses)
        
        // Adjust learning rate based on loss trend
        val currentLR = _learningRate.value
        
        val newLR = when {
            isPlateauing -> {
                // Reduce learning rate on plateau
                (currentLR * 0.5f).coerceAtLeast(MIN_LEARNING_RATE)
            }
            isDecreasing -> {
                // Slightly increase learning rate if loss is consistently decreasing
                (currentLR * 1.05f).coerceAtMost(MAX_LEARNING_RATE)
            }
            else -> {
                // Reduce learning rate if loss is increasing
                (currentLR * 0.7f).coerceAtLeast(MIN_LEARNING_RATE)
            }
        }
        
        if (newLR != currentLR) {
            _learningRate.value = newLR
            Log.d(TAG, "Updated learning rate: $currentLR -> $newLR (loss: $loss)")
        }
        
        // Keep history at a reasonable size
        if (lossHistory.size > 20) {
            lossHistory.removeAt(0)
        }
    }
    
    /**
     * Check if loss is consistently decreasing
     */
    private fun isLossDecreasing(losses: List<Float>): Boolean {
        if (losses.size < 3) return false
        
        for (i in 2 until losses.size) {
            if (losses[i] >= losses[i-2]) {
                return false
            }
        }
        
        return true
    }
    
    /**
     * Check if loss has plateaued
     */
    private fun isLossPlateau(losses: List<Float>): Boolean {
        if (losses.size < 3) return false
        
        val recentLosses = losses.takeLast(3)
        val mean = recentLosses.average()
        val threshold = mean * 0.01 // 1% threshold
        
        // Check if all recent losses are within threshold of mean
        return recentLosses.all { abs(it - mean) < threshold }
    }
    
    /**
     * Apply Adam optimization to a parameter
     */
    fun optimizeParameter(paramId: String, gradient: Float): Float {
        iteration++
        
        // Get current momentum and velocity
        val momentum = momentumCache.getOrDefault(paramId, 0f)
        val velocity = velocityCache.getOrDefault(paramId, 0f)
        
        // Update momentum and velocity (Adam algorithm)
        val newMomentum = BETA1 * momentum + (1 - BETA1) * gradient
        val newVelocity = BETA2 * velocity + (1 - BETA2) * gradient * gradient
        
        // Store updated values
        momentumCache[paramId] = newMomentum
        velocityCache[paramId] = newVelocity
        
        // Bias correction
        val momentumCorrected = newMomentum / (1 - BETA1.pow(iteration))
        val velocityCorrected = newVelocity / (1 - BETA2.pow(iteration))
        
        // Calculate parameter update
        val update = _learningRate.value * momentumCorrected / (sqrt(velocityCorrected) + EPSILON)
        
        return update
    }
    
    /**
     * Apply learning rate decay
     */
    fun applyDecay(epoch: Int, totalEpochs: Int) {
        // Implement learning rate decay schedule
        val progress = epoch.toFloat() / totalEpochs
        
        // Step decay
        if (epoch > 0 && epoch % 10 == 0) {
            _learningRate.value = (_learningRate.value * 0.8f).coerceAtLeast(MIN_LEARNING_RATE)
            Log.d(TAG, "Applied step decay at epoch $epoch, new learning rate: ${_learningRate.value}")
        }
        
        // Cosine annealing
        if (progress > 0.5f) {
            val cosineDecay = 0.5f * (1 + kotlin.math.cos(kotlin.math.PI * progress)).toFloat()
            val newLR = MIN_LEARNING_RATE + (INITIAL_LEARNING_RATE - MIN_LEARNING_RATE) * cosineDecay
            _learningRate.value = newLR
            Log.d(TAG, "Applied cosine annealing at epoch $epoch, new learning rate: ${_learningRate.value}")
        }
    }
    
    /**
     * Get learning rate for a specific layer
     */
    fun getLearningRateForLayer(layerIndex: Int, totalLayers: Int): Float {
        // Implement layer-wise learning rates
        // Deeper layers often need smaller learning rates
        val layerFactor = 1.0f - (layerIndex.toFloat() / totalLayers) * 0.5f
        return _learningRate.value * layerFactor
    }
    
    /**
     * Power function for floats
     */
    private fun Float.pow(exponent: Int): Float {
        var result = 1f
        var base = this
        var exp = exponent
        
        while (exp > 0) {
            if (exp and 1 == 1) {
                result *= base
            }
            base *= base
            exp = exp shr 1
        }
        
        return result
    }
}
