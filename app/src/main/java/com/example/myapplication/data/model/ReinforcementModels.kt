package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

/**
 * Represents a state in the reinforcement learning environment
 */
data class ReinforcementState(
    val habitId: String,
    val timeBucket: Int, // 0-7 (3-hour blocks)
    val dayBucket: Int, // 0-6 (days of week)
    val streakBucket: Int, // 0-4 (streak levels)
    val contextBucket: Int // 0-2 (context levels)
) {
    override fun toString(): String {
        return "State(time=$timeBucket, day=$dayBucket, streak=$streakBucket, context=$contextBucket)"
    }
}

/**
 * Represents an action in the reinforcement learning environment
 */
data class ReinforcementAction(
    val actionId: Int // 0-4 (different timing recommendations)
) {
    override fun toString(): String {
        return "Action($actionId)"
    }
}

/**
 * Represents a reinforcement learning episode
 */
@Entity(
    tableName = "reinforcement_episodes",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habitId")]
)
data class ReinforcementEpisode(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val habitId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val stateTimeBucket: Int,
    val stateDayBucket: Int,
    val stateStreakBucket: Int,
    val stateContextBucket: Int,
    val actionId: Int,
    val reward: Float,
    val nextStateTimeBucket: Int,
    val nextStateDayBucket: Int,
    val nextStateStreakBucket: Int,
    val nextStateContextBucket: Int
)

/**
 * Represents a Q-value in the reinforcement learning Q-table
 */
@Entity(
    tableName = "q_values",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habitId")]
)
data class QValue(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val habitId: String,
    val stateTimeBucket: Int,
    val stateDayBucket: Int,
    val stateStreakBucket: Int,
    val stateContextBucket: Int,
    val actionId: Int,
    val value: Float,
    val updateTimestamp: Long = System.currentTimeMillis()
)

/**
 * Represents a recommendation from the reinforcement learning agent
 */
@Entity(
    tableName = "reinforcement_recommendations",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habitId")]
)
data class ReinforcementRecommendation(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val habitId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionId: Int,
    val confidence: Float, // 0-1 scale
    val followed: Boolean? = null, // null if not yet known
    val feedback: Float? = null // User feedback on recommendation
)

/**
 * Represents a context snapshot at the time of a recommendation
 */
@Entity(
    tableName = "context_snapshots",
    foreignKeys = [
        ForeignKey(
            entity = ReinforcementRecommendation::class,
            parentColumns = ["id"],
            childColumns = ["recommendationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recommendationId")]
)
data class ContextSnapshot(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val recommendationId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val timeOfDay: Float,
    val dayOfWeek: Float,
    val activityLevel: Float,
    val lightLevel: Float,
    val temperature: Float,
    val weatherCondition: Float,
    val locationHome: Float,
    val locationWork: Float,
    val batteryLevel: Float,
    val deviceUsage: Float
)

/**
 * Represents a model version for tracking improvements
 */
@Entity(tableName = "model_versions")
data class ModelVersion(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val habitId: String? = null, // null for general models
    val category: String? = null, // null for habit-specific models
    val version: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val accuracy: Float? = null,
    val loss: Float? = null,
    val qTableSize: Int? = null,
    val description: String? = null,
    val filePath: String? = null
)
