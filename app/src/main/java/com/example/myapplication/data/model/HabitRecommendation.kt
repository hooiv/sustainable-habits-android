package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

/**
 * Represents a personalized recommendation for a habit
 */
@Entity(
    tableName = "habit_recommendations",
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
data class HabitRecommendation(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val habitId: String,
    val type: Int, // 0 = timing, 1 = streak, 2 = modification, 3 = motivation, 4 = context
    val title: String,
    val description: String,
    val confidence: Float, // 0-1 scale
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isFollowed: Boolean? = null, // null if not yet known
    val userRating: Int? = null // 1-5 rating from user
)
