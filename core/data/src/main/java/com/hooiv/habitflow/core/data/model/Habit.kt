package com.hooiv.habitflow.core.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.hooiv.habitflow.core.data.database.Converters
import java.util.UUID

/**
 * Habit entity — the core domain object of HabitFlow.
 *
 * Design decisions:
 *  - All fields are `val` (immutable). Every mutation goes through `copy()` so Room
 *    can observe the change via Flow and Compose receives a new object (no silent mutation).
 *  - Timestamp fields use `Long` (epoch millis) directly — avoids the Date ↔ Long
 *    TypeConverter round-trip on every read/write and removes mutable `Date` allocations.
 *  - `completionHistory` stores epoch-millis longs using a fast '|'-delimited converter
 *    (see [Converters.fromLongList]).  The [HabitCompletion] table is the canonical source
 *    of completion records with full metadata; this field is a compact cache for quick
 *    streak and progress calculations without a JOIN.
 *  - `unlockedBadges` is `List<Int>` in the entity (Room-friendly) but callers that need
 *    O(1) membership tests should convert to `Set<Int>` locally.
 *  - Soft-delete via `isDeleted` lets the sync layer push tombstones to Firebase before
 *    the local row is purged.
 *
 *  @Immutable tells the Compose compiler this class is stable so it will skip
 *  recomposition of subtrees that receive an identical instance.
 */
@Immutable
@Entity(
    tableName = "habits",
    indices = [
        Index(value = ["isDeleted"]),
        Index(value = ["lastCompletedDate"]),
        Index(value = ["category"])
    ]
)
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
    /** Epoch millis of the last successful goal completion; null if never completed. */
    val lastCompletedDate: Long? = null,
    /** Epoch millis when this habit was created. */
    val createdDate: Long = System.currentTimeMillis(),
    /** Compact cache of completion timestamps (epoch millis) for streak/progress maths. */
    val completionHistory: List<Long> = emptyList(),
    val isEnabled: Boolean = true,
    val reminderTime: String? = null,
    /** Streak-milestone IDs (7, 30, 100 …) for which the user has earned a badge. */
    val unlockedBadges: List<Int> = emptyList(),
    val category: String? = null,
    /** Epoch millis of the last field modification — used for sync conflict resolution. */
    val lastUpdatedTimestamp: Long = System.currentTimeMillis(),

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

@Immutable
data class HabitNote(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    /** Epoch millis. */
    val timestamp: Long = System.currentTimeMillis(),
    val mood: Int? = null
)
