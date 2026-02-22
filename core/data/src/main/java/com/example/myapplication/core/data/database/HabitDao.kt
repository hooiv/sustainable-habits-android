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
    suspend fun insertOrReplaceHabits(habits: List<Habit>)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("SELECT * FROM habits WHERE date(createdDate) = date('now') AND isDeleted = 0")
    fun getTodayHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :habitId")
    fun getHabitById(habitId: String): Flow<Habit?>

    @Query("UPDATE habits SET isDeleted = 1, isSynced = 0, lastUpdatedTimestamp = :timestamp WHERE id = :habitId")
    suspend fun softDeleteHabit(habitId: String, timestamp: java.util.Date = java.util.Date())

    @Query("SELECT * FROM habits WHERE isDeleted = 0 ORDER BY createdDate DESC")
    fun getAllHabits(): Flow<List<Habit>>
}
