package com.example.myapplication.features.gamification

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.model.HabitCompletion
import com.example.myapplication.core.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for gamification features
 */
@HiltViewModel
class GamificationViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val gamificationManager: GamificationManager
) : ViewModel() {
    private val TAG = "GamificationViewModel"
    
    // Current XP
    private val _currentXp = MutableStateFlow(0)
    val currentXp: StateFlow<Int> = _currentXp.asStateFlow()
    
    // Current level
    private val _currentLevel = MutableStateFlow(1)
    val currentLevel: StateFlow<Int> = _currentLevel.asStateFlow()
    
    // XP for next level
    private val _xpForNextLevel = MutableStateFlow(100)
    val xpForNextLevel: StateFlow<Int> = _xpForNextLevel.asStateFlow()
    
    // All badges
    private val _allBadges = MutableStateFlow<List<Badge>>(emptyList())
    val allBadges: StateFlow<List<Badge>> = _allBadges.asStateFlow()
    
    // Unlocked badges
    private val _unlockedBadges = MutableStateFlow<List<Badge>>(emptyList())
    val unlockedBadges: StateFlow<List<Badge>> = _unlockedBadges.asStateFlow()
    
    // Recently unlocked badge
    private val _recentlyUnlockedBadge = MutableStateFlow<Badge?>(null)
    val recentlyUnlockedBadge: StateFlow<Badge?> = _recentlyUnlockedBadge.asStateFlow()
    
    // Available rewards
    private val _availableRewards = MutableStateFlow<List<Reward>>(emptyList())
    val availableRewards: StateFlow<List<Reward>> = _availableRewards.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Show badge unlock animation
    private val _showBadgeUnlockAnimation = MutableStateFlow(false)
    val showBadgeUnlockAnimation: StateFlow<Boolean> = _showBadgeUnlockAnimation.asStateFlow()
    
    // XP awarded for badge
    private val _xpAwarded = MutableStateFlow(0)
    val xpAwarded: StateFlow<Int> = _xpAwarded.asStateFlow()
    
    init {
        loadGamificationData()
    }
    
    /**
     * Load gamification data
     */
    fun loadGamificationData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Initialize gamification system
                gamificationManager.initialize()
                
                // Load XP and level
                _currentXp.value = gamificationManager.currentXp.value
                _currentLevel.value = gamificationManager.currentLevel.value
                _xpForNextLevel.value = gamificationManager.xpForNextLevel.value
                
                // Load badges
                _allBadges.value = gamificationManager.allBadges.value
                _unlockedBadges.value = gamificationManager.allBadges.value.filter { it.isUnlocked }
                
                // Load rewards
                _availableRewards.value = gamificationManager.availableRewards.value
                
                Log.d(TAG, "Gamification data loaded: Level ${_currentLevel.value}, XP ${_currentXp.value}/${_xpForNextLevel.value}")
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load gamification data: ${e.message}"
                Log.e(TAG, "Error loading gamification data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Check for badge unlocks when a habit is completed
     */
    fun checkBadgeUnlocks(habit: Habit, completion: HabitCompletion) {
        viewModelScope.launch {
            try {
                // Get all habits
                val habits = habitRepository.getAllHabits().first()
                
                // Get all completions
                val allCompletions = mutableListOf<HabitCompletion>()
                habits.forEach { h ->
                    val completions = habitRepository.getHabitCompletions(h.id).first()
                    allCompletions.addAll(completions)
                }
                
                // Check for streak badges
                if (habit.streak > 0) {
                    val streakBadges = _allBadges.value.filter { 
                        it.type == BadgeType.STREAK && 
                        habit.streak >= it.milestone && 
                        !it.isUnlocked 
                    }
                    
                    if (streakBadges.isNotEmpty()) {
                        val badge = streakBadges.first()
                        unlockBadge(badge)
                    }
                }
                
                // Check for completion badges
                val completionCount = allCompletions.size
                val completionBadges = _allBadges.value.filter { 
                    it.type == BadgeType.COMPLETION && 
                    completionCount >= it.milestone && 
                    !it.isUnlocked 
                }
                
                if (completionBadges.isNotEmpty()) {
                    val badge = completionBadges.first()
                    unlockBadge(badge)
                }
                
                // Check for category badges
                val categoryCount = habits.count { it.category == habit.category }
                val categoryBadges = _allBadges.value.filter { 
                    it.type == BadgeType.CATEGORY && 
                    it.category == habit.category &&
                    categoryCount >= it.milestone && 
                    !it.isUnlocked 
                }
                
                if (categoryBadges.isNotEmpty()) {
                    val badge = categoryBadges.first()
                    unlockBadge(badge)
                }
                
                // Check for special badges
                val calendar = Calendar.getInstance()
                calendar.time = Date(completion.completionDate)
                
                // Early bird badge
                if (calendar.get(Calendar.HOUR_OF_DAY) < 8) {
                    val earlyBirdBadge = _allBadges.value.find { it.id == "early_bird" && !it.isUnlocked }
                    if (earlyBirdBadge != null) {
                        val earlyMorningCompletions = allCompletions.count {
                            val completionCalendar = Calendar.getInstance()
                            completionCalendar.time = Date(it.completionDate)
                            completionCalendar.get(Calendar.HOUR_OF_DAY) < 8
                        }
                        
                        if (earlyMorningCompletions >= 5) {
                            unlockBadge(earlyBirdBadge)
                        }
                    }
                }
                
                // Night owl badge
                if (calendar.get(Calendar.HOUR_OF_DAY) >= 22) {
                    val nightOwlBadge = _allBadges.value.find { it.id == "night_owl" && !it.isUnlocked }
                    if (nightOwlBadge != null) {
                        val lateNightCompletions = allCompletions.count {
                            val completionCalendar = Calendar.getInstance()
                            completionCalendar.time = Date(it.completionDate)
                            completionCalendar.get(Calendar.HOUR_OF_DAY) >= 22
                        }
                        
                        if (lateNightCompletions >= 5) {
                            unlockBadge(nightOwlBadge)
                        }
                    }
                }
                
                // Weekend warrior badge
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                    val weekendBadge = _allBadges.value.find { it.id == "weekend_warrior" && !it.isUnlocked }
                    if (weekendBadge != null) {
                        val weekendCompletions = allCompletions.count {
                            val completionCalendar = Calendar.getInstance()
                            completionCalendar.time = Date(it.completionDate)
                            val completionDay = completionCalendar.get(Calendar.DAY_OF_WEEK)
                            completionDay == Calendar.SATURDAY || completionDay == Calendar.SUNDAY
                        }
                        
                        if (weekendCompletions >= 10) {
                            unlockBadge(weekendBadge)
                        }
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to check badge unlocks: ${e.message}"
                Log.e(TAG, "Error checking badge unlocks", e)
            }
        }
    }
    
    /**
     * Unlock a badge
     */
    private fun unlockBadge(badge: Badge) {
        viewModelScope.launch {
            try {
                // Update badge
                val updatedBadges = _allBadges.value.toMutableList()
                val index = updatedBadges.indexOfFirst { it.id == badge.id }
                if (index >= 0) {
                    updatedBadges[index] = badge.copy(isUnlocked = true, unlockedDate = Date())
                    _allBadges.value = updatedBadges
                    _unlockedBadges.value = updatedBadges.filter { it.isUnlocked }
                }
                
                // Award XP
                val xpAwarded = when (badge.type) {
                    BadgeType.STREAK -> 50
                    BadgeType.COMPLETION -> 100
                    BadgeType.CATEGORY -> 75
                    BadgeType.SPECIAL -> 150
                }
                
                _xpAwarded.value = xpAwarded
                
                // Show badge unlock animation
                _recentlyUnlockedBadge.value = badge
                _showBadgeUnlockAnimation.value = true
                
                Log.d(TAG, "Badge unlocked: ${badge.title}, XP awarded: $xpAwarded")
            } catch (e: Exception) {
                _errorMessage.value = "Failed to unlock badge: ${e.message}"
                Log.e(TAG, "Error unlocking badge", e)
            }
        }
    }
}
