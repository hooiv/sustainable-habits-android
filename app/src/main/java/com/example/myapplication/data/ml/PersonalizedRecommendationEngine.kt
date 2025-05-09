package com.example.myapplication.data.ml

import android.util.Log
import com.example.myapplication.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates personalized recommendations for habits
 * Uses multiple ML models to create a comprehensive recommendation system
 */
@Singleton
class PersonalizedRecommendationEngine @Inject constructor(
    private val reinforcementLearningAgent: ReinforcementLearningAgent,
    private val explainableAI: ExplainableAI,
    private val contextFeatureCollector: ContextFeatureCollector
) {
    companion object {
        private const val TAG = "RecommendationEngine"
        
        // Recommendation types
        const val RECOMMENDATION_TIMING = 0
        const val RECOMMENDATION_STREAK = 1
        const val RECOMMENDATION_MODIFICATION = 2
        const val RECOMMENDATION_MOTIVATION = 3
        const val RECOMMENDATION_CONTEXT = 4
    }
    
    // Current recommendations
    private val _recommendations = MutableStateFlow<List<HabitRecommendation>>(emptyList())
    val recommendations: StateFlow<List<HabitRecommendation>> = _recommendations.asStateFlow()
    
    /**
     * Generate personalized recommendations for a habit
     */
    fun generateRecommendations(
        habit: Habit,
        completions: List<HabitCompletion>,
        predictions: List<NeuralPrediction>
    ) {
        try {
            val newRecommendations = mutableListOf<HabitRecommendation>()
            
            // Get context features
            val contextFeatures = contextFeatureCollector.contextFeatures.value
            
            // Generate timing recommendation
            val timingRecommendation = generateTimingRecommendation(habit, completions, predictions, contextFeatures)
            if (timingRecommendation != null) {
                newRecommendations.add(timingRecommendation)
            }
            
            // Generate streak recommendation
            val streakRecommendation = generateStreakRecommendation(habit, completions, predictions)
            if (streakRecommendation != null) {
                newRecommendations.add(streakRecommendation)
            }
            
            // Generate habit modification recommendation
            val modificationRecommendation = generateModificationRecommendation(habit, completions, predictions)
            if (modificationRecommendation != null) {
                newRecommendations.add(modificationRecommendation)
            }
            
            // Generate motivation recommendation
            val motivationRecommendation = generateMotivationRecommendation(habit, completions, predictions)
            if (motivationRecommendation != null) {
                newRecommendations.add(motivationRecommendation)
            }
            
            // Generate context recommendation
            val contextRecommendation = generateContextRecommendation(habit, completions, contextFeatures)
            if (contextRecommendation != null) {
                newRecommendations.add(contextRecommendation)
            }
            
            // Update recommendations
            _recommendations.value = newRecommendations
            
            Log.d(TAG, "Generated ${newRecommendations.size} recommendations for habit: ${habit.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Error generating recommendations: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Generate timing recommendation
     */
    private fun generateTimingRecommendation(
        habit: Habit,
        completions: List<HabitCompletion>,
        predictions: List<NeuralPrediction>,
        contextFeatures: FloatArray
    ): HabitRecommendation? {
        // Initialize reinforcement learning agent
        reinforcementLearningAgent.initialize(habit, completions)
        
        // Update state and get recommended action
        reinforcementLearningAgent.updateState(habit, contextFeatures)
        val action = reinforcementLearningAgent.recommendedAction.value ?: return null
        
        // Get action description
        val actionDescription = reinforcementLearningAgent.getActionDescription(action)
        
        // Find optimal time prediction
        val optimalTimePrediction = predictions
            .filter { it.predictionType == PredictionType.OPTIMAL_TIME }
            .maxByOrNull { it.timestamp }
        
        // Create recommendation
        val recommendationText = if (optimalTimePrediction != null) {
            val optimalHour = (optimalTimePrediction.probability * 24).toInt()
            val optimalTimeString = String.format("%02d:00", optimalHour)
            "$actionDescription. Your optimal time is around $optimalTimeString."
        } else {
            actionDescription
        }
        
        return HabitRecommendation(
            id = UUID.randomUUID().toString(),
            habitId = habit.id,
            type = RECOMMENDATION_TIMING,
            title = "Timing Recommendation",
            description = recommendationText,
            confidence = 0.8f,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Generate streak recommendation
     */
    private fun generateStreakRecommendation(
        habit: Habit,
        completions: List<HabitCompletion>,
        predictions: List<NeuralPrediction>
    ): HabitRecommendation? {
        // Find streak continuation prediction
        val streakPrediction = predictions
            .filter { it.predictionType == PredictionType.STREAK_CONTINUATION }
            .maxByOrNull { it.timestamp }
            ?: return null
        
        // Create recommendation based on streak prediction
        val (title, description) = when {
            streakPrediction.probability > 0.8f -> {
                Pair(
                    "Keep Your Streak Going!",
                    "You have a great chance (${(streakPrediction.probability * 100).toInt()}%) of continuing your ${habit.streak}-day streak. Keep up the good work!"
                )
            }
            streakPrediction.probability > 0.5f -> {
                Pair(
                    "Maintain Your Streak",
                    "You have a decent chance (${(streakPrediction.probability * 100).toInt()}%) of continuing your ${habit.streak}-day streak. Try to complete this habit today."
                )
            }
            else -> {
                Pair(
                    "Streak at Risk",
                    "Your ${habit.streak}-day streak might be at risk (${(streakPrediction.probability * 100).toInt()}% chance of continuing). Make this habit a priority today."
                )
            }
        }
        
        return HabitRecommendation(
            id = UUID.randomUUID().toString(),
            habitId = habit.id,
            type = RECOMMENDATION_STREAK,
            title = title,
            description = description,
            confidence = streakPrediction.confidence,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Generate habit modification recommendation
     */
    private fun generateModificationRecommendation(
        habit: Habit,
        completions: List<HabitCompletion>,
        predictions: List<NeuralPrediction>
    ): HabitRecommendation? {
        // Only generate modification recommendations if we have enough data
        if (completions.size < 5) {
            return null
        }
        
        // Find completion likelihood prediction
        val completionPrediction = predictions
            .filter { it.predictionType == PredictionType.COMPLETION_LIKELIHOOD }
            .maxByOrNull { it.timestamp }
            ?: return null
        
        // Generate recommendation based on completion likelihood
        if (completionPrediction.probability < 0.4f) {
            // Habit seems difficult to maintain, suggest modifications
            
            // Check if frequency might be too high
            val daysBetweenCompletions = calculateAverageDaysBetweenCompletions(completions)
            
            val (title, description) = when (habit.frequency) {
                HabitFrequency.DAILY -> {
                    if (daysBetweenCompletions > 2) {
                        Pair(
                            "Adjust Your Frequency",
                            "This daily habit seems challenging to maintain. Consider changing to a weekly frequency to build consistency."
                        )
                    } else {
                        Pair(
                            "Simplify Your Habit",
                            "This habit might be too challenging. Try breaking it down into smaller steps or reducing your goal."
                        )
                    }
                }
                HabitFrequency.WEEKLY -> {
                    if (daysBetweenCompletions > 10) {
                        Pair(
                            "Adjust Your Frequency",
                            "This weekly habit seems challenging to maintain. Consider changing to a monthly frequency to build consistency."
                        )
                    } else {
                        Pair(
                            "Simplify Your Habit",
                            "This habit might be too challenging. Try breaking it down into smaller steps or reducing your goal."
                        )
                    }
                }
                else -> {
                    Pair(
                        "Simplify Your Habit",
                        "This habit might be too challenging. Try breaking it down into smaller steps or reducing your goal."
                    )
                }
            }
            
            return HabitRecommendation(
                id = UUID.randomUUID().toString(),
                habitId = habit.id,
                type = RECOMMENDATION_MODIFICATION,
                title = title,
                description = description,
                confidence = 0.7f,
                timestamp = System.currentTimeMillis()
            )
        }
        
        return null
    }
    
    /**
     * Generate motivation recommendation
     */
    private fun generateMotivationRecommendation(
        habit: Habit,
        completions: List<HabitCompletion>,
        predictions: List<NeuralPrediction>
    ): HabitRecommendation? {
        // Generate different types of motivation based on habit progress
        
        val (title, description) = when {
            habit.streak > 20 -> {
                Pair(
                    "Impressive Streak!",
                    "You've maintained this habit for ${habit.streak} days! That's a significant achievement that puts you ahead of 95% of people."
                )
            }
            habit.streak > 10 -> {
                Pair(
                    "Building a Strong Habit",
                    "Your ${habit.streak}-day streak shows real commitment. Research shows that habits start to become automatic after about 21 days."
                )
            }
            habit.streak > 0 -> {
                Pair(
                    "Keep Building Momentum",
                    "You've completed this habit ${habit.streak} days in a row. Each day makes the habit stronger and easier to maintain."
                )
            }
            completions.isNotEmpty() -> {
                Pair(
                    "Restart Your Habit",
                    "You've successfully completed this habit ${completions.size} times. Today is a perfect day to restart your streak!"
                )
            }
            else -> {
                Pair(
                    "Start Your Journey",
                    "The best time to start a new habit was yesterday. The second best time is today. You've got this!"
                )
            }
        }
        
        return HabitRecommendation(
            id = UUID.randomUUID().toString(),
            habitId = habit.id,
            type = RECOMMENDATION_MOTIVATION,
            title = title,
            description = description,
            confidence = 0.9f,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Generate context-based recommendation
     */
    private fun generateContextRecommendation(
        habit: Habit,
        completions: List<HabitCompletion>,
        contextFeatures: FloatArray
    ): HabitRecommendation? {
        // Generate recommendations based on current context
        
        // Check time of day
        val timeOfDay = contextFeatures[ContextFeatureCollector.FEATURE_TIME_OF_DAY]
        val hourOfDay = (timeOfDay * 24).toInt()
        
        // Check activity level
        val activityLevel = contextFeatures[ContextFeatureCollector.FEATURE_ACTIVITY_LEVEL]
        
        // Check location
        val atHome = contextFeatures[ContextFeatureCollector.FEATURE_LOCATION_HOME] > 0.7f
        val atWork = contextFeatures[ContextFeatureCollector.FEATURE_LOCATION_WORK] > 0.7f
        
        // Generate context-specific recommendation
        val (title, description) = when {
            hourOfDay in 5..9 && habit.name.contains("morning", ignoreCase = true) -> {
                Pair(
                    "Perfect Morning Timing",
                    "It's morning time - ideal for your '${habit.name}' habit. Morning habits set a positive tone for the day."
                )
            }
            hourOfDay in 21..23 && habit.name.contains("night", ignoreCase = true) -> {
                Pair(
                    "Evening Wind-Down",
                    "It's evening - a great time for your '${habit.name}' habit. Evening routines improve sleep quality."
                )
            }
            activityLevel < 0.3f && !habit.name.contains("exercise", ignoreCase = true) -> {
                Pair(
                    "Low Activity Period",
                    "You're currently in a period of low activity - a good opportunity for your '${habit.name}' habit."
                )
            }
            activityLevel > 0.7f && habit.name.contains("exercise", ignoreCase = true) -> {
                Pair(
                    "High Energy Time",
                    "Your energy level is high - perfect for your '${habit.name}' habit."
                )
            }
            atHome && habit.name.contains("home", ignoreCase = true) -> {
                Pair(
                    "Home Environment",
                    "You're at home - an ideal location for your '${habit.name}' habit."
                )
            }
            atWork && habit.name.contains("work", ignoreCase = true) -> {
                Pair(
                    "Work Environment",
                    "You're at work - a good time for your '${habit.name}' habit."
                )
            }
            else -> return null // No relevant context recommendation
        }
        
        return HabitRecommendation(
            id = UUID.randomUUID().toString(),
            habitId = habit.id,
            type = RECOMMENDATION_CONTEXT,
            title = title,
            description = description,
            confidence = 0.75f,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Calculate average days between completions
     */
    private fun calculateAverageDaysBetweenCompletions(completions: List<HabitCompletion>): Float {
        if (completions.size < 2) {
            return 0f
        }
        
        val sortedCompletions = completions.sortedBy { it.completionDate }
        var totalDays = 0L
        
        for (i in 1 until sortedCompletions.size) {
            val daysDiff = (sortedCompletions[i].completionDate - sortedCompletions[i-1].completionDate) / (1000 * 60 * 60 * 24)
            totalDays += daysDiff
        }
        
        return totalDays.toFloat() / (sortedCompletions.size - 1)
    }
}
