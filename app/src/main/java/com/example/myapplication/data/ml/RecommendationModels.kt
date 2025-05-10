package com.example.myapplication.data.ml

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
 * Enum for action types
 */
enum class ActionType {
    SEND_NOTIFICATION,
    ADJUST_DIFFICULTY,
    SUGGEST_PAIRING,
    PROVIDE_ENCOURAGEMENT,
    SUGGEST_ENVIRONMENT_CHANGE,
    SUGGEST_TIME_CHANGE,
    SUGGEST_SOCIAL_SUPPORT
}
