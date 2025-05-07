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

    // Example: Exposing the getHabitsByFrequency from DAO through repository
    fun getHabitsByFrequency(frequency: String): Flow<List<Habit>> {
        return habitDao.getHabitsByFrequency(frequency)
    }

    suspend fun markHabitCompleted(habitId: String, completionDate: Date = Date()) {
        val habit = habitDao.getHabitById(habitId).firstOrNull() ?: return

        val updatedHabit = habit.copy(
            goalProgress = habit.goalProgress + 1,
            lastCompletedDate = completionDate,
            completionHistory = habit.completionHistory + completionDate
        )

        val (newStreak, newGoalProgress) = calculateStreakAndProgress(updatedHabit, completionDate)

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
                goalProgress = newGoalProgress,
                unlockedBadges = newUnlockedBadges
            )
        )
    }

    private fun calculateStreakAndProgress(habit: Habit, completionDate: Date): Pair<Int, Int> {
        var currentStreak = habit.streak
        var currentGoalProgress = habit.goalProgress

        if (habit.lastCompletedDate == null) {
            currentStreak = if (currentGoalProgress >= habit.goal) 1 else 0
        } else {
            val calendar = Calendar.getInstance()
            calendar.time = habit.lastCompletedDate
            val lastCompletionDay = calendar.get(Calendar.DAY_OF_YEAR)
            val lastCompletionYear = calendar.get(Calendar.YEAR)

            calendar.time = completionDate
            val previousCompletionDay = calendar.get(Calendar.DAY_OF_YEAR)
            val previousCompletionYear = calendar.get(Calendar.YEAR)

            val expectedPreviousDayForStreak: Calendar = Calendar.getInstance().apply {
                time = completionDate
                add(Calendar.DAY_OF_YEAR, -1)
            }

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
}
