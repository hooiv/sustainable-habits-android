package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.myapplication.core.data.database.Converters
import java.util.Date
import java.util.UUID

/**
 * Represents a single completion of a habit, with timestamp and optional notes or metadata.
 */
@Entity(
    tableName = "habit_completions",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habitId"]),
        Index(value = ["completionDate"])
    ]
)
@TypeConverters(Converters::class)
data class HabitCompletion(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val habitId: String, // Foreign key to Habit
    val completionDate: Long, // Timestamp for this completion
    val note: String? = null, // Optional note for this completion
    val mood: Int? = null, // Optional mood rating (1-5)
    val location: String? = null, // Optional location info
    val photoUri: String? = null // Optional photo evidence
)
