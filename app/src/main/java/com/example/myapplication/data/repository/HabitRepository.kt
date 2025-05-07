package com.example.myapplication.data.repository

import com.example.myapplication.data.database.HabitDao
import com.example.myapplication.data.model.Habit
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Mark as a singleton so Hilt provides the same instance
class HabitRepository @Inject constructor(private val habitDao: HabitDao) {

    fun getAllHabits(): Flow<List<Habit>> {
        return habitDao.getAllHabits()
    }

    fun getHabitById(habitId: String): Flow<Habit?> {
        return habitDao.getHabitById(habitId)
    }

    suspend fun insertHabit(habit: Habit) {
        habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
    }

    // Example: Exposing the getHabitsByFrequency from DAO through repository
    fun getHabitsByFrequency(frequency: String): Flow<List<Habit>> {
        return habitDao.getHabitsByFrequency(frequency)
    }
}
