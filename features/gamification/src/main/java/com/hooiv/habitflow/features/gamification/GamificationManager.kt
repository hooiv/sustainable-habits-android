package com.hooiv.habitflow.features.gamification

import android.util.Log
import com.hooiv.habitflow.core.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages gamification features like rewards, badges, and experience points
 */
@Singleton
class GamificationManager @Inject constructor(
    private val habitRepository: HabitRepository
) {
    companion object {
        private const val TAG = "GamificationManager"
    }

    // Experience points
    private val _currentXp = MutableStateFlow(0)
    val currentXp: StateFlow<Int> = _currentXp.asStateFlow()

    // User level
    private val _currentLevel = MutableStateFlow(1)
    val currentLevel: StateFlow<Int> = _currentLevel.asStateFlow()

    // XP required for next level
    private val _xpForNextLevel = MutableStateFlow(100)
    val xpForNextLevel: StateFlow<Int> = _xpForNextLevel.asStateFlow()

    // All badges
    private val _allBadges = MutableStateFlow<List<Badge>>(emptyList())
    val allBadges: StateFlow<List<Badge>> = _allBadges.asStateFlow()

    // Rewards
    private val _availableRewards = MutableStateFlow<List<Reward>>(emptyList())
    val availableRewards: StateFlow<List<Reward>> = _availableRewards.asStateFlow()

    // Streak milestones for badges
    private val streakMilestones = listOf(7, 30, 100, 365)

    // Completion milestones for badges
    private val completionMilestones = listOf(10, 50, 100, 500)

    // Category completion milestones
    private val categoryMilestones = listOf(3, 5, 10)

    /**
     * Initialize gamification system
     */
    suspend fun initialize() {
        try {
            // Initialize badges
            initializeBadges()

            // Calculate current XP and level
            calculateXpAndLevel()

            // Initialize rewards
            initializeRewards()
        } catch (e: Exception) {
            if (android.util.Log.isLoggable(TAG, android.util.Log.ERROR)) Log.e(TAG, "Error initializing gamification system", e)
        }
    }

    /**
     * Initialize badges
     */
    private suspend fun initializeBadges() {
        val badges = buildList {
            // Streak badges
            streakMilestones.forEach { milestone ->
                add(Badge(
                    id = "streak_$milestone",
                    title = "$milestone Day Streak",
                    description = "Complete a habit for $milestone days in a row",
                    type = BadgeType.STREAK,
                    milestone = milestone
                ))
            }
            // Completion badges
            completionMilestones.forEach { milestone ->
                add(Badge(
                    id = "completion_$milestone",
                    title = "$milestone Completions",
                    description = "Complete habits $milestone times",
                    type = BadgeType.COMPLETION,
                    milestone = milestone
                ))
            }
            // Category badges
            listOf("health", "productivity", "mindfulness", "social").forEach { category ->
                categoryMilestones.forEach { milestone ->
                    add(Badge(
                        id = "${category}_$milestone",
                        title = "$category Master",
                        description = "Complete $milestone $category habits",
                        type = BadgeType.CATEGORY,
                        category = category,
                        milestone = milestone
                    ))
                }
            }
            // Special badges
            add(Badge(id = "early_bird",      title = "Early Bird",       description = "Complete 5 habits before 8 AM",  type = BadgeType.SPECIAL))
            add(Badge(id = "night_owl",       title = "Night Owl",        description = "Complete 5 habits after 10 PM",  type = BadgeType.SPECIAL))
            add(Badge(id = "weekend_warrior", title = "Weekend Warrior",  description = "Complete 10 habits on weekends", type = BadgeType.SPECIAL))
        }
        val resolvedBadges = updateBadgeUnlockStatus(badges)

        _allBadges.value = resolvedBadges
    }

    /**
     * Returns a new badge list with `isUnlocked` set based on user data.
     * Uses a single getAllCompletions() query — no N+1 per-habit queries.
     */
    private suspend fun updateBadgeUnlockStatus(badges: List<Badge>): List<Badge> {
        return try {
            val habits = habitRepository.getAllHabits().first()
            val allCompletions = habitRepository.getAllCompletions().first()

            val maxStreak = habits.maxOfOrNull { it.streak } ?: 0
            val totalCompletions = allCompletions.size
            val categoryCounts = habits.groupBy { it.category }.mapValues { it.value.size }

            val calendar = Calendar.getInstance()
            val earlyCompletions = allCompletions.count { c ->
                calendar.timeInMillis = c.completionDate; calendar[Calendar.HOUR_OF_DAY] < 8
            }
            val lateCompletions = allCompletions.count { c ->
                calendar.timeInMillis = c.completionDate; calendar[Calendar.HOUR_OF_DAY] >= 22
            }
            val weekendCompletions = allCompletions.count { c ->
                calendar.timeInMillis = c.completionDate
                val dow = calendar[Calendar.DAY_OF_WEEK]
                dow == Calendar.SATURDAY || dow == Calendar.SUNDAY
            }

            badges.map { badge ->
                val unlocked = when (badge.type) {
                    BadgeType.STREAK     -> maxStreak >= badge.milestone
                    BadgeType.COMPLETION -> totalCompletions >= badge.milestone
                    BadgeType.CATEGORY   -> (categoryCounts[badge.category] ?: 0) >= badge.milestone
                    BadgeType.SPECIAL    -> when (badge.id) {
                        "early_bird"      -> earlyCompletions >= 5
                        "night_owl"       -> lateCompletions >= 5
                        "weekend_warrior" -> weekendCompletions >= 10
                        else              -> badge.isUnlocked
                    }
                }
                if (unlocked != badge.isUnlocked) badge.copy(isUnlocked = unlocked) else badge
            }
        } catch (e: Exception) {
            if (android.util.Log.isLoggable(TAG, android.util.Log.ERROR))
                android.util.Log.e(TAG, "Error updating badge unlock status: ${e.message}")
            badges
        }
    }

    /**
     * Calculate current XP and level.
     * Uses a single getAllCompletions() query instead of N+1 per-habit queries.
     */
    private suspend fun calculateXpAndLevel() {
        try {
            // Single query for all habits and all completions
            val habits = habitRepository.getAllHabits().first()
            val allCompletions = habitRepository.getAllCompletions().first()

            // Calculate total XP
            // Base XP: 10 per completion
            var totalXp = allCompletions.size * 10

            // Streak bonus: 5 XP per streak level
            totalXp += habits.sumOf { it.streak * 5 }

            // Calculate level and XP for next level
            // Level formula: level = 1 + sqrt(totalXp / 100)
            val level = 1 + kotlin.math.sqrt(totalXp / 100.0).toInt()

            // XP for next level formula: 100 * (level + 1)^2 - 100 * level^2
            val xpForNextLevel = 100 * (level + 1) * (level + 1) - 100 * level * level

            // Current XP within this level
            val currentLevelTotalXp = 100 * level * level
            val currentXpInLevel = totalXp - currentLevelTotalXp

            _currentLevel.value = level
            _currentXp.value = currentXpInLevel
            _xpForNextLevel.value = xpForNextLevel
        } catch (e: Exception) {
            if (android.util.Log.isLoggable(TAG, android.util.Log.ERROR)) Log.e(TAG, "Error calculating XP and level", e)
        }
    }

    /**
     * Initialize rewards
     */
    private fun initializeRewards() {
        _availableRewards.value = buildList {
            add(Reward(id = "custom_theme",       title = "Custom Theme",       description = "Unlock a custom theme for the app",       xpCost = 500))
            add(Reward(id = "advanced_analytics", title = "Advanced Analytics", description = "Unlock advanced analytics features",       xpCost = 1000))
            add(Reward(id = "premium_badges",     title = "Premium Badges",     description = "Unlock premium badge collection",          xpCost = 2000))
        }
    }
}
