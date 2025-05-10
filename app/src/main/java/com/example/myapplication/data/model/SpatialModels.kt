package com.example.myapplication.data.model

import java.util.UUID

/**
 * Data class for 3D position
 */
data class Offset3D(
    val x: Float,
    val y: Float,
    val z: Float
)

/**
 * Data class for 3D rotation
 */
data class Rotation3D(
    val x: Float,
    val y: Float,
    val z: Float
)

/**
 * Enum for spatial object types
 */
enum class SpatialObjectType {
    HABIT_SPHERE,
    STREAK_TOWER,
    GOAL_PYRAMID,
    CATEGORY_CUBE,
    ACHIEVEMENT_STAR,
    REMINDER_CLOCK
}

/**
 * Data class for spatial objects
 */
data class SpatialObject(
    val id: String = UUID.randomUUID().toString(),
    val type: SpatialObjectType,
    var position: Offset3D,
    var rotation: Rotation3D,
    var scale: Float = 1.0f,
    val color: androidx.compose.ui.graphics.Color,
    val label: String? = null,
    val relatedHabitId: String? = null
)
