package com.example.myapplication.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.model.Habit
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

    @Query("SELECT * FROM habits WHERE id = :habitId")
    fun getHabitById(habitId: String): Flow<Habit?>

    @Query("SELECT * FROM habits ORDER BY createdDate DESC")
    fun getAllHabits(): Flow<List<Habit>>

    // Example: Query to get habits by frequency
    @Query("SELECT * FROM habits WHERE frequency = :frequency ORDER BY createdDate DESC")
    fun getHabitsByFrequency(frequency: String): Flow<List<Habit>>

    // Query to get today's habits
    @Query("SELECT * FROM habits WHERE date(createdDate) = date('now')")
    fun getTodayHabits(): Flow<List<Habit>>

    // Mark a habit as complete
    @Query("UPDATE habits SET goalProgress = goalProgress + 1 WHERE id = :habitId")
    suspend fun markHabitComplete(habitId: String)

    // You can add more specific queries as needed, e.g., for searching by name, etc.
}
