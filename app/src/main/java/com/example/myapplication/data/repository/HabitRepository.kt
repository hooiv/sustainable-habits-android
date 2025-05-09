package com.example.myapplication.data.repository

import com.example.myapplication.data.database.HabitDao
import com.example.myapplication.data.model.Habit
import com.example.myapplication.data.model.HabitFrequency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar
import java.util.Date
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

    suspend fun insertOrReplaceHabits(habits: List<Habit>) {
        habitDao.insertOrReplaceHabits(habits)
    }

    // Example: Exposing the getHabitsByFrequency from DAO through repository
    fun getHabitsByFrequency(frequency: String): Flow<List<Habit>> {
        return habitDao.getHabitsByFrequency(frequency)
    }

    suspend fun markHabitCompleted(habitId: String, completionDate: Date = Date()) {
        val habit = habitDao.getHabitById(habitId).firstOrNull() ?: return
        
        // Check if habit is enabled
        if (!habit.isEnabled) {
            return
        }
        
        // First, check if we've already completed this habit in the current period
        if (isSamePeriod(habit.lastCompletedDate, completionDate, habit.frequency) && 
            habit.lastCompletedDate != null) {
            // If already completed in same period, just increment progress
            val updatedHabit = habit.copy(
                goalProgress = habit.goalProgress + 1,
                lastCompletedDate = completionDate,
                completionHistory = (habit.completionHistory + completionDate).toMutableList()
            )
            
            // Calculate streak only when goal is reached
            if (updatedHabit.goalProgress >= updatedHabit.goal) {
                val newStreak = habit.streak + 1
                
                // Badge milestones
                val badgeMilestones = listOf(7, 30, 100)
                val newUnlockedBadges = habit.unlockedBadges.toMutableList()
                for (milestone in badgeMilestones) {
                    if (newStreak >= milestone && !newUnlockedBadges.contains(milestone)) {
                        newUnlockedBadges.add(milestone)
                    }
                }
                
                habitDao.updateHabit(
                    updatedHabit.copy(
                        streak = newStreak,
                        goalProgress = 0, // Reset progress after reaching goal
                        unlockedBadges = newUnlockedBadges
                    )
                )
            } else {
                habitDao.updateHabit(updatedHabit)
            }
        } else {
            // Different period or first completion
            val updatedHabit = habit.copy(
                goalProgress = 1, // Start with 1, not incrementing
                lastCompletedDate = completionDate,
                completionHistory = (habit.completionHistory + completionDate).toMutableList()
            )
            
            // Check if goal is reached in one go
            if (updatedHabit.goalProgress >= updatedHabit.goal) {
                val newStreak = calculateNewStreak(habit, completionDate)
                
                // Badge milestones
                val badgeMilestones = listOf(7, 30, 100)
                val newUnlockedBadges = habit.unlockedBadges.toMutableList()
                for (milestone in badgeMilestones) {
                    if (newStreak >= milestone && !newUnlockedBadges.contains(milestone)) {
                        newUnlockedBadges.add(milestone)
                    }
                }
                
                habitDao.updateHabit(
                    updatedHabit.copy(
                        streak = newStreak,
                        goalProgress = 0, // Reset progress after reaching goal
                        unlockedBadges = newUnlockedBadges
                    )
                )
            } else {
                habitDao.updateHabit(updatedHabit)
            }
        }
    }

    // Helper function to calculate new streak based on correct consecutive check
    private fun calculateNewStreak(habit: Habit, completionDate: Date): Int {
        // If this is the first completion ever, start with streak 1
        if (habit.lastCompletedDate == null) {
            return 1
        }
        
        return when (habit.frequency) {
            HabitFrequency.DAILY -> {
                if (isConsecutiveDay(habit.lastCompletedDate, completionDate)) {
                    habit.streak + 1
                } else if (isSameDay(habit.lastCompletedDate, completionDate)) {
                    habit.streak // Don't change streak for same day completions
                } else {
                    1 // Reset streak if days not consecutive
                }
            }
            HabitFrequency.WEEKLY -> {
                if (isConsecutiveWeek(habit.lastCompletedDate, completionDate)) {
                    habit.streak + 1
                } else if (isSameWeek(habit.lastCompletedDate, completionDate)) {
                    habit.streak // Don't change streak for same week completions
                } else {
                    1 // Reset streak if weeks not consecutive
                }
            }
            HabitFrequency.MONTHLY -> {
                if (isConsecutiveMonth(habit.lastCompletedDate, completionDate)) {
                    habit.streak + 1
                } else if (isSameMonth(habit.lastCompletedDate, completionDate)) {
                    habit.streak // Don't change streak for same month completions
                } else {
                    1 // Reset streak if months not consecutive
                }
            }
        }
    }

    private fun calculateStreakAndProgress(habit: Habit, completionDate: Date): Pair<Int, Int> {
        var currentStreak = habit.streak
        var currentGoalProgress = habit.goalProgress

        if (habit.lastCompletedDate == null) {
            currentStreak = if (currentGoalProgress >= habit.goal) 1 else 0
        } else {
            // Simplify streak calculation logic by directly calling the helper methods
            if (currentGoalProgress >= habit.goal) {
                when (habit.frequency) {
                    HabitFrequency.DAILY -> {
                        if (isConsecutiveDay(habit.lastCompletedDate, completionDate)) {
                            currentStreak++
                        } else if (!isSameDay(habit.lastCompletedDate, completionDate)) {
                            currentStreak = 1
                        }
                    }
                    HabitFrequency.WEEKLY -> {
                        if (isConsecutiveWeek(habit.lastCompletedDate, completionDate)) {
                            currentStreak++
                        } else if (!isSameWeek(habit.lastCompletedDate, completionDate)) {
                            currentStreak = 1
                        }
                    }
                    HabitFrequency.MONTHLY -> {
                        if (isConsecutiveMonth(habit.lastCompletedDate, completionDate)) {
                            currentStreak++
                        } else if (!isSameMonth(habit.lastCompletedDate, completionDate)) {
                            currentStreak = 1
                        }
                    }
                }
                currentGoalProgress = 0
            } else {
                if (!isSamePeriod(habit.lastCompletedDate, completionDate, habit.frequency)) {
                    currentStreak = 0
                }
            }
        }

        if (currentGoalProgress >= habit.goal && habit.lastCompletedDate == null) {
            currentStreak = 1
        }

        return Pair(currentStreak, currentGoalProgress)
    }

    private fun isSameDay(date1: Date?, date2: Date): Boolean {
        if (date1 == null) return false
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isConsecutiveDay(previousCompletion: Date?, currentCompletion: Date): Boolean {
        if (previousCompletion == null) return false
        val prevCal = Calendar.getInstance().apply { time = previousCompletion }
        val currCal = Calendar.getInstance().apply { time = currentCompletion }
        prevCal.add(Calendar.DAY_OF_YEAR, 1)
        return prevCal.get(Calendar.YEAR) == currCal.get(Calendar.YEAR) &&
               prevCal.get(Calendar.DAY_OF_YEAR) == currCal.get(Calendar.DAY_OF_YEAR)
    }

    private fun isSameWeek(date1: Date?, date2: Date): Boolean {
        if (date1 == null) return false
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
    }

    private fun isConsecutiveWeek(previousCompletion: Date?, currentCompletion: Date): Boolean {
        if (previousCompletion == null) return false
        val prevCal = Calendar.getInstance().apply { time = previousCompletion }
        val currCal = Calendar.getInstance().apply { time = currentCompletion }
        prevCal.add(Calendar.WEEK_OF_YEAR, 1)
        return prevCal.get(Calendar.YEAR) == currCal.get(Calendar.YEAR) &&
               prevCal.get(Calendar.WEEK_OF_YEAR) == currCal.get(Calendar.WEEK_OF_YEAR)
    }

    private fun isSameMonth(date1: Date?, date2: Date): Boolean {
        if (date1 == null) return false
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
    }

    private fun isConsecutiveMonth(previousCompletion: Date?, currentCompletion: Date): Boolean {
        if (previousCompletion == null) return false
        val prevCal = Calendar.getInstance().apply { time = previousCompletion }
        val currCal = Calendar.getInstance().apply { time = currentCompletion }
        prevCal.add(Calendar.MONTH, 1)
        return prevCal.get(Calendar.YEAR) == currCal.get(Calendar.YEAR) &&
               prevCal.get(Calendar.MONTH) == currCal.get(Calendar.MONTH)
    }

    private fun isSamePeriod(date1: Date?, date2: Date, frequency: HabitFrequency): Boolean {
        if (date1 == null) return false
        return when (frequency) {
            HabitFrequency.DAILY -> isSameDay(date1, date2)
            HabitFrequency.WEEKLY -> isSameWeek(date1, date2)
            HabitFrequency.MONTHLY -> isSameMonth(date1, date2)
        }
    }

    suspend fun resetHabitProgressIfNeeded(habitId: String, currentDate: Date = Date()) {
        val habit = habitDao.getHabitById(habitId).firstOrNull() ?: return
        if (habit.lastCompletedDate == null) return

        if (!isSamePeriod(habit.lastCompletedDate, currentDate, habit.frequency)) {
            if (habit.goalProgress < habit.goal && habit.streak > 0) {
                habitDao.updateHabit(habit.copy(goalProgress = 0, streak = 0))
            } else {
                habitDao.updateHabit(habit.copy(goalProgress = 0))
            }
        }
    }

    // Fetch habits for the current day
    fun getTodayHabits(): Flow<List<Habit>> {
        return habitDao.getTodayHabits()
    }
}
