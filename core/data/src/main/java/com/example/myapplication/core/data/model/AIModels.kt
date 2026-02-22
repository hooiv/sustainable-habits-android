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
