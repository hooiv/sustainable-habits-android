package com.example.myapplication.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.myapplication.core.data.database.Converters
import java.util.UUID
import java.util.Date

/**
 * Enhanced Habit entity with advanced features for tracking, gamification, and social interactions
 */
@Entity(tableName = "habits") // Annotate as an entity with table name
@TypeConverters(Converters::class) // Specify TypeConverters for this entity
data class Habit(
    @PrimaryKey // Mark id as the primary key
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val goal: Int = 1, // e.g., complete 1 time for daily, 3 times for weekly
    var goalProgress: Int = 0, // How many times completed in the current frequency period
    var streak: Int = 0,
    var lastCompletedDate: Date? = null,
    val createdDate: Date = Date(),
    var completionHistory: MutableList<Date> = mutableListOf(), // History of all completion dates
    var isEnabled: Boolean = true, // To allow pausing a habit
    var reminderTime: String? = null, // For notifications, e.g., "09:00"
    var unlockedBadges: List<Int> = emptyList(), // List of streak milestones for which badges are unlocked
    var category: String? = null, // Optional field for habit categorization
    var lastUpdatedTimestamp: Date = Date(), // Timestamp of the last modification

    // Metadata
    var difficulty: HabitDifficulty = HabitDifficulty.MEDIUM,
    var priority: HabitPriority = HabitPriority.MEDIUM,
    var color: String? = null, // Custom color for the habit (hex code)
    var icon: String? = null, // Icon identifier for the habit
    var notes: List<HabitNote> = emptyList(), // Keep notes as they are useful
    var tags: List<String> = emptyList(),

    // Sync Metadata
    var isSynced: Boolean = false, // True if local matches remote
    var lastSynced: Long = 0, // Timestamp of last successful sync
    var isDeleted: Boolean = false // Soft delete flag
)

/**
 * Enum for habit frequency
 */
enum class HabitFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM // For custom schedules
}

/**
 * Enum for habit difficulty levels
 */
enum class HabitDifficulty {
    VERY_EASY,
    EASY,
    MEDIUM,
    HARD,
    VERY_HARD
}

/**
 * Enum for habit priority levels
 */
enum class HabitPriority {
    VERY_LOW,
    LOW,
    MEDIUM,
    HIGH,
    VERY_HIGH
}



/**
 * Data class for habit notes
 */
data class HabitNote(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val timestamp: Date = Date(),
    val mood: Int? = null // Optional mood rating (1-5)
)


