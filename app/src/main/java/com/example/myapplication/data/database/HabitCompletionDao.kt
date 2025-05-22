package com.example.myapplication.data.database

import androidx.room.*
import com.example.myapplication.data.model.HabitCompletion
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the habit_completions table.
 */
@Dao
interface HabitCompletionDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletion)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletions(completions: List<HabitCompletion>)
    
    @Update
    suspend fun updateCompletion(completion: HabitCompletion)
    
    @Delete
    suspend fun deleteCompletion(completion: HabitCompletion)
    
    @Query("SELECT * FROM habit_completions WHERE id = :completionId")
    fun getCompletionById(completionId: String): Flow<HabitCompletion?>
    
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY completionDate DESC")
    fun getCompletionsForHabit(habitId: String): Flow<List<HabitCompletion>>
    
    @Query("SELECT * FROM habit_completions ORDER BY completionDate DESC")
    fun getAllCompletions(): Flow<List<HabitCompletion>>
    
    @Query("SELECT * FROM habit_completions WHERE completionDate BETWEEN :startDate AND :endDate ORDER BY completionDate DESC")
    fun getCompletionsInDateRange(startDate: Long, endDate: Long): Flow<List<HabitCompletion>>
    
    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId")
    fun getCompletionCountForHabit(habitId: String): Flow<Int>
    
    @Query("DELETE FROM habit_completions WHERE habitId = :habitId")
    suspend fun deleteCompletionsForHabit(habitId: String)
    
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND completionDate BETWEEN :startDate AND :endDate ORDER BY completionDate DESC")
    fun getCompletionsForHabitInDateRange(habitId: String, startDate: Long, endDate: Long): Flow<List<HabitCompletion>>
    
    @Query("SELECT * FROM habit_completions WHERE completionDate >= :date ORDER BY completionDate DESC")
    fun getCompletionsAfterDate(date: Long): Flow<List<HabitCompletion>>
    
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND completionDate >= :date ORDER BY completionDate DESC")
    fun getCompletionsForHabitAfterDate(habitId: String, date: Long): Flow<List<HabitCompletion>>
}
