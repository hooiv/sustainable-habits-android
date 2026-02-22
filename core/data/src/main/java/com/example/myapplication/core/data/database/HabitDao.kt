package com.example.myapplication.core.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.core.data.model.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceHabits(habits: List<Habit>) // New method for batch insert/replace

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    // Query to get today's habits
    @Query("SELECT * FROM habits WHERE date(createdDate) = date('now') AND isDeleted = 0")
    fun getTodayHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :habitId")
    fun getHabitById(habitId: String): Flow<Habit?>

    // --- Sync Methods ---

    @Query("SELECT * FROM habits WHERE isSynced = 0")
    suspend fun getUnsyncedHabits(): List<Habit>

    @Query("UPDATE habits SET isSynced = 1, lastSynced = :timestamp WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE habits SET isDeleted = 1, isSynced = 0, lastUpdatedTimestamp = :timestamp WHERE id = :habitId")
    suspend fun softDeleteHabit(habitId: String, timestamp: java.util.Date = java.util.Date())

    // Update getAllHabits to exclude soft-deleted items
    @Query("SELECT * FROM habits WHERE isDeleted = 0 ORDER BY createdDate DESC")
    fun getAllHabits(): Flow<List<Habit>>
}
