package com.example.myapplication.core.data.repository

import com.example.myapplication.core.data.database.HabitDao
import com.example.myapplication.core.data.database.HabitCompletionDao
import com.example.myapplication.core.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Mark as a singleton so Hilt provides the same instance
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val habitCompletionDao: HabitCompletionDao,
    private val workManager: androidx.work.WorkManager
) {

    fun getAllHabits(): Flow<List<Habit>> {
        return habitDao.getAllHabits()
    }

    fun getHabitById(habitId: String): Flow<Habit?> {
        return habitDao.getHabitById(habitId)
    }

    suspend fun insertHabit(habit: Habit) {
        habitDao.insertHabit(habit.copy(isSynced = false, lastUpdatedTimestamp = Date()))
        enqueueSync()
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit.copy(isSynced = false, lastUpdatedTimestamp = Date()))
        enqueueSync()
    }

    suspend fun deleteHabit(habit: Habit) {
        // Soft delete: Mark as deleted and unsynced
        habitDao.softDeleteHabit(habit.id)
        enqueueSync()
    }

    suspend fun insertOrReplaceHabits(habits: List<Habit>) {
        habitDao.insertOrReplaceHabits(habits)
    }

    // Example: Exposing the getHabitsByFrequency from DAO through repository
    fun getHabitsByFrequency(frequency: String): Flow<List<Habit>> {
        return habitDao.getHabitsByFrequency(frequency)
    }

    suspend fun markHabitCompleted(habitId: String, completionDate: Date = Date(), note: String? = null, mood: Int? = null, location: String? = null, photoUri: String? = null) {
        val habit = habitDao.getHabitById(habitId).firstOrNull() ?: return

        // Check if habit is enabled
        if (!habit.isEnabled) {
            return
        }

        // Create a habit completion record
        val completion = HabitCompletion(
            habitId = habitId,
            completionDate = completionDate.time,
            note = note,
            mood = mood,
            location = location,
            photoUri = photoUri
        )

        // Insert the completion record
        habitCompletionDao.insertCompletion(completion)

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
            HabitFrequency.CUSTOM -> {
                // For custom frequency, simplified streak logic since customSchedule was removed
                habit.streak + 1 
            }
        }
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
            HabitFrequency.CUSTOM -> {
                // For custom frequency, we need to check if both dates are in the same custom period
                // This is a simplified implementation
                true // Assuming custom periods are defined elsewhere
            }
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

    /**
     * Get completions for a specific habit
     */
    fun getHabitCompletions(habitId: String): Flow<List<HabitCompletion>> {
        return habitCompletionDao.getCompletionsForHabit(habitId)
    }

    /**
     * Get all completions
     */
    fun getAllCompletions(): Flow<List<HabitCompletion>> {
        return habitCompletionDao.getAllCompletions()
    }

    /**
     * Get completions in a date range
     */
    fun getCompletionsInDateRange(startDate: Long, endDate: Long): Flow<List<HabitCompletion>> {
        return habitCompletionDao.getCompletionsInDateRange(startDate, endDate)
    }

    /**
     * Get completions for a habit in a date range
     */
    fun getCompletionsForHabitInDateRange(habitId: String, startDate: Long, endDate: Long): Flow<List<HabitCompletion>> {
        return habitCompletionDao.getCompletionsForHabitInDateRange(habitId, startDate, endDate)
    }

    /**
     * Insert a habit completion
     */
    suspend fun insertHabitCompletion(completion: HabitCompletion) {
        habitCompletionDao.insertCompletion(completion)
    }

    /**
     * Delete a habit completion
     */
    suspend fun deleteHabitCompletion(completion: HabitCompletion) {
        habitCompletionDao.deleteCompletion(completion)
    }

    /**
     * Update a habit completion
     */
    suspend fun updateHabitCompletion(completion: HabitCompletion) {
        habitCompletionDao.updateCompletion(completion)
    }

    // --- Sync Logic ---

    /**
     * Triggers an immediate background sync.
     * Uses KEEP policy to avoid replacing existing syncs if running.
     */
    fun enqueueSync() {
        val syncRequest = androidx.work.OneTimeWorkRequestBuilder<com.example.myapplication.core.data.sync.HabitSyncWorker>()
            .setConstraints(
                androidx.work.Constraints.Builder()
                    .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                    .build()
            )
            .build()
        
        workManager.enqueueUniqueWork(
            "HabitSyncWork",
            androidx.work.ExistingWorkPolicy.KEEP,
            syncRequest
        )
    }

    /**
     * Forces a sync (REPLACE policy).
     */
    fun forceSync() {
         val syncRequest = androidx.work.OneTimeWorkRequestBuilder<com.example.myapplication.core.data.sync.HabitSyncWorker>()
            .setExpedited(androidx.work.OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(
                androidx.work.Constraints.Builder()
                    .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            "HabitSyncWork_Force",
            androidx.work.ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
}
