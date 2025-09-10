package com.example.myapplication.core.data.model

import java.util.Date
import java.util.UUID

/**
 * Data class representing an AI suggestion
 */
data class AISuggestion(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val type: SuggestionType,
    val confidence: Float, // 0.0 to 1.0
    val timestamp: Date = Date(),
    val relatedHabitId: String? = null,
    val actionable: Boolean = true
)

/**
 * Enum for different types of AI suggestions
 */
enum class SuggestionType {
    NEW_HABIT,
    HABIT_IMPROVEMENT,
    STREAK_PROTECTION,
    SCHEDULE_OPTIMIZATION,
    HABIT_CHAIN,
    MOTIVATION,
    INSIGHT
}

/**
 * Data class for analytics insights
 */
data class AnalyticsInsight(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val type: InsightType,
    val confidence: Float, // 0.0 to 1.0
    val timestamp: Date = Date(),
    val relatedHabitIds: List<String> = emptyList(),
    val actionable: Boolean = true
)

/**
 * Enum for different types of analytics insights
 */
enum class InsightType {
    PATTERN_DISCOVERY,
    CORRELATION_ANALYSIS,
    TREND_IDENTIFICATION,
    PERFORMANCE_METRICS,
    PREDICTION,
    RECOMMENDATION,
    ANOMALY_DETECTION
}
