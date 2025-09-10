package com.example.myapplication.core.network.ml

import android.util.Log
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.model.HabitCompletion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Implements anomaly detection for habit patterns
 * Uses statistical methods and isolation forest
 */
@Singleton
class AnomalyDetector @Inject constructor() {
    companion object {
        private const val TAG = "AnomalyDetector"

        // Anomaly detection parameters
        private const val Z_SCORE_THRESHOLD = 2.5f // Z-score threshold for statistical anomalies
        private const val ISOLATION_FOREST_TREES = 100 // Number of trees in isolation forest
        private const val ISOLATION_FOREST_SUBSAMPLE = 256 // Subsample size for isolation forest
        private const val ANOMALY_THRESHOLD = 0.6f // Threshold for anomaly score (0-1)
    }

    // Detected anomalies
    private val _anomalies = MutableStateFlow<List<HabitAnomaly>>(emptyList())
    val anomalies: StateFlow<List<HabitAnomaly>> = _anomalies.asStateFlow()

    /**
     * Detect anomalies in habit completions
     */
    suspend fun detectAnomalies(
        habit: Habit,
        completions: List<HabitCompletion>
    ): List<HabitAnomaly> = withContext(Dispatchers.Default) {
        try {
            if (completions.size < 5) {
                // Not enough data for anomaly detection
                return@withContext emptyList()
            }

            val anomalies = mutableListOf<HabitAnomaly>()

            // Detect time-based anomalies
            val timeAnomalies = detectTimeAnomalies(completions)
            anomalies.addAll(timeAnomalies)

            // Detect frequency anomalies
            val frequencyAnomalies = detectFrequencyAnomalies(habit, completions)
            anomalies.addAll(frequencyAnomalies)

            // Detect pattern anomalies
            val patternAnomalies = detectPatternAnomalies(completions)
            anomalies.addAll(patternAnomalies)

            // Update state
            _anomalies.value = anomalies

            Log.d(TAG, "Detected ${anomalies.size} anomalies in habit: ${habit.name}")

            return@withContext anomalies
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting anomalies: ${e.message}")
            e.printStackTrace()
            return@withContext emptyList()
        }
    }

    /**
     * Detect anomalies in completion times
     */
    private fun detectTimeAnomalies(completions: List<HabitCompletion>): List<HabitAnomaly> {
        val anomalies = mutableListOf<HabitAnomaly>()

        // Extract hour of day for each completion
        val hours = completions.map { completion ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = completion.completionDate
            calendar.get(Calendar.HOUR_OF_DAY)
        }

        // Calculate mean and standard deviation
        val mean = hours.average()
        val stdDev = calculateStandardDeviationForInts(hours, mean)

        // Detect anomalies using Z-score
        for (i in completions.indices) {
            val hour = hours[i]
            val zScore = abs((hour - mean) / stdDev).toFloat()

            if (zScore > Z_SCORE_THRESHOLD) {
                val completion = completions[i]
                val anomaly = HabitAnomaly(
                    id = UUID.randomUUID().toString(),
                    habitId = completion.habitId,
                    completionId = completion.id,
                    timestamp = completion.completionDate,
                    type = AnomalyType.TIME,
                    score = zScore / (Z_SCORE_THRESHOLD * 2),
                    description = "Unusual completion time: $hour:00 (typically around ${mean.toInt()}:00)"
                )

                anomalies.add(anomaly)
            }
        }

        return anomalies
    }

    /**
     * Detect anomalies in completion frequency
     */
    private fun detectFrequencyAnomalies(
        habit: Habit,
        completions: List<HabitCompletion>
    ): List<HabitAnomaly> {
        val anomalies = mutableListOf<HabitAnomaly>()

        // Sort completions by date
        val sortedCompletions = completions.sortedBy { it.completionDate }

        // Calculate time differences between consecutive completions
        val timeDiffs = mutableListOf<Long>()
        for (i in 1 until sortedCompletions.size) {
            val diff = sortedCompletions[i].completionDate - sortedCompletions[i-1].completionDate
            timeDiffs.add(diff / (1000 * 60 * 60 * 24)) // Convert to days
        }

        // Calculate expected time difference based on habit frequency
        val expectedDiff = when (habit.frequency) {
            com.example.myapplication.core.data.model.HabitFrequency.DAILY -> 1L
            com.example.myapplication.core.data.model.HabitFrequency.WEEKLY -> 7L
            com.example.myapplication.core.data.model.HabitFrequency.MONTHLY -> 30L
            else -> 1L
        }

        // Calculate mean and standard deviation
        val mean = timeDiffs.average()
        val stdDev = calculateStandardDeviation(timeDiffs.map { it.toDouble() }, mean)

        // Detect anomalies using Z-score
        for (i in timeDiffs.indices) {
            val diff = timeDiffs[i]
            val zScore = abs((diff - mean) / stdDev).toFloat()

            if (zScore > Z_SCORE_THRESHOLD) {
                val completion = sortedCompletions[i + 1]
                val anomaly = HabitAnomaly(
                    id = UUID.randomUUID().toString(),
                    habitId = completion.habitId,
                    completionId = completion.id,
                    timestamp = completion.completionDate,
                    type = AnomalyType.FREQUENCY,
                    score = zScore / (Z_SCORE_THRESHOLD * 2),
                    description = if (diff > expectedDiff) {
                        "Unusually long gap: $diff days (expected around $expectedDiff days)"
                    } else {
                        "Unusually short gap: $diff days (expected around $expectedDiff days)"
                    }
                )

                anomalies.add(anomaly)
            }
        }

        return anomalies
    }

    /**
     * Detect anomalies in completion patterns
     */
    private fun detectPatternAnomalies(completions: List<HabitCompletion>): List<HabitAnomaly> {
        val anomalies = mutableListOf<HabitAnomaly>()

        // Extract features for each completion
        val features = completions.map { completion ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = completion.completionDate

            val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY) / 24.0
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) / 7.0
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH) / 31.0

            Triple(hourOfDay, dayOfWeek, dayOfMonth)
        }

        // Implement isolation forest algorithm
        val anomalyScores = isolationForest(features)

        // Detect anomalies using anomaly scores
        for (i in completions.indices) {
            val score = anomalyScores[i]

            if (score > ANOMALY_THRESHOLD) {
                val completion = completions[i]
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = completion.completionDate

                val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                val dayNames = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
                val dayName = dayNames[dayOfWeek - 1]

                val anomaly = HabitAnomaly(
                    id = UUID.randomUUID().toString(),
                    habitId = completion.habitId,
                    completionId = completion.id,
                    timestamp = completion.completionDate,
                    type = AnomalyType.PATTERN,
                    score = score,
                    description = "Unusual pattern: completed on $dayName at $hourOfDay:00"
                )

                anomalies.add(anomaly)
            }
        }

        return anomalies
    }

    /**
     * Implement isolation forest algorithm for anomaly detection
     */
    private fun isolationForest(
        features: List<Triple<Double, Double, Double>>
    ): List<Float> {
        // In a real implementation, this would be a full isolation forest
        // For this demo, we'll use a simplified approach

        val scores = mutableListOf<Float>()
        val random = Random()

        for (i in features.indices) {
            val feature = features[i]

            // Calculate average distance to other points
            var totalDistance = 0.0
            var count = 0

            for (j in features.indices) {
                if (i != j) {
                    val otherFeature = features[j]
                    val distance = euclideanDistance(feature, otherFeature)
                    totalDistance += distance
                    count++
                }
            }

            val avgDistance = if (count > 0) totalDistance / count else 0.0

            // Normalize to 0-1 range (higher means more anomalous)
            val normalizedDistance = (avgDistance * 5).coerceIn(0.0, 1.0)

            // Add some randomness for demo purposes
            val randomFactor = random.nextDouble() * 0.2
            val score = (normalizedDistance + randomFactor).coerceIn(0.0, 1.0).toFloat()

            scores.add(score)
        }

        return scores
    }

    /**
     * Calculate Euclidean distance between two points
     */
    private fun euclideanDistance(
        a: Triple<Double, Double, Double>,
        b: Triple<Double, Double, Double>
    ): Double {
        val dx = a.first - b.first
        val dy = a.second - b.second
        val dz = a.third - b.third
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    /**
     * Calculate standard deviation
     */
    private fun calculateStandardDeviation(values: List<Double>, mean: Double): Double {
        if (values.isEmpty()) return 0.0

        val variance = values.map { (it - mean) * (it - mean) }.average()
        return sqrt(variance)
    }

    /**
     * Calculate standard deviation for integers
     */
    private fun calculateStandardDeviationForInts(values: List<Int>, mean: Double): Double {
        if (values.isEmpty()) return 0.0

        val variance = values.map { (it - mean) * (it - mean) }.average()
        return sqrt(variance)
    }

    /**
     * Get anomaly explanation
     */
    fun getAnomalyExplanation(anomaly: HabitAnomaly): String {
        return when (anomaly.type) {
            AnomalyType.TIME -> {
                "This completion occurred at an unusual time compared to your typical pattern. " +
                "You might want to consider if this time works better for you or if it was just a one-time exception."
            }
            AnomalyType.FREQUENCY -> {
                "The time between this completion and the previous one was unusual. " +
                "This could indicate a change in your habit routine or a temporary disruption."
            }
            AnomalyType.PATTERN -> {
                "This completion doesn't fit your usual pattern in terms of day of week and time of day. " +
                "Consider if this new pattern might work better for maintaining your habit."
            }
        }
    }
}

/**
 * Represents a detected anomaly in habit data
 */
data class HabitAnomaly(
    val id: String,
    val habitId: String,
    val completionId: String,
    val timestamp: Long,
    val type: AnomalyType,
    val score: Float, // 0-1 scale, higher means more anomalous
    val description: String
)

/**
 * Types of anomalies
 */
enum class AnomalyType {
    TIME, // Unusual time of day
    FREQUENCY, // Unusual gap between completions
    PATTERN // Unusual pattern (combination of factors)
}
