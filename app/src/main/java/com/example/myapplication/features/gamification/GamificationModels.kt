package com.example.myapplication.features.gamification

import java.util.*

/**
 * Badge types
 */
enum class BadgeType {
    STREAK,      // Streak-based badges
    COMPLETION,  // Completion count badges
    CATEGORY,    // Category-specific badges
    SPECIAL      // Special achievement badges
}

/**
 * Represents a badge/achievement
 */
data class Badge(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val type: BadgeType,
    val milestone: Int = 0,
    val category: String = "",
    var isUnlocked: Boolean = false,
    val unlockedDate: Date? = null
)

/**
 * Represents a reward that can be unlocked with XP
 */
data class Reward(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val xpCost: Int,
    var isUnlocked: Boolean = false,
    val unlockedDate: Date? = null
)

/**
 * Represents a level-up event
 */
data class LevelUpEvent(
    val oldLevel: Int,
    val newLevel: Int,
    val unlockedRewards: List<Reward> = emptyList()
)

/**
 * Represents a badge unlock event
 */
data class BadgeUnlockEvent(
    val badge: Badge,
    val xpAwarded: Int
)
