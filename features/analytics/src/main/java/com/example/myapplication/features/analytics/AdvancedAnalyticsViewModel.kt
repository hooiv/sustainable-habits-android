package com.example.myapplication.features.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.model.HabitCompletion
import com.example.myapplication.core.data.repository.HabitRepository
import com.example.myapplication.features.analytics.ui.AnalyticsInsight
import com.example.myapplication.features.analytics.ui.InsightType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for the Advanced Analytics screen
 */
@HiltViewModel
class AdvancedAnalyticsViewModel @Inject constructor(
    private val habitRepository: HabitRepository
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

    /** Emits a one-shot error message to display in the UI (null = no error). */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadData()
    }

    /**
     * Load habits and completions using [combine] so both flows are collected
     * concurrently in a single coroutine — avoids the nested-collect anti-pattern
     * that would start a new completions collector on every habits emission.
     */
    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                combine(
                    habitRepository.getAllHabits(),
                    habitRepository.getAllCompletions()
                ) { habitList, completionList -> habitList to completionList }
                    .collect { (habitList, completionList) ->
                        _habits.value = habitList
                        _completions.value = completionList

                        if (_insights.value.isEmpty() && habitList.isNotEmpty() && completionList.isNotEmpty()) {
                            generateInsights()
                        }

                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Error loading data: ${e.message}"
            }
        }
    }

    /** Clears the current error so the UI can dismiss the message. */
    fun clearError() {
        _errorMessage.value = null
    }

    fun refreshData() {
        loadData()
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun generateInsights() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val habitList = _habits.value
                val completionList = _completions.value
                _insights.value = if (habitList.isEmpty() || completionList.isEmpty()) {
                    emptyList()
                } else {
                    buildInsights(habitList, completionList)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error generating insights: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Derives real insights from [habits] and [completions].
     * Habits are referenced by stable sorted order so results are deterministic.
     */
    private fun buildInsights(
        habits: List<Habit>,
        completions: List<HabitCompletion>
    ): List<AnalyticsInsight> {
        val sorted = habits.sortedBy { it.id }
        val byHabit = completions.groupBy { it.habitId }
        val insights = mutableListOf<AnalyticsInsight>()

        // Best-performing habit (highest completion count)
        val bestHabit = sorted.maxByOrNull { byHabit[it.id]?.size ?: 0 }
        if (bestHabit != null) {
            val count = byHabit[bestHabit.id]?.size ?: 0
            insights.add(
                AnalyticsInsight(
                    id = "trend_${bestHabit.id.replace(Regex("[^a-zA-Z0-9_-]"), "_")}",
                    title = "Top Performing Habit",
                    description = "'${bestHabit.name}' leads with $count total completions — keep it up!",
                    type = InsightType.TREND_DETECTION,
                    confidence = 1.0f,
                    relatedHabitIds = listOf(bestHabit.id),
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        // Longest current streak
        val streakHabit = sorted.maxByOrNull { it.streak }
        if (streakHabit != null && streakHabit.streak > 0) {
            insights.add(
                AnalyticsInsight(
                    id = "streak_${streakHabit.id.replace(Regex("[^a-zA-Z0-9_-]"), "_")}",
                    title = "Longest Active Streak",
                    description = "'${streakHabit.name}' has a current streak of ${streakHabit.streak} day(s). Don't break the chain!",
                    type = InsightType.ACHIEVEMENT,
                    confidence = 1.0f,
                    relatedHabitIds = listOf(streakHabit.id),
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        // Pair with highest co-completion on same calendar day
        if (sorted.size >= 2) {
            var bestCorrelation = -1f
            var habitA = sorted[0]
            var habitB = sorted[1]
            for (i in sorted.indices) {
                val daysA = completionDayKeys(byHabit[sorted[i].id]) ?: continue
                for (j in i + 1 until sorted.size) {
                    val daysB = completionDayKeys(byHabit[sorted[j].id]) ?: continue
                    val intersectionSize = (daysA intersect daysB).size
                    val unionSize = daysA.size + daysB.size - intersectionSize
                    if (unionSize > 0) {
                        val jaccard = intersectionSize.toFloat() / unionSize
                        if (jaccard > bestCorrelation) {
                            bestCorrelation = jaccard
                            habitA = sorted[i]
                            habitB = sorted[j]
                        }
                    }
                }
            }
            if (bestCorrelation >= 0f) {
                val pct = (bestCorrelation * 100).toInt()
                insights.add(
                    AnalyticsInsight(
                        id = "corr_${habitA.id.replace(Regex("[^a-zA-Z0-9_-]"), "_")}_${habitB.id.replace(Regex("[^a-zA-Z0-9_-]"), "_")}",
                        title = "Habit Correlation",
                        description = "'${habitA.name}' and '${habitB.name}' are completed together $pct% of the time.",
                        type = InsightType.CORRELATION,
                        confidence = bestCorrelation,
                        relatedHabitIds = listOf(habitA.id, habitB.id),
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }

        // Habit with zero completions (needs attention)
        val neglectedHabit = sorted.firstOrNull { (byHabit[it.id]?.size ?: 0) == 0 }
        if (neglectedHabit != null) {
            insights.add(
                AnalyticsInsight(
                    id = "anomaly_${neglectedHabit.id.replace(Regex("[^a-zA-Z0-9_-]"), "_")}",
                    title = "Habit Needs Attention",
                    description = "'${neglectedHabit.name}' has no recorded completions yet. Try completing it today!",
                    type = InsightType.ANOMALY_DETECTION,
                    confidence = 1.0f,
                    relatedHabitIds = listOf(neglectedHabit.id),
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        return insights
    }

    /** Converts completion records to a set of calendar-day keys (Long) for overlap comparison. */
    private fun completionDayKeys(completions: List<HabitCompletion>?): Set<Long>? {
        if (completions.isNullOrEmpty()) return null
        return completions.map { dayKeyOf(it.completionDate) }.toSet()
    }
}

/**
 * Returns a Long key representing the calendar day of [epochMillis].
 * Uses `year * 10000L + dayOfYear` to avoid integer overflow and ensure uniqueness.
 */
internal fun dayKeyOf(epochMillis: Long): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = epochMillis
    return c.get(Calendar.YEAR) * 10000L + c.get(Calendar.DAY_OF_YEAR)
}
