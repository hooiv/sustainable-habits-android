package com.hooiv.habitflow.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.hooiv.habitflow.core.data.database.Converters
import java.util.Date
import java.util.UUID

/**
 * Habit entity.
 *
 * All fields are [val] (immutable). Mutations always go through [copy] so that
 * Room can detect the change and the UI receives a new object via Flow.
 *
 * Soft-delete ([isDeleted]) lets the sync layer push tombstones to Firebase
 * before the local row is purged.
 */
@Entity(tableName = "habits")
@TypeConverters(Converters::class)
data class Habit(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val goal: Int = 1,
    val goalProgress: Int = 0,
    val streak: Int = 0,
    val lastCompletedDate: Date? = null,
    val createdDate: Date = Date(),
    val completionHistory: List<Date> = emptyList(),
    val isEnabled: Boolean = true,
    val reminderTime: String? = null,
    val unlockedBadges: List<Int> = emptyList(),
    val category: String? = null,
    val lastUpdatedTimestamp: Date = Date(),

    // Metadata
    val difficulty: HabitDifficulty = HabitDifficulty.MEDIUM,
    val priority: HabitPriority = HabitPriority.MEDIUM,
    val color: String? = null,
    val icon: String? = null,
    val notes: List<HabitNote> = emptyList(),
    val tags: List<String> = emptyList(),

    // Sync metadata
    val isSynced: Boolean = false,
    val lastSynced: Long = 0L,
    val isDeleted: Boolean = false
)

enum class HabitFrequency { DAILY, WEEKLY, MONTHLY, CUSTOM }

enum class HabitDifficulty { VERY_EASY, EASY, MEDIUM, HARD, VERY_HARD }

enum class HabitPriority { VERY_LOW, LOW, MEDIUM, HIGH, VERY_HIGH }

data class HabitNote(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val timestamp: Date = Date(),
    val mood: Int? = null
)
