package com.example.myapplication.data.ml

import android.content.Context
import android.util.Log
import com.example.myapplication.data.model.Habit
import com.example.myapplication.data.model.HabitCompletion
import com.example.myapplication.data.model.HabitFrequency
import com.example.myapplication.data.repository.HabitRepository
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Collects and processes habit data for neural network training
 */
@Singleton
class HabitDataCollector @Inject constructor(
    private val context: Context,
    private val habitRepository: HabitRepository
) {
    companion object {
        private const val TAG = "HabitDataCollector"

        // Feature indices
        const val FEATURE_STREAK = 0
        const val FEATURE_GOAL_PROGRESS = 1
        const val FEATURE_FREQUENCY = 2
        const val FEATURE_DIFFICULTY = 3
        const val FEATURE_TIME_OF_DAY = 4
        const val FEATURE_DAY_OF_WEEK = 5
        const val FEATURE_CONSISTENCY = 6
        const val FEATURE_COMPLETION_RATE = 7
        const val FEATURE_DAYS_SINCE_LAST_COMPLETION = 8
        const val FEATURE_MOOD = 9

        // Target indices
        const val TARGET_COMPLETION_LIKELIHOOD = 0
        const val TARGET_STREAK_CONTINUATION = 1
        const val TARGET_OPTIMAL_TIME = 2
    }

    /**
     * Collect training data for a specific habit
     */
    suspend fun collectTrainingData(habitId: String): List<Pair<FloatArray, FloatArray>> {
        val trainingData = mutableListOf<Pair<FloatArray, FloatArray>>()

        try {
            // Get habit and its completions
            val habit = habitRepository.getHabitById(habitId).first()
            if (habit == null) {
                Log.e(TAG, "Habit not found: $habitId")
                return emptyList()
            }

            val completions = habitRepository.getHabitCompletions(habitId).first()

            // If we have enough real data, use it
            if (completions.size >= 10) {
                trainingData.addAll(generateTrainingDataFromCompletions(habit, completions))
            }

            // Always add some synthetic data to ensure enough training examples
            trainingData.addAll(generateSyntheticTrainingData(habit, completions))

            Log.d(TAG, "Collected ${trainingData.size} training examples for habit: ${habit.name}")

            return trainingData
        } catch (e: Exception) {
            Log.e(TAG, "Error collecting training data: ${e.message}")
            e.printStackTrace()
            return generateFallbackTrainingData()
        }
    }

    /**
     * Generate training data from actual habit completions
     */
    private fun generateTrainingDataFromCompletions(
        habit: Habit,
        completions: List<HabitCompletion>
    ): List<Pair<FloatArray, FloatArray>> {
        val trainingData = mutableListOf<Pair<FloatArray, FloatArray>>()

        // Sort completions by date
        val sortedCompletions = completions.sortedBy { it.completionDate }

        // Calculate completion rate
        val completionRate = if (habit.createdDate != null) {
            val daysSinceCreation = (System.currentTimeMillis() - habit.createdDate.time) / (1000 * 60 * 60 * 24)
            if (daysSinceCreation > 0) completions.size.toFloat() / daysSinceCreation.toFloat() else 0f
            } else 0f

        // Calculate consistency (standard deviation of time between completions)
        val timesBetweenCompletions = mutableListOf<Long>()
        for (i in 1 until sortedCompletions.size) {
            val timeBetween = sortedCompletions[i].completionDate - sortedCompletions[i-1].completionDate
            timesBetweenCompletions.add(timeBetween)
        }

        val consistency = if (timesBetweenCompletions.size > 1) {
            val mean = timesBetweenCompletions.average()
            val variance = timesBetweenCompletions.map { (it - mean) * (it - mean) }.average()
            val stdDev = Math.sqrt(variance)
            // Normalize: lower stdDev means higher consistency
            1.0f - (stdDev / (mean * 2)).coerceIn(0.0, 1.0).toFloat()
        } else {
            0.5f // Default for not enough data
        }

        // For each completion, create a training example
        for (i in 1 until sortedCompletions.size) {
            val completion = sortedCompletions[i]
            val prevCompletion = sortedCompletions[i-1]

            // Calculate days since last completion
            val daysSinceLastCompletion =
                (completion.completionDate - prevCompletion.completionDate) / (1000 * 60 * 60 * 24)

            // Extract time of day (0-1 scale, 0 = midnight, 0.5 = noon, 1 = midnight)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = completion.completionDate
            val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
            val minuteOfHour = calendar.get(Calendar.MINUTE)
            val timeOfDay = (hourOfDay + minuteOfHour / 60.0f) / 24.0f

            // Extract day of week (0-1 scale, 0 = Monday, 1 = Sunday)
            val dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY) / 6.0f

            // Create feature vector
            val features = FloatArray(10) { 0f }
            features[FEATURE_STREAK] = habit.streak.toFloat() / 30f // Normalize streak
            features[FEATURE_GOAL_PROGRESS] = habit.goalProgress.toFloat() / habit.goal.toFloat()
            features[FEATURE_FREQUENCY] = when (habit.frequency) {
                HabitFrequency.DAILY -> 1f
                HabitFrequency.WEEKLY -> 0.5f
                HabitFrequency.MONTHLY -> 0.25f
                else -> 0f
            }
            features[FEATURE_DIFFICULTY] = habit.difficulty.ordinal.toFloat() / 5f
            features[FEATURE_TIME_OF_DAY] = timeOfDay
            features[FEATURE_DAY_OF_WEEK] = dayOfWeek
            features[FEATURE_CONSISTENCY] = consistency
            features[FEATURE_COMPLETION_RATE] = completionRate.coerceIn(0f, 1f)
            features[FEATURE_DAYS_SINCE_LAST_COMPLETION] = (daysSinceLastCompletion / 7f).coerceIn(0f, 1f)
            features[FEATURE_MOOD] = completion.mood?.toFloat()?.div(5f) ?: 0.5f

            // Create target vector based on actual outcomes
            val targets = FloatArray(3) { 0f }

            // Completion likelihood (based on historical pattern)
            targets[TARGET_COMPLETION_LIKELIHOOD] = 1f // It was completed

            // Streak continuation (did it continue the streak?)
            val streakContinued = if (i < sortedCompletions.size - 1) {
                val nextCompletion = sortedCompletions[i+1]
                val daysBetween = (nextCompletion.completionDate - completion.completionDate) / (1000 * 60 * 60 * 24)
                when (habit.frequency) {
                    HabitFrequency.DAILY -> daysBetween <= 1
                    HabitFrequency.WEEKLY -> daysBetween <= 7
                    HabitFrequency.MONTHLY -> daysBetween <= 31
                    else -> false
                }
            } else {
                // For the most recent completion, use current streak as indicator
                habit.streak > 0
            }
            targets[TARGET_STREAK_CONTINUATION] = if (streakContinued) 1f else 0f

            // Optimal time (based on successful completions)
            targets[TARGET_OPTIMAL_TIME] = timeOfDay

            trainingData.add(Pair(features, targets))
        }

        return trainingData
    }

    /**
     * Generate synthetic training data based on habit properties and existing completions
     */
    private fun generateSyntheticTrainingData(
        habit: Habit,
        completions: List<HabitCompletion>
    ): List<Pair<FloatArray, FloatArray>> {
        val trainingData = mutableListOf<Pair<FloatArray, FloatArray>>()
        val random = Random()

        // Extract patterns from real completions if available
        val completionTimes = mutableListOf<Float>()
        val completionDays = mutableListOf<Float>()

        completions.forEach { completion ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = completion.completionDate

            val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
            val minuteOfHour = calendar.get(Calendar.MINUTE)
            val timeOfDay = (hourOfDay + minuteOfHour / 60.0f) / 24.0f
            completionTimes.add(timeOfDay)

            val dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY) / 6.0f
            completionDays.add(dayOfWeek)
        }

        // Calculate average completion time and day if available
        val avgCompletionTime = if (completionTimes.isNotEmpty())
            completionTimes.average().toFloat() else 0.5f
        val avgCompletionDay = if (completionDays.isNotEmpty())
            completionDays.average().toFloat() else 0.5f

        // Generate synthetic examples
        for (i in 0 until 20) {
            // Base features on habit properties
            val baseFeatures = floatArrayOf(
                habit.streak.toFloat() / 30f, // Normalize streak
                habit.goalProgress.toFloat() / habit.goal.toFloat(),
                when (habit.frequency) {
                    HabitFrequency.DAILY -> 1f
                    HabitFrequency.WEEKLY -> 0.5f
                    HabitFrequency.MONTHLY -> 0.25f
                    else -> 0f
                },
                habit.difficulty.ordinal.toFloat() / 5f,
                avgCompletionTime, // Use average time if available
                avgCompletionDay, // Use average day if available
                0.5f, // Default consistency
                0.5f, // Default completion rate
                0.2f, // Default days since last completion
                0.5f  // Default mood
            )

            // Add some random variations
            val features = baseFeatures.copyOf()
            for (j in features.indices) {
                features[j] += (random.nextFloat() - 0.5f) * 0.2f
                features[j] = features[j].coerceIn(0f, 1f)
            }

            // Generate target values based on habit properties and some randomness
            val completionLikelihood = (features[FEATURE_STREAK] * 0.3f +
                                       features[FEATURE_GOAL_PROGRESS] * 0.4f +
                                       features[FEATURE_CONSISTENCY] * 0.3f)
                .coerceIn(0f, 1f)

            val streakContinuation = (features[FEATURE_STREAK] * 0.5f +
                                     features[FEATURE_COMPLETION_RATE] * 0.3f +
                                     (1f - features[FEATURE_DIFFICULTY]) * 0.2f)
                .coerceIn(0f, 1f)

            val optimalTime = features[FEATURE_TIME_OF_DAY]

            val targets = floatArrayOf(completionLikelihood, streakContinuation, optimalTime)

            trainingData.add(Pair(features, targets))
        }

        return trainingData
    }

    /**
     * Generate fallback training data when no habit data is available
     */
    private fun generateFallbackTrainingData(): List<Pair<FloatArray, FloatArray>> {
        val trainingData = mutableListOf<Pair<FloatArray, FloatArray>>()
        val random = Random()

        // Generate 20 random examples
        for (i in 0 until 20) {
            val features = FloatArray(10) { random.nextFloat() }
            val targets = FloatArray(3) { random.nextFloat() }
            trainingData.add(Pair(features, targets))
        }

        return trainingData
    }
}
