package com.example.myapplication.data.ml

import android.util.Log
import com.example.myapplication.data.model.Habit
import com.example.myapplication.data.model.HabitCompletion
import com.example.myapplication.data.model.NeuralPrediction
import com.example.myapplication.data.model.PredictionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Provides explanations for neural network predictions
 * Uses simple, interpretable models to explain complex neural network decisions
 */
@Singleton
class ExplainableAI @Inject constructor() {
    companion object {
        private const val TAG = "ExplainableAI"
    }
    
    // Feature importance scores
    private val _featureImportance = MutableStateFlow<Map<String, Float>>(emptyMap())
    val featureImportance: StateFlow<Map<String, Float>> = _featureImportance.asStateFlow()
    
    // Explanation text
    private val _explanation = MutableStateFlow<String>("")
    val explanation: StateFlow<String> = _explanation.asStateFlow()
    
    /**
     * Generate an explanation for a prediction
     */
    fun explainPrediction(
        prediction: NeuralPrediction,
        habit: Habit,
        completions: List<HabitCompletion>,
        contextFeatures: FloatArray
    ) {
        try {
            when (prediction.predictionType) {
                PredictionType.COMPLETION_LIKELIHOOD -> explainCompletionLikelihood(prediction, habit, completions, contextFeatures)
                PredictionType.STREAK_CONTINUATION -> explainStreakContinuation(prediction, habit, completions, contextFeatures)
                PredictionType.OPTIMAL_TIME -> explainOptimalTime(prediction, habit, completions, contextFeatures)
                else -> generateGenericExplanation(prediction)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating explanation: ${e.message}")
            e.printStackTrace()
            generateGenericExplanation(prediction)
        }
    }
    
    /**
     * Explain completion likelihood prediction
     */
    private fun explainCompletionLikelihood(
        prediction: NeuralPrediction,
        habit: Habit,
        completions: List<HabitCompletion>,
        contextFeatures: FloatArray
    ) {
        // Calculate feature importance using a simple linear model
        val importanceMap = mutableMapOf<String, Float>()
        
        // Streak importance
        val streakImportance = 0.3f * (habit.streak.toFloat() / 30f).coerceIn(0f, 1f)
        importanceMap["Current Streak"] = streakImportance
        
        // Goal progress importance
        val progressImportance = 0.25f * (habit.goalProgress.toFloat() / habit.goal.toFloat()).coerceIn(0f, 1f)
        importanceMap["Goal Progress"] = progressImportance
        
        // Time of day importance
        val timeOfDay = contextFeatures[ContextFeatureCollector.FEATURE_TIME_OF_DAY]
        val timeImportance = calculateTimeImportance(timeOfDay, completions)
        importanceMap["Time of Day"] = timeImportance
        
        // Day of week importance
        val dayOfWeek = contextFeatures[ContextFeatureCollector.FEATURE_DAY_OF_WEEK]
        val dayImportance = calculateDayImportance(dayOfWeek, completions)
        importanceMap["Day of Week"] = dayImportance
        
        // Activity level importance
        val activityLevel = contextFeatures[ContextFeatureCollector.FEATURE_ACTIVITY_LEVEL]
        val activityImportance = 0.1f * activityLevel
        importanceMap["Activity Level"] = activityImportance
        
        // Update feature importance
        _featureImportance.value = importanceMap
        
        // Generate explanation text
        val explanationBuilder = StringBuilder()
        
        explanationBuilder.append("You have a ${(prediction.probability * 100).toInt()}% chance of completing this habit today. ")
        
        // Add streak explanation
        if (habit.streak > 0) {
            explanationBuilder.append("Your current streak of ${habit.streak} days is a positive factor. ")
        } else {
            explanationBuilder.append("Starting a new streak today would be beneficial. ")
        }
        
        // Add time explanation
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        
        if (timeImportance > 0.15f) {
            explanationBuilder.append("This time of day (around $hourOfDay:00) has been good for you in the past. ")
        } else if (timeImportance < 0.05f) {
            explanationBuilder.append("You don't usually complete this habit at this time of day. ")
        }
        
        // Add day explanation
        val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val today = (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY) % 7
        val todayName = dayNames[today]
        
        if (dayImportance > 0.15f) {
            explanationBuilder.append("$todayName has been a good day for this habit in the past. ")
        } else if (dayImportance < 0.05f) {
            explanationBuilder.append("You don't usually complete this habit on $todayName. ")
        }
        
        // Add activity explanation
        if (activityLevel > 0.7f) {
            explanationBuilder.append("Your current high activity level may make it harder to focus on this habit. ")
        } else if (activityLevel < 0.3f) {
            explanationBuilder.append("Your current low activity level is a good opportunity for this habit. ")
        }
        
        _explanation.value = explanationBuilder.toString()
    }
    
    /**
     * Explain streak continuation prediction
     */
    private fun explainStreakContinuation(
        prediction: NeuralPrediction,
        habit: Habit,
        completions: List<HabitCompletion>,
        contextFeatures: FloatArray
    ) {
        // Calculate feature importance
        val importanceMap = mutableMapOf<String, Float>()
        
        // Streak importance
        val streakImportance = 0.4f * (habit.streak.toFloat() / 30f).coerceIn(0f, 1f)
        importanceMap["Current Streak"] = streakImportance
        
        // Consistency importance
        val consistencyImportance = calculateConsistencyImportance(completions)
        importanceMap["Consistency"] = consistencyImportance
        
        // Frequency importance
        val frequencyImportance = when (habit.frequency) {
            com.example.myapplication.data.model.HabitFrequency.DAILY -> 0.2f
            com.example.myapplication.data.model.HabitFrequency.WEEKLY -> 0.15f
            com.example.myapplication.data.model.HabitFrequency.MONTHLY -> 0.1f
            else -> 0.1f
        }
        importanceMap["Frequency"] = frequencyImportance
        
        // Difficulty importance
        val difficultyImportance = 0.2f * (1f - habit.difficulty.toFloat() / 5f)
        importanceMap["Difficulty"] = difficultyImportance
        
        // Update feature importance
        _featureImportance.value = importanceMap
        
        // Generate explanation text
        val explanationBuilder = StringBuilder()
        
        explanationBuilder.append("You have a ${(prediction.probability * 100).toInt()}% chance of maintaining your streak. ")
        
        // Add streak explanation
        if (habit.streak > 7) {
            explanationBuilder.append("Your impressive streak of ${habit.streak} days shows strong commitment. ")
        } else if (habit.streak > 0) {
            explanationBuilder.append("Your current streak of ${habit.streak} days is a good start. ")
        } else {
            explanationBuilder.append("Starting a new streak today would be beneficial. ")
        }
        
        // Add consistency explanation
        if (consistencyImportance > 0.15f) {
            explanationBuilder.append("Your consistent habit completion pattern is helping you maintain streaks. ")
        } else if (consistencyImportance < 0.05f) {
            explanationBuilder.append("Your inconsistent completion pattern makes streaks harder to maintain. ")
        }
        
        // Add difficulty explanation
        if (habit.difficulty > 3) {
            explanationBuilder.append("This habit's high difficulty level (${habit.difficulty}/5) makes streaks more challenging. ")
        } else if (habit.difficulty < 2) {
            explanationBuilder.append("This habit's low difficulty level (${habit.difficulty}/5) makes streaks easier to maintain. ")
        }
        
        _explanation.value = explanationBuilder.toString()
    }
    
    /**
     * Explain optimal time prediction
     */
    private fun explainOptimalTime(
        prediction: NeuralPrediction,
        habit: Habit,
        completions: List<HabitCompletion>,
        contextFeatures: FloatArray
    ) {
        // Calculate feature importance
        val importanceMap = mutableMapOf<String, Float>()
        
        // Historical time importance
        val historicalTimeImportance = 0.5f
        importanceMap["Historical Patterns"] = historicalTimeImportance
        
        // Activity level importance
        val activityLevel = contextFeatures[ContextFeatureCollector.FEATURE_ACTIVITY_LEVEL]
        val activityImportance = 0.2f * (1f - activityLevel)
        importanceMap["Activity Level"] = activityImportance
        
        // Light level importance
        val lightLevel = contextFeatures[ContextFeatureCollector.FEATURE_LIGHT_LEVEL]
        val lightImportance = 0.15f * lightLevel
        importanceMap["Light Level"] = lightImportance
        
        // Device usage importance
        val deviceUsage = contextFeatures[ContextFeatureCollector.FEATURE_DEVICE_USAGE]
        val usageImportance = 0.15f * (1f - deviceUsage)
        importanceMap["Device Usage"] = usageImportance
        
        // Update feature importance
        _featureImportance.value = importanceMap
        
        // Generate explanation text
        val explanationBuilder = StringBuilder()
        
        // Convert optimal time to hour
        val optimalHour = (prediction.probability * 24).toInt()
        val optimalTimeString = String.format("%02d:00", optimalHour)
        
        explanationBuilder.append("Your optimal time for this habit is around $optimalTimeString. ")
        
        // Add historical pattern explanation
        if (completions.isNotEmpty()) {
            val timeDistribution = getTimeDistribution(completions)
            val mostFrequentHour = timeDistribution.maxByOrNull { it.value }?.key
            
            if (mostFrequentHour != null && abs(mostFrequentHour - optimalHour) <= 2) {
                explanationBuilder.append("This aligns with your historical completion times. ")
            } else if (mostFrequentHour != null) {
                explanationBuilder.append("This differs from your usual time (${mostFrequentHour}:00) but may be more effective. ")
            }
        }
        
        // Add activity explanation
        if (activityLevel > 0.7f) {
            explanationBuilder.append("Lower activity periods may be better for this habit. ")
        } else if (activityLevel < 0.3f) {
            explanationBuilder.append("Your current low activity level is ideal for this habit. ")
        }
        
        // Add light explanation
        if (optimalHour >= 7 && optimalHour <= 18) {
            explanationBuilder.append("Daylight hours appear to work well for this habit. ")
        } else {
            explanationBuilder.append("Evening or night hours appear to work well for this habit. ")
        }
        
        _explanation.value = explanationBuilder.toString()
    }
    
    /**
     * Generate a generic explanation for any prediction
     */
    private fun generateGenericExplanation(prediction: NeuralPrediction) {
        val explanationBuilder = StringBuilder()
        
        explanationBuilder.append("This prediction is based on your habit history and current context. ")
        explanationBuilder.append("The model is ${(prediction.confidence * 100).toInt()}% confident in this prediction. ")
        explanationBuilder.append("As you complete more habits, the predictions will become more accurate and personalized.")
        
        _explanation.value = explanationBuilder.toString()
        _featureImportance.value = mapOf(
            "Historical Data" to 0.6f,
            "Current Context" to 0.3f,
            "Similar Users" to 0.1f
        )
    }
    
    /**
     * Calculate time importance based on historical completions
     */
    private fun calculateTimeImportance(currentTime: Float, completions: List<HabitCompletion>): Float {
        if (completions.isEmpty()) {
            return 0.1f
        }
        
        val currentHour = (currentTime * 24).toInt()
        val timeDistribution = getTimeDistribution(completions)
        
        // Check if current hour is a common completion time
        val currentHourCount = timeDistribution[currentHour] ?: 0
        val totalCompletions = completions.size
        
        return (currentHourCount.toFloat() / totalCompletions).coerceIn(0.05f, 0.3f)
    }
    
    /**
     * Calculate day importance based on historical completions
     */
    private fun calculateDayImportance(currentDay: Float, completions: List<HabitCompletion>): Float {
        if (completions.isEmpty()) {
            return 0.1f
        }
        
        val currentDayOfWeek = (currentDay * 7).toInt()
        val dayDistribution = getDayDistribution(completions)
        
        // Check if current day is a common completion day
        val currentDayCount = dayDistribution[currentDayOfWeek] ?: 0
        val totalCompletions = completions.size
        
        return (currentDayCount.toFloat() / totalCompletions).coerceIn(0.05f, 0.3f)
    }
    
    /**
     * Calculate consistency importance based on historical completions
     */
    private fun calculateConsistencyImportance(completions: List<HabitCompletion>): Float {
        if (completions.size < 3) {
            return 0.1f
        }
        
        // Sort completions by date
        val sortedCompletions = completions.sortedBy { it.completionDate }
        
        // Calculate time differences between consecutive completions
        val timeDiffs = mutableListOf<Long>()
        for (i in 1 until sortedCompletions.size) {
            val diff = sortedCompletions[i].completionDate - sortedCompletions[i-1].completionDate
            timeDiffs.add(diff)
        }
        
        // Calculate standard deviation of time differences
        val mean = timeDiffs.average()
        val variance = timeDiffs.map { (it - mean) * (it - mean) }.average()
        val stdDev = Math.sqrt(variance)
        
        // Normalize: lower stdDev means higher consistency
        val normalizedStdDev = (stdDev / mean).coerceIn(0.0, 2.0)
        val consistencyScore = (1.0 - normalizedStdDev / 2.0).coerceIn(0.05, 0.3)
        
        return consistencyScore.toFloat()
    }
    
    /**
     * Get distribution of completions by hour of day
     */
    private fun getTimeDistribution(completions: List<HabitCompletion>): Map<Int, Int> {
        val distribution = mutableMapOf<Int, Int>()
        
        for (completion in completions) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = completion.completionDate
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            
            distribution[hour] = (distribution[hour] ?: 0) + 1
        }
        
        return distribution
    }
    
    /**
     * Get distribution of completions by day of week
     */
    private fun getDayDistribution(completions: List<HabitCompletion>): Map<Int, Int> {
        val distribution = mutableMapOf<Int, Int>()
        
        for (completion in completions) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = completion.completionDate
            val day = (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY) % 7
            
            distribution[day] = (distribution[day] ?: 0) + 1
        }
        
        return distribution
    }
}
