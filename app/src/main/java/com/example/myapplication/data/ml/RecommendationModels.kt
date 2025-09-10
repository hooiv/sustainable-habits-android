package com.example.myapplication.data.ml

import com.example.myapplication.data.model.ActionType
import java.util.UUID

/**
 * Data class for habit recommendations
 */
data class HabitRecommendation(
    val id: String = UUID.randomUUID().toString(),
    val habitId: String,
    val recommendationType: RecommendationType,
    val message: String,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis(),
    var isFollowed: Boolean = false
)

/**
 * Enum for recommendation types
 */
enum class RecommendationType {
    OPTIMAL_TIME,
    STREAK_REMINDER,
    ENVIRONMENT_CHANGE,
    MOTIVATION_BOOST,
    HABIT_PAIRING,
    DIFFICULTY_ADJUSTMENT,
    SOCIAL_SUPPORT
}

/**
 * Data class for reinforcement learning actions
 */
data class ReinforcementAction(
    val id: String = UUID.randomUUID().toString(),
    val habitId: String,
    val actionType: ActionType,
    val parameters: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Data class for neural prediction features
 */
data class PredictionFeatures(
    val habitId: String,
    val timeFeatures: FloatArray,
    val contextFeatures: FloatArray,
    val habitFeatures: FloatArray,
    val userFeatures: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PredictionFeatures

        if (habitId != other.habitId) return false
        if (!timeFeatures.contentEquals(other.timeFeatures)) return false
        if (!contextFeatures.contentEquals(other.contextFeatures)) return false
        if (!habitFeatures.contentEquals(other.habitFeatures)) return false
        if (!userFeatures.contentEquals(other.userFeatures)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = habitId.hashCode()
        result = 31 * result + timeFeatures.contentHashCode()
        result = 31 * result + contextFeatures.contentHashCode()
        result = 31 * result + habitFeatures.contentHashCode()
        result = 31 * result + userFeatures.contentHashCode()
        return result
    }
}

/**
 * Data class for recommendation context
 */
data class RecommendationContext(
    val userId: String,
    val timestamp: Long,
    val location: String? = null,
    val weather: String? = null,
    val mood: Float? = null,
    val energy: Float? = null,
    val socialContext: String? = null
)

/**
 * Data class for recommendation feedback
 */
data class RecommendationFeedback(
    val recommendationId: String,
    val userId: String,
    val wasHelpful: Boolean,
    val wasFollowed: Boolean,
    val feedback: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
