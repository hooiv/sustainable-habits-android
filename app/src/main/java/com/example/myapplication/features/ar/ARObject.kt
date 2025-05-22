package com.example.myapplication.features.ar

import androidx.compose.ui.geometry.Offset

/**
 * Types of AR objects
 */
enum class ARObjectType {
    HABIT_TREE,
    STREAK_FLAME,
    ACHIEVEMENT_TROPHY,
    PROGRESS_CHART,
    HABIT_REMINDER,
    MOTIVATION_OBJECT,
    CUSTOM_OBJECT
}

/**
 * Represents an object in AR space
 */
data class ARObject(
    val type: ARObjectType,
    val position: Offset,
    val scale: Float = 1.0f,
    val rotation: Float = 0.0f,
    val label: String? = null,
    val relatedHabitId: String? = null
)
