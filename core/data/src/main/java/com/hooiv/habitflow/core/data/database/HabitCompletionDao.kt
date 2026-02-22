package com.hooiv.habitflow.core.data.database

import androidx.room.*
import com.hooiv.habitflow.core.data.model.HabitCompletion
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the habit_completions table.
 */
@Dao
interface HabitCompletionDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletion)

    @Update
    suspend fun updateCompletion(completion: HabitCompletion)
    
    @Delete
    suspend fun deleteCompletion(completion: HabitCompletion)
    
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY completionDate DESC")
    fun getCompletionsForHabit(habitId: String): Flow<List<HabitCompletion>>
    
    @Query("SELECT * FROM habit_completions ORDER BY completionDate DESC")
    fun getAllCompletions(): Flow<List<HabitCompletion>>
}
