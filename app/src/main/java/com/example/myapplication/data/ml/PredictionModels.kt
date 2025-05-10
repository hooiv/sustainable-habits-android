package com.example.myapplication.data.ml

import java.util.UUID

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

/**
 * Data class for habit predictions
 */
data class HabitPrediction(
    val id: String = UUID.randomUUID().toString(),
    val habitId: String,
    val habitName: String,
    val predictionType: PredictionType,
    val probability: Float, // 0.0 to 1.0
    val timeframe: String,
    val confidenceInterval: Pair<Float, Float>,
    val factors: List<PredictionFactor> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Data class for prediction factors
 */
data class PredictionFactor(
    val name: String,
    val weight: Float, // -1.0 to 1.0
    val confidence: Float // 0.0 to 1.0
)

/**
 * Data class for habit prediction recommendations
 */
data class HabitPredictionRecommendation(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val category: String? = null,
    val confidence: Float, // 0.0 to 1.0
    val relevanceScore: Float, // 0.0 to 1.0
    val factors: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
