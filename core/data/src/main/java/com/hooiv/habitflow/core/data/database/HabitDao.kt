package com.hooiv.habitflow.core.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.hooiv.habitflow.core.data.model.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceHabits(habits: List<Habit>)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    /** Returns all non-deleted habits as a reactive Flow, ordered newest first. */
    @Query("SELECT * FROM habits WHERE isDeleted = 0 ORDER BY createdDate DESC")
    fun getAllHabits(): Flow<List<Habit>>

    /** Returns a reactive Flow for a single habit (null when not found). */
    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    fun getHabitById(habitId: String): Flow<Habit?>

    /**
     * Single-shot suspension query for use inside transactions and repository
     * logic that only needs one value — avoids the `flow.firstOrNull()` anti-pattern.
     */
    @Query("SELECT * FROM habits WHERE id = :habitId AND isDeleted = 0 LIMIT 1")
    suspend fun getHabitByIdOnce(habitId: String): Habit?

    /**
     * All active (non-deleted, enabled) habits — used by the home-screen widget
     * to display today's habits to track.
     */
    @Query("SELECT * FROM habits WHERE isDeleted = 0 AND isEnabled = 1 ORDER BY createdDate DESC")
    fun getActiveHabits(): Flow<List<Habit>>

    /** Soft-deletes a habit and marks it as unsynced for the next Firebase push. */
    @Query("""
        UPDATE habits
           SET isDeleted = 1,
               isSynced  = 0,
               lastUpdatedTimestamp = :timestampMs
         WHERE id = :habitId
    """)
    suspend fun softDeleteHabit(habitId: String, timestampMs: Long = System.currentTimeMillis())
}
