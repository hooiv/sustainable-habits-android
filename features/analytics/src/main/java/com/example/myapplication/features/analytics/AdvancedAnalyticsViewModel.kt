package com.example.myapplication.features.analytics

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.model.HabitCompletion
import com.example.myapplication.core.data.repository.HabitRepository
import com.example.myapplication.features.analytics.ui.AnalyticsInsight
import com.example.myapplication.features.analytics.ui.InsightType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for the Advanced Analytics screen
 */
@HiltViewModel
class AdvancedAnalyticsViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    // State
    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()
    
    private val _completions = MutableStateFlow<List<HabitCompletion>>(emptyList())
    val completions: StateFlow<List<HabitCompletion>> = _completions.asStateFlow()
    
    private val _insights = MutableStateFlow<List<AnalyticsInsight>>(emptyList())
    val insights: StateFlow<List<AnalyticsInsight>> = _insights.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()
    
    init {
        loadData()
    }
    
    /**
     * Load habits and completions
     */
    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Load habits
                habitRepository.getAllHabits().collect { habitList ->
                    _habits.value = habitList
                    
                    // After loading habits, load completions
                    habitRepository.getAllCompletions().collect { completionList ->
                        _completions.value = completionList
                        
                        // Generate initial insights
                        if (_insights.value.isEmpty() && habitList.isNotEmpty() && completionList.isNotEmpty()) {
                            generateInsights()
                        }
                        
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                showToast("Error loading data: ${e.message}")
            }
        }
    }
    
    /**
     * Refresh data
     */
    fun refreshData() {
        loadData()
    }
    
    /**
     * Set selected tab
     */
    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }
    
    /**
     * Generate insights
     */
    fun generateInsights() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Simulate API call delay
                delay(1500)
                
                // Generate insights based on habits and completions
                val habitList = _habits.value
                val completionList = _completions.value
                
                if (habitList.isEmpty() || completionList.isEmpty()) {
                    _insights.value = emptyList()
                } else {
                    _insights.value = generateSampleInsights(habitList, completionList)
                }
            } catch (e: Exception) {
                showToast("Error generating insights: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Generate sample insights
     */
    private fun generateSampleInsights(
        habits: List<Habit>,
        completions: List<HabitCompletion>
    ): List<AnalyticsInsight> {
        val insights = mutableListOf<AnalyticsInsight>()
        
        // Trend detection insight
        if (habits.isNotEmpty()) {
            val randomHabit = habits.random()
            insights.add(
                AnalyticsInsight(
                    id = UUID.randomUUID().toString(),
                    title = "Improving Trend",
                    description = "Your consistency with '${randomHabit.name}' has improved by 23% over the past 30 days.",
                    type = InsightType.TREND_DETECTION,
                    confidence = 0.87f,
                    relatedHabitIds = listOf(randomHabit.id),
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        // Pattern recognition insight
        if (habits.size >= 2) {
            val randomHabits = habits.shuffled().take(2)
            insights.add(
                AnalyticsInsight(
                    id = UUID.randomUUID().toString(),
                    title = "Weekly Pattern Detected",
                    description = "You tend to complete '${randomHabits[0].name}' and '${randomHabits[1].name}' on the same days, typically on weekends.",
                    type = InsightType.PATTERN_RECOGNITION,
                    confidence = 0.78f,
                    relatedHabitIds = randomHabits.map { it.id },
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        // Correlation insight
        if (habits.size >= 2) {
            val randomHabits = habits.shuffled().take(2)
            insights.add(
                AnalyticsInsight(
                    id = UUID.randomUUID().toString(),
                    title = "Strong Correlation",
                    description = "When you complete '${randomHabits[0].name}', you're 75% more likely to also complete '${randomHabits[1].name}' on the same day.",
                    type = InsightType.CORRELATION,
                    confidence = 0.92f,
                    relatedHabitIds = randomHabits.map { it.id },
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        // Anomaly detection insight
        if (habits.isNotEmpty()) {
            val randomHabit = habits.random()
            insights.add(
                AnalyticsInsight(
                    id = UUID.randomUUID().toString(),
                    title = "Unusual Pattern",
                    description = "Your completion of '${randomHabit.name}' has been unusually inconsistent on Mondays compared to other days.",
                    type = InsightType.ANOMALY_DETECTION,
                    confidence = 0.81f,
                    relatedHabitIds = listOf(randomHabit.id),
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        // Prediction insight
        if (habits.isNotEmpty()) {
            val randomHabit = habits.random()
            insights.add(
                AnalyticsInsight(
                    id = UUID.randomUUID().toString(),
                    title = "Streak Prediction",
                    description = "Based on your current pattern, you're likely to achieve a 14-day streak with '${randomHabit.name}' by next week.",
                    type = InsightType.PREDICTION,
                    confidence = 0.76f,
                    relatedHabitIds = listOf(randomHabit.id),
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        // Recommendation insight
        if (habits.isNotEmpty()) {
            val randomHabit = habits.random()
            insights.add(
                AnalyticsInsight(
                    id = UUID.randomUUID().toString(),
                    title = "Optimal Time Recommendation",
                    description = "You're 40% more likely to complete '${randomHabit.name}' when you do it in the morning rather than evening.",
                    type = InsightType.RECOMMENDATION,
                    confidence = 0.85f,
                    relatedHabitIds = listOf(randomHabit.id),
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        // Achievement insight
        if (habits.isNotEmpty() && completions.isNotEmpty()) {
            val randomHabit = habits.random()
            insights.add(
                AnalyticsInsight(
                    id = UUID.randomUUID().toString(),
                    title = "Milestone Achieved",
                    description = "Congratulations! You've completed '${randomHabit.name}' 30 times since you started tracking it.",
                    type = InsightType.ACHIEVEMENT,
                    confidence = 1.0f,
                    relatedHabitIds = listOf(randomHabit.id),
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        return insights.shuffled()
    }
    
    /**
     * Show a toast message
     */
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
