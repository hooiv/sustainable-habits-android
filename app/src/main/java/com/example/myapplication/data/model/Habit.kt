package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.myapplication.data.database.Converters // Import Converters
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

    // Advanced features
    var difficulty: HabitDifficulty = HabitDifficulty.MEDIUM, // Difficulty level of the habit
    var priority: HabitPriority = HabitPriority.MEDIUM, // Priority level of the habit
    var color: String? = null, // Custom color for the habit (hex code)
    var icon: String? = null, // Icon identifier for the habit
    var tags: List<String> = emptyList(), // Tags for categorizing and filtering habits
    var notes: List<HabitNote> = emptyList(), // Notes and reflections about the habit
    var experiencePoints: Int = 0, // XP earned from completing the habit
    var level: Int = 1, // Level of mastery for this habit
    var linkedHabits: List<String> = emptyList(), // IDs of related habits (for habit chaining)
    var challengeId: String? = null, // ID of a challenge this habit is part of
    var isPublic: Boolean = false, // Whether this habit is visible to friends
    var isShared: Boolean = false, // Whether this habit is shared with friends
    var sharedWithUserIds: List<String> = emptyList(), // IDs of users this habit is shared with
    var streakFreezeUsed: Int = 0, // Number of streak freezes used (to prevent streak loss)
    var longestStreak: Int = 0, // Record of the longest streak achieved
    var completionRate: Float = 0f, // Percentage of successful completions
    var habitChainPosition: Int = -1, // Position in a habit chain (-1 if not part of a chain)
    var customSchedule: List<Int> = emptyList(), // Custom schedule (e.g., days of week for CUSTOM frequency)
    var reminderMessage: String? = null, // Custom message for reminders
    var locationBasedReminder: LocationReminder? = null, // Location-based reminder
    var progressHistory: Map<String, Int> = emptyMap(), // History of progress by date (ISO date string -> progress)
    var moodRatings: Map<String, Int> = emptyMap(), // Mood ratings after completing habit (ISO date -> rating 1-5)
    var habitStrength: Float = 0f, // Calculated strength of the habit (0-1)
    var visualizationType: VisualizationType = VisualizationType.DEFAULT, // Preferred visualization type
    var customRewards: List<HabitReward> = emptyList(), // Custom rewards for achieving milestones
    var skipDates: List<Date> = emptyList() // Dates when the habit is skipped (e.g., vacation)
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
 * Enum for visualization types
 */
enum class VisualizationType {
    DEFAULT,
    CALENDAR_HEATMAP,
    STREAK_TIMELINE,
    PROGRESS_CHART,
    HABIT_TREE
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

/**
 * Data class for location-based reminders
 */
data class LocationReminder(
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val radiusInMeters: Int = 100,
    val enterOrExit: LocationTrigger = LocationTrigger.ENTER
)

/**
 * Enum for location trigger type
 */
enum class LocationTrigger {
    ENTER,
    EXIT,
    BOTH
}

/**
 * Data class for custom habit rewards
 */
data class HabitReward(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val triggerType: RewardTriggerType,
    val triggerValue: Int, // The value that triggers this reward
    val isUnlocked: Boolean = false,
    val unlockedDate: Date? = null,
    val iconName: String? = null
)

/**
 * Enum for reward trigger types
 */
enum class RewardTriggerType {
    STREAK_MILESTONE,
    COMPLETION_COUNT,
    LEVEL_REACHED,
    PERFECT_WEEK,
    PERFECT_MONTH
}
