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

// Using ActionType from com.example.myapplication.data.model.ActionType
