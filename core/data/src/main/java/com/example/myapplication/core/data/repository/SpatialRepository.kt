package com.example.myapplication.core.data.repository

import com.example.myapplication.core.data.model.Habit
// import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * Represents a 3D object in spatial computing
 */
data class SpatialObject(
    val id: String = UUID.randomUUID().toString(),
    val type: SpatialObjectType,
    val position: Offset3D,
    val rotation: Rotation3D,
    val scale: Float,
    val color: Int, // ARGB color value
    val label: String,
    val relatedHabitId: String? = null
)

/**
 * Types of spatial objects
 */
enum class SpatialObjectType {
    HABIT_SPHERE,
    STREAK_TOWER,
    GOAL_PYRAMID,
    ACHIEVEMENT_STAR,
    CATEGORY_CUBE,
    REMINDER_CLOCK
}

/**
 * Represents a 3D position
 */
data class Offset3D(
    val x: Float,
    val y: Float,
    val z: Float
)

/**
 * Represents a 3D rotation in degrees
 */
data class Rotation3D(
    val x: Float,
    val y: Float,
    val z: Float
)

/**
 * Repository for spatial objects
 */
@Singleton
class SpatialRepository @Inject constructor(
    private val habitRepository: HabitRepository
) {
    // In-memory storage for spatial objects
    private val _spatialObjects = MutableStateFlow<List<SpatialObject>>(emptyList())
    val spatialObjects = _spatialObjects.asStateFlow()

    init {
        // Initialize with sample data
        initializeSampleData()
    }

    /**
     * Get all spatial objects
     */
    suspend fun getSpatialObjects(): List<SpatialObject> {
        return _spatialObjects.value
    }

    /**
     * Get spatial objects for a specific habit
     */
    suspend fun getSpatialObjectsForHabit(habitId: String): List<SpatialObject> {
        return _spatialObjects.value.filter { it.relatedHabitId == habitId }
    }

    /**
     * Add a spatial object
     */
    suspend fun addSpatialObject(spatialObject: SpatialObject) {
        _spatialObjects.value = _spatialObjects.value + spatialObject
    }

    /**
     * Update a spatial object
     */
    suspend fun updateSpatialObject(spatialObject: SpatialObject) {
        _spatialObjects.value = _spatialObjects.value.map {
            if (it.id == spatialObject.id) spatialObject else it
        }
    }

    /**
     * Delete a spatial object
     */
    suspend fun deleteSpatialObject(objectId: String) {
        _spatialObjects.value = _spatialObjects.value.filter { it.id != objectId }
    }

    /**
     * Initialize sample data
     */
    private fun initializeSampleData() {
        // Initialize with empty list for now
        _spatialObjects.value = emptyList()

        // In a real app, we would load data from a database or API
        // For this demo, we'll just create some sample data
        val sampleObjects = listOf(
            SpatialObject(
                type = SpatialObjectType.HABIT_SPHERE,
                position = Offset3D(0f, 0f, 0f),
                rotation = Rotation3D(0f, 0f, 0f),
                scale = 1.0f,
                color = 0xFF2196F3.toInt(), // Blue
                label = "Morning Meditation"
            ),
            SpatialObject(
                type = SpatialObjectType.STREAK_TOWER,
                position = Offset3D(100f, 50f, 0f),
                rotation = Rotation3D(0f, 0f, 0f),
                scale = 1.2f,
                color = 0xFFF44336.toInt(), // Red
                label = "Exercise"
            ),
            SpatialObject(
                type = SpatialObjectType.GOAL_PYRAMID,
                position = Offset3D(-100f, -50f, 0f),
                rotation = Rotation3D(0f, 0f, 0f),
                scale = 0.8f,
                color = 0xFF4CAF50.toInt(), // Green
                label = "Read Books"
            )
        )

        _spatialObjects.value = sampleObjects
    }

    /**
     * Create sample objects from habits
     */
    private fun createSampleObjectsFromHabits(habits: List<Habit>): List<SpatialObject> {
        return habits.mapIndexed { index, habit ->
            // Calculate position in a spiral pattern
            val angle = index * 30f * (PI / 180f)
            val radius = 100f + index * 20f
            val x = radius * cos(angle).toFloat()
            val y = radius * sin(angle).toFloat()

            // Determine object type based on habit properties
            val objectType = when {
                habit.streak > 10 -> SpatialObjectType.STREAK_TOWER
                habit.unlockedBadges.isNotEmpty() -> SpatialObjectType.ACHIEVEMENT_STAR
                habit.goal > 1 -> SpatialObjectType.GOAL_PYRAMID
                habit.category != null -> SpatialObjectType.CATEGORY_CUBE
                habit.reminderTime != null -> SpatialObjectType.REMINDER_CLOCK
                else -> SpatialObjectType.HABIT_SPHERE
            }

            // Determine color based on habit category
            val color = when (habit.category) {
                "Health" -> 0xFF4CAF50.toInt() // Green
                "Fitness" -> 0xFFF44336.toInt() // Red
                "Learning" -> 0xFF2196F3.toInt() // Blue
                "Productivity" -> 0xFFFF9800.toInt() // Orange
                "Mindfulness" -> 0xFF9C27B0.toInt() // Purple
                else -> 0xFF3F51B5.toInt() // Indigo
            }

            SpatialObject(
                id = UUID.randomUUID().toString(),
                type = objectType,
                position = Offset3D(x, y, 0f),
                rotation = Rotation3D(0f, 0f, 0f),
                scale = 0.5f + (habit.streak.coerceAtMost(10) / 10f) * 0.5f,
                color = color,
                label = habit.name,
                relatedHabitId = habit.id
            )
        }
    }
}
