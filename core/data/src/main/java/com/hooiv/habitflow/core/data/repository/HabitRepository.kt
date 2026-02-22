package com.hooiv.habitflow.core.data.repository

import androidx.room.withTransaction
import com.hooiv.habitflow.core.data.database.AppDatabase
import com.hooiv.habitflow.core.data.database.HabitCompletionDao
import com.hooiv.habitflow.core.data.database.HabitDao
import com.hooiv.habitflow.core.data.model.Habit
import com.hooiv.habitflow.core.data.model.HabitCompletion
import com.hooiv.habitflow.core.data.model.HabitFrequency
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val db: AppDatabase,
    private val habitDao: HabitDao,
    private val habitCompletionDao: HabitCompletionDao
) {

    // ── Read ──────────────────────────────────────────────────────────────────

    fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits()

    fun getHabitById(habitId: String): Flow<Habit?> = habitDao.getHabitById(habitId)

    fun getActiveHabits(): Flow<List<Habit>> = habitDao.getActiveHabits()

    // ── Write ─────────────────────────────────────────────────────────────────

    suspend fun insertHabit(habit: Habit) {
        habitDao.insertHabit(
            habit.copy(isSynced = false, lastUpdatedTimestamp = System.currentTimeMillis())
        )
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(
            habit.copy(isSynced = false, lastUpdatedTimestamp = System.currentTimeMillis())
        )
    }

    suspend fun deleteHabit(habit: Habit) {
        habitDao.softDeleteHabit(habit.id)
    }

    suspend fun insertOrReplaceHabits(habits: List<Habit>) {
        habitDao.insertOrReplaceHabits(habits)
    }

    // ── Completion ────────────────────────────────────────────────────────────

    /**
     * Records a habit completion and updates streak / progress atomically.
     *
     * Uses `withTransaction` so the [HabitCompletion] insert and the [Habit] update
     * are committed together — the UI never sees an inconsistent intermediate state.
     */
    suspend fun markHabitCompleted(
        habitId: String,
        completionDateMs: Long = System.currentTimeMillis(),
        note: String? = null,
        mood: Int? = null,
        location: String? = null,
        photoUri: String? = null
    ) {
        // Single-shot suspend query — avoids `flow.firstOrNull()` anti-pattern
        val habit = habitDao.getHabitByIdOnce(habitId) ?: return
        if (!habit.isEnabled) return

        val completion = HabitCompletion(
            habitId = habitId,
            completionDate = completionDateMs,
            note = note,
            mood = mood,
            location = location,
            photoUri = photoUri
        )

        db.withTransaction {
            habitCompletionDao.insertCompletion(completion)

            val updatedHistory = habit.completionHistory + completionDateMs

            val alreadyInSamePeriod =
                habit.lastCompletedDate != null &&
                isSamePeriod(habit.lastCompletedDate, completionDateMs, habit.frequency)

            val updatedHabit = if (alreadyInSamePeriod) {
                val newProgress = habit.goalProgress + 1
                if (newProgress >= habit.goal) {
                    habit.copy(
                        goalProgress = 0,
                        streak = habit.streak + 1,
                        lastCompletedDate = completionDateMs,
                        completionHistory = updatedHistory,
                        unlockedBadges = newMilestones(habit.unlockedBadges, habit.streak + 1)
                    )
                } else {
                    habit.copy(
                        goalProgress = newProgress,
                        lastCompletedDate = completionDateMs,
                        completionHistory = updatedHistory
                    )
                }
            } else {
                // New period
                if (habit.goal <= 1) {
                    val newStreak = calculateNewStreak(habit, completionDateMs)
                    habit.copy(
                        goalProgress = 0,
                        streak = newStreak,
                        lastCompletedDate = completionDateMs,
                        completionHistory = updatedHistory,
                        unlockedBadges = newMilestones(habit.unlockedBadges, newStreak)
                    )
                } else {
                    habit.copy(
                        goalProgress = 1,
                        lastCompletedDate = completionDateMs,
                        completionHistory = updatedHistory
                    )
                }
            }

            habitDao.updateHabit(
                updatedHabit.copy(isSynced = false, lastUpdatedTimestamp = System.currentTimeMillis())
            )
        }
    }

    // ── Completions ───────────────────────────────────────────────────────────

    fun getHabitCompletions(habitId: String): Flow<List<HabitCompletion>> =
        habitCompletionDao.getCompletionsForHabit(habitId)

    fun getAllCompletions(): Flow<List<HabitCompletion>> =
        habitCompletionDao.getAllCompletions()

    suspend fun deleteHabitCompletion(completion: HabitCompletion) =
        habitCompletionDao.deleteCompletion(completion)

    suspend fun updateHabitCompletion(completion: HabitCompletion) =
        habitCompletionDao.updateCompletion(completion)

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Returns a new `unlockedBadges` list that includes any newly reached milestones.
     * Uses [Set] for O(1) membership check rather than `List.contains()`.
     */
    private fun newMilestones(current: List<Int>, newStreak: Int): List<Int> {
        val unlocked = current.toHashSet()
        val milestones = intArrayOf(7, 30, 100, 365)
        var changed = false
        for (m in milestones) {
            if (newStreak >= m && unlocked.add(m)) changed = true
        }
        return if (changed) unlocked.sorted() else current
    }

    private fun calculateNewStreak(habit: Habit, completionMs: Long): Int {
        val prev = habit.lastCompletedDate ?: return 1
        return when (habit.frequency) {
            HabitFrequency.DAILY -> when {
                isConsecutiveDay(prev, completionMs) -> habit.streak + 1
                isSameDay(prev, completionMs)        -> habit.streak
                else                                 -> 1
            }
            HabitFrequency.WEEKLY -> when {
                isConsecutiveWeek(prev, completionMs) -> habit.streak + 1
                isSameWeek(prev, completionMs)        -> habit.streak
                else                                  -> 1
            }
            HabitFrequency.MONTHLY -> when {
                isConsecutiveMonth(prev, completionMs) -> habit.streak + 1
                isSameMonth(prev, completionMs)        -> habit.streak
                else                                   -> 1
            }
            HabitFrequency.CUSTOM -> habit.streak + 1
        }
    }

    private fun cal(ms: Long): Calendar = Calendar.getInstance().also { it.timeInMillis = ms }

    private fun isSameDay(a: Long, b: Long): Boolean {
        val ca = cal(a); val cb = cal(b)
        return ca[Calendar.YEAR] == cb[Calendar.YEAR] &&
               ca[Calendar.DAY_OF_YEAR] == cb[Calendar.DAY_OF_YEAR]
    }

    private fun isConsecutiveDay(prev: Long, curr: Long): Boolean {
        val cp = cal(prev).also { it.add(Calendar.DAY_OF_YEAR, 1) }
        val cc = cal(curr)
        return cp[Calendar.YEAR] == cc[Calendar.YEAR] &&
               cp[Calendar.DAY_OF_YEAR] == cc[Calendar.DAY_OF_YEAR]
    }

    private fun isSameWeek(a: Long, b: Long): Boolean {
        val ca = cal(a); val cb = cal(b)
        return ca[Calendar.YEAR] == cb[Calendar.YEAR] &&
               ca[Calendar.WEEK_OF_YEAR] == cb[Calendar.WEEK_OF_YEAR]
    }

    private fun isConsecutiveWeek(prev: Long, curr: Long): Boolean {
        val cp = cal(prev).also { it.add(Calendar.WEEK_OF_YEAR, 1) }
        val cc = cal(curr)
        return cp[Calendar.YEAR] == cc[Calendar.YEAR] &&
               cp[Calendar.WEEK_OF_YEAR] == cc[Calendar.WEEK_OF_YEAR]
    }

    private fun isSameMonth(a: Long, b: Long): Boolean {
        val ca = cal(a); val cb = cal(b)
        return ca[Calendar.YEAR] == cb[Calendar.YEAR] &&
               ca[Calendar.MONTH] == cb[Calendar.MONTH]
    }

    private fun isConsecutiveMonth(prev: Long, curr: Long): Boolean {
        val cp = cal(prev).also { it.add(Calendar.MONTH, 1) }
        val cc = cal(curr)
        return cp[Calendar.YEAR] == cc[Calendar.YEAR] &&
               cp[Calendar.MONTH] == cc[Calendar.MONTH]
    }

    private fun isSamePeriod(prev: Long, curr: Long, freq: HabitFrequency) = when (freq) {
        HabitFrequency.DAILY   -> isSameDay(prev, curr)
        HabitFrequency.WEEKLY  -> isSameWeek(prev, curr)
        HabitFrequency.MONTHLY -> isSameMonth(prev, curr)
        HabitFrequency.CUSTOM  -> false
    }
}
