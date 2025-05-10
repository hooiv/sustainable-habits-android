package com.example.myapplication.data.ml

import android.util.Log
import com.example.myapplication.data.model.Habit
import com.example.myapplication.data.model.HabitCategory
import com.example.myapplication.data.model.HabitCompletion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.exp
import kotlin.math.sqrt

/**
 * Implements meta-learning for faster adaptation to new habits
 * Uses Model-Agnostic Meta-Learning (MAML) approach
 */
@Singleton
class MetaLearning @Inject constructor() {
    companion object {
        private const val TAG = "MetaLearning"

        // Meta-learning parameters
        private const val META_LEARNING_RATE = 0.01f
        private const val INNER_LEARNING_RATE = 0.1f
        private const val META_BATCH_SIZE = 5
        private const val INNER_STEPS = 5
        private const val FEATURE_SIZE = 10
        private const val HIDDEN_SIZE = 8
        private const val OUTPUT_SIZE = 3
    }

    // Meta-model parameters
    private val inputToHidden = Array(HIDDEN_SIZE) { FloatArray(FEATURE_SIZE) { 0f } }
    private val hiddenToOutput = Array(OUTPUT_SIZE) { FloatArray(HIDDEN_SIZE) { 0f } }

    // Meta-learning state
    private val _metaLearningProgress = MutableStateFlow(0f)
    val metaLearningProgress: StateFlow<Float> = _metaLearningProgress.asStateFlow()

    private val _adaptationProgress = MutableStateFlow(0f)
    val adaptationProgress: StateFlow<Float> = _adaptationProgress.asStateFlow()

    /**
     * Initialize meta-model
     */
    init {
        // Initialize with small random weights
        val random = Random()

        for (i in 0 until HIDDEN_SIZE) {
            for (j in 0 until FEATURE_SIZE) {
                inputToHidden[i][j] = (random.nextFloat() * 0.2f - 0.1f)
            }
        }

        for (i in 0 until OUTPUT_SIZE) {
            for (j in 0 until HIDDEN_SIZE) {
                hiddenToOutput[i][j] = (random.nextFloat() * 0.2f - 0.1f)
            }
        }

        Log.d(TAG, "Initialized meta-model")
    }

    /**
     * Perform meta-learning on multiple habits
     */
    suspend fun metaLearn(
        habits: List<Habit>,
        completionsMap: Map<String, List<HabitCompletion>>
    ): Boolean = withContext(Dispatchers.Default) {
        try {
            if (habits.size < META_BATCH_SIZE) {
                Log.d(TAG, "Not enough habits for meta-learning: ${habits.size} < $META_BATCH_SIZE")
                return@withContext false
            }

            Log.d(TAG, "Starting meta-learning with ${habits.size} habits")

            // Prepare meta-gradients
            val metaGradientsInputToHidden = Array(HIDDEN_SIZE) { FloatArray(FEATURE_SIZE) { 0f } }
            val metaGradientsHiddenToOutput = Array(OUTPUT_SIZE) { FloatArray(HIDDEN_SIZE) { 0f } }

            // For each meta-batch
            val numMetaBatches = habits.size / META_BATCH_SIZE

            for (metaBatch in 0 until numMetaBatches) {
                // Select habits for this meta-batch
                val batchHabits = habits.subList(
                    metaBatch * META_BATCH_SIZE,
                    (metaBatch + 1) * META_BATCH_SIZE
                )

                // For each habit in the meta-batch
                for (habit in batchHabits) {
                    val completions = completionsMap[habit.id] ?: continue
                    if (completions.size < 5) continue

                    // Clone meta-model parameters for task-specific adaptation
                    val taskInputToHidden = inputToHidden.map { it.clone() }.toTypedArray()
                    val taskHiddenToOutput = hiddenToOutput.map { it.clone() }.toTypedArray()

                    // Split completions into support (training) and query (validation) sets
                    val (supportCompletions, queryCompletions) = splitCompletions(completions)

                    // Prepare features and targets
                    val supportFeatures = extractFeatures(habit, supportCompletions)
                    val supportTargets = extractTargets(habit, supportCompletions)

                    val queryFeatures = extractFeatures(habit, queryCompletions)
                    val queryTargets = extractTargets(habit, queryCompletions)

                    // Inner loop adaptation
                    for (step in 0 until INNER_STEPS) {
                        // Forward pass on support set
                        val supportPredictions = mutableListOf<FloatArray>()
                        for (features in supportFeatures) {
                            val prediction = forwardPass(features, taskInputToHidden, taskHiddenToOutput)
                            supportPredictions.add(prediction)
                        }

                        // Calculate gradients on support set
                        val gradientsInputToHidden = Array(HIDDEN_SIZE) { FloatArray(FEATURE_SIZE) { 0f } }
                        val gradientsHiddenToOutput = Array(OUTPUT_SIZE) { FloatArray(HIDDEN_SIZE) { 0f } }

                        for (i in supportFeatures.indices) {
                            val features = supportFeatures[i]
                            val targets = supportTargets[i]
                            val predictions = supportPredictions[i]

                            // Calculate gradients
                            calculateGradients(
                                features, targets, predictions,
                                taskInputToHidden, taskHiddenToOutput,
                                gradientsInputToHidden, gradientsHiddenToOutput
                            )
                        }

                        // Update task-specific parameters
                        for (i in 0 until HIDDEN_SIZE) {
                            for (j in 0 until FEATURE_SIZE) {
                                taskInputToHidden[i][j] -= INNER_LEARNING_RATE * gradientsInputToHidden[i][j]
                            }
                        }

                        for (i in 0 until OUTPUT_SIZE) {
                            for (j in 0 until HIDDEN_SIZE) {
                                taskHiddenToOutput[i][j] -= INNER_LEARNING_RATE * gradientsHiddenToOutput[i][j]
                            }
                        }
                    }

                    // Forward pass on query set with adapted parameters
                    val queryPredictions = mutableListOf<FloatArray>()
                    for (features in queryFeatures) {
                        val prediction = forwardPass(features, taskInputToHidden, taskHiddenToOutput)
                        queryPredictions.add(prediction)
                    }

                    // Calculate meta-gradients on query set
                    val taskMetaGradientsInputToHidden = Array(HIDDEN_SIZE) { FloatArray(FEATURE_SIZE) { 0f } }
                    val taskMetaGradientsHiddenToOutput = Array(OUTPUT_SIZE) { FloatArray(HIDDEN_SIZE) { 0f } }

                    for (i in queryFeatures.indices) {
                        val features = queryFeatures[i]
                        val targets = queryTargets[i]
                        val predictions = queryPredictions[i]

                        // Calculate gradients
                        calculateGradients(
                            features, targets, predictions,
                            taskInputToHidden, taskHiddenToOutput,
                            taskMetaGradientsInputToHidden, taskMetaGradientsHiddenToOutput
                        )
                    }

                    // Accumulate meta-gradients
                    for (i in 0 until HIDDEN_SIZE) {
                        for (j in 0 until FEATURE_SIZE) {
                            metaGradientsInputToHidden[i][j] += taskMetaGradientsInputToHidden[i][j] / META_BATCH_SIZE
                        }
                    }

                    for (i in 0 until OUTPUT_SIZE) {
                        for (j in 0 until HIDDEN_SIZE) {
                            metaGradientsHiddenToOutput[i][j] += taskMetaGradientsHiddenToOutput[i][j] / META_BATCH_SIZE
                        }
                    }
                }

                // Update meta-model parameters
                for (i in 0 until HIDDEN_SIZE) {
                    for (j in 0 until FEATURE_SIZE) {
                        inputToHidden[i][j] -= META_LEARNING_RATE * metaGradientsInputToHidden[i][j]
                    }
                }

                for (i in 0 until OUTPUT_SIZE) {
                    for (j in 0 until HIDDEN_SIZE) {
                        hiddenToOutput[i][j] -= META_LEARNING_RATE * metaGradientsHiddenToOutput[i][j]
                    }
                }

                // Update progress
                _metaLearningProgress.value = (metaBatch + 1).toFloat() / numMetaBatches

                Log.d(TAG, "Meta-batch ${metaBatch + 1}/$numMetaBatches completed")
            }

            Log.d(TAG, "Meta-learning completed")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error during meta-learning: ${e.message}")
            e.printStackTrace()
            return@withContext false
        }
    }

    /**
     * Adapt meta-model to a new habit
     */
    suspend fun adaptToHabit(
        habit: Habit,
        completions: List<HabitCompletion>
    ): Pair<Array<FloatArray>, Array<FloatArray>> = withContext(Dispatchers.Default) {
        try {
            Log.d(TAG, "Adapting meta-model to habit: ${habit.name}")

            // Clone meta-model parameters
            val adaptedInputToHidden = inputToHidden.map { it.clone() }.toTypedArray()
            val adaptedHiddenToOutput = hiddenToOutput.map { it.clone() }.toTypedArray()

            if (completions.size < 3) {
                Log.d(TAG, "Not enough completions for adaptation: ${completions.size} < 3")
                return@withContext Pair(adaptedInputToHidden, adaptedHiddenToOutput)
            }

            // Prepare features and targets
            val features = extractFeatures(habit, completions)
            val targets = extractTargets(habit, completions)

            // Adaptation steps
            val numSteps = 10
            for (step in 0 until numSteps) {
                // Forward pass
                val predictions = mutableListOf<FloatArray>()
                for (featureVector in features) {
                    val prediction = forwardPass(featureVector, adaptedInputToHidden, adaptedHiddenToOutput)
                    predictions.add(prediction)
                }

                // Calculate gradients
                val gradientsInputToHidden = Array(HIDDEN_SIZE) { FloatArray(FEATURE_SIZE) { 0f } }
                val gradientsHiddenToOutput = Array(OUTPUT_SIZE) { FloatArray(HIDDEN_SIZE) { 0f } }

                for (i in features.indices) {
                    val featureVector = features[i]
                    val targetVector = targets[i]
                    val predictionVector = predictions[i]

                    // Calculate gradients
                    calculateGradients(
                        featureVector, targetVector, predictionVector,
                        adaptedInputToHidden, adaptedHiddenToOutput,
                        gradientsInputToHidden, gradientsHiddenToOutput
                    )
                }

                // Update adapted parameters
                for (i in 0 until HIDDEN_SIZE) {
                    for (j in 0 until FEATURE_SIZE) {
                        adaptedInputToHidden[i][j] -= INNER_LEARNING_RATE * gradientsInputToHidden[i][j]
                    }
                }

                for (i in 0 until OUTPUT_SIZE) {
                    for (j in 0 until HIDDEN_SIZE) {
                        adaptedHiddenToOutput[i][j] -= INNER_LEARNING_RATE * gradientsHiddenToOutput[i][j]
                    }
                }

                // Update progress
                _adaptationProgress.value = (step + 1).toFloat() / numSteps
            }

            Log.d(TAG, "Adaptation completed for habit: ${habit.name}")
            return@withContext Pair(adaptedInputToHidden, adaptedHiddenToOutput)
        } catch (e: Exception) {
            Log.e(TAG, "Error during adaptation: ${e.message}")
            e.printStackTrace()
            return@withContext Pair(inputToHidden, hiddenToOutput)
        }
    }

    /**
     * Make predictions using adapted model
     */
    fun predict(
        features: FloatArray,
        adaptedInputToHidden: Array<FloatArray>,
        adaptedHiddenToOutput: Array<FloatArray>
    ): FloatArray {
        return forwardPass(features, adaptedInputToHidden, adaptedHiddenToOutput)
    }

    /**
     * Forward pass through the neural network
     */
    private fun forwardPass(
        features: FloatArray,
        inputToHidden: Array<FloatArray>,
        hiddenToOutput: Array<FloatArray>
    ): FloatArray {
        // Input to hidden layer
        val hidden = FloatArray(HIDDEN_SIZE)
        for (i in 0 until HIDDEN_SIZE) {
            var sum = 0f
            for (j in 0 until FEATURE_SIZE) {
                if (j < features.size) {
                    sum += features[j] * inputToHidden[i][j]
                }
            }
            hidden[i] = sigmoid(sum)
        }

        // Hidden to output layer
        val output = FloatArray(OUTPUT_SIZE)
        for (i in 0 until OUTPUT_SIZE) {
            var sum = 0f
            for (j in 0 until HIDDEN_SIZE) {
                sum += hidden[j] * hiddenToOutput[i][j]
            }
            output[i] = sigmoid(sum)
        }

        return output
    }

    /**
     * Calculate gradients for backpropagation
     */
    private fun calculateGradients(
        features: FloatArray,
        targets: FloatArray,
        predictions: FloatArray,
        inputToHidden: Array<FloatArray>,
        hiddenToOutput: Array<FloatArray>,
        gradientsInputToHidden: Array<FloatArray>,
        gradientsHiddenToOutput: Array<FloatArray>
    ) {
        // Calculate hidden layer activations
        val hidden = FloatArray(HIDDEN_SIZE)
        for (i in 0 until HIDDEN_SIZE) {
            var sum = 0f
            for (j in 0 until FEATURE_SIZE) {
                if (j < features.size) {
                    sum += features[j] * inputToHidden[i][j]
                }
            }
            hidden[i] = sigmoid(sum)
        }

        // Calculate output layer errors
        val outputErrors = FloatArray(OUTPUT_SIZE)
        for (i in 0 until OUTPUT_SIZE) {
            outputErrors[i] = predictions[i] - targets[i]
        }

        // Calculate hidden layer errors
        val hiddenErrors = FloatArray(HIDDEN_SIZE)
        for (i in 0 until HIDDEN_SIZE) {
            var error = 0f
            for (j in 0 until OUTPUT_SIZE) {
                error += outputErrors[j] * hiddenToOutput[j][i]
            }
            hiddenErrors[i] = error * hidden[i] * (1 - hidden[i])
        }

        // Calculate gradients for hidden to output weights
        for (i in 0 until OUTPUT_SIZE) {
            for (j in 0 until HIDDEN_SIZE) {
                gradientsHiddenToOutput[i][j] += outputErrors[i] * hidden[j]
            }
        }

        // Calculate gradients for input to hidden weights
        for (i in 0 until HIDDEN_SIZE) {
            for (j in 0 until FEATURE_SIZE) {
                if (j < features.size) {
                    gradientsInputToHidden[i][j] += hiddenErrors[i] * features[j]
                }
            }
        }
    }

    /**
     * Split completions into support and query sets
     */
    private fun splitCompletions(
        completions: List<HabitCompletion>
    ): Pair<List<HabitCompletion>, List<HabitCompletion>> {
        val shuffled = completions.shuffled()
        val splitIndex = (completions.size * 0.7).toInt()

        val support = shuffled.subList(0, splitIndex)
        val query = shuffled.subList(splitIndex, shuffled.size)

        return Pair(support, query)
    }

    /**
     * Extract features from habit completions
     */
    private fun extractFeatures(
        habit: Habit,
        completions: List<HabitCompletion>
    ): List<FloatArray> {
        return completions.map { completion ->
            val features = FloatArray(FEATURE_SIZE)

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = completion.completionDate

            // Time of day (0-1)
            val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
            val minuteOfHour = calendar.get(Calendar.MINUTE)
            features[0] = (hourOfDay + minuteOfHour / 60.0f) / 24.0f

            // Day of week (0-1)
            features[1] = (calendar.get(Calendar.DAY_OF_WEEK) - 1) / 6.0f

            // Streak (normalized)
            features[2] = habit.streak.toFloat() / 30f

            // Goal progress (normalized)
            features[3] = habit.goalProgress.toFloat() / habit.goal.toFloat()

            // Frequency (encoded)
            features[4] = when (habit.frequency) {
                com.example.myapplication.data.model.HabitFrequency.DAILY -> 1f
                com.example.myapplication.data.model.HabitFrequency.WEEKLY -> 0.5f
                com.example.myapplication.data.model.HabitFrequency.MONTHLY -> 0.25f
                else -> 0f
            }

            // Category (encoded)
            features[5] = habit.category?.let {
                encodeCategory(it)
            } ?: 0.5f

            // Mood (normalized)
            features[6] = completion.mood?.toFloat()?.div(5f) ?: 0.5f

            // Difficulty (normalized)
            features[7] = habit.difficulty.ordinal.toFloat() / 5f

            // Days since creation (normalized)
            val daysSinceCreation = if (habit.createdDate != null) {
                (completion.completionDate - habit.createdDate.time) / (1000 * 60 * 60 * 24)
            } else {
                0L
            }
            features[8] = (daysSinceCreation / 90f).coerceIn(0f, 1f)

            // Time since last completion (normalized)
            features[9] = 0.5f // Placeholder

            features
        }
    }

    /**
     * Extract targets from habit completions
     */
    private fun extractTargets(
        habit: Habit,
        completions: List<HabitCompletion>
    ): List<FloatArray> {
        // Sort completions by date
        val sortedCompletions = completions.sortedBy { it.completionDate }

        return sortedCompletions.mapIndexed { index, completion ->
            val targets = FloatArray(OUTPUT_SIZE)

            // Target 1: Completion likelihood (always 1 for actual completions)
            targets[0] = 1f

            // Target 2: Streak continuation
            targets[1] = if (index < sortedCompletions.size - 1) {
                val nextCompletion = sortedCompletions[index + 1]
                val daysBetween = (nextCompletion.completionDate - completion.completionDate) / (1000 * 60 * 60 * 24)

                when (habit.frequency) {
                    com.example.myapplication.data.model.HabitFrequency.DAILY -> if (daysBetween <= 1) 1f else 0f
                    com.example.myapplication.data.model.HabitFrequency.WEEKLY -> if (daysBetween <= 7) 1f else 0f
                    com.example.myapplication.data.model.HabitFrequency.MONTHLY -> if (daysBetween <= 31) 1f else 0f
                    else -> 0f
                }
            } else {
                0.5f // Unknown for the last completion
            }

            // Target 3: Optimal time
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = completion.completionDate
            val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
            val minuteOfHour = calendar.get(Calendar.MINUTE)
            targets[2] = (hourOfDay + minuteOfHour / 60.0f) / 24.0f

            targets
        }
    }

    /**
     * Encode habit category
     */
    private fun encodeCategory(category: Any): Float {
        val categoryName = when (category) {
            is HabitCategory -> category.name
            is String -> category
            else -> ""
        }.toString().lowercase()

        return when {
            categoryName.contains("health") || categoryName.contains("fitness") || categoryName.contains("exercise") -> 0.1f
            categoryName.contains("productivity") || categoryName.contains("work") || categoryName.contains("study") -> 0.3f
            categoryName.contains("mindfulness") || categoryName.contains("meditation") || categoryName.contains("mental") -> 0.5f
            categoryName.contains("social") || categoryName.contains("relationship") -> 0.7f
            categoryName.contains("creativity") || categoryName.contains("hobby") -> 0.9f
            else -> 0.5f
        }
    }

    /**
     * Sigmoid activation function
     */
    private fun sigmoid(x: Float): Float {
        return (1.0f / (1.0f + exp(-x.toDouble()))).toFloat()
    }

    /**
     * Serialize model parameters to bytes
     */
    fun serializeParameters(
        inputToHidden: Array<FloatArray>,
        hiddenToOutput: Array<FloatArray>
    ): ByteArray {
        val buffer = ByteBuffer.allocate(
            4 + 4 + 4 + // Dimensions
                (HIDDEN_SIZE * FEATURE_SIZE * 4) + // Input to hidden weights
                (OUTPUT_SIZE * HIDDEN_SIZE * 4) // Hidden to output weights
        )
        buffer.order(ByteOrder.nativeOrder())

        // Write dimensions
        buffer.putInt(FEATURE_SIZE)
        buffer.putInt(HIDDEN_SIZE)
        buffer.putInt(OUTPUT_SIZE)

        // Write input to hidden weights
        for (i in 0 until HIDDEN_SIZE) {
            for (j in 0 until FEATURE_SIZE) {
                buffer.putFloat(inputToHidden[i][j])
            }
        }

        // Write hidden to output weights
        for (i in 0 until OUTPUT_SIZE) {
            for (j in 0 until HIDDEN_SIZE) {
                buffer.putFloat(hiddenToOutput[i][j])
            }
        }

        return buffer.array()
    }

    /**
     * Deserialize model parameters from bytes
     */
    fun deserializeParameters(bytes: ByteArray): Pair<Array<FloatArray>, Array<FloatArray>> {
        val buffer = ByteBuffer.wrap(bytes)
        buffer.order(ByteOrder.nativeOrder())

        // Read dimensions
        val featureSize = buffer.getInt()
        val hiddenSize = buffer.getInt()
        val outputSize = buffer.getInt()

        // Initialize arrays
        val inputToHidden = Array(hiddenSize) { FloatArray(featureSize) }
        val hiddenToOutput = Array(outputSize) { FloatArray(hiddenSize) }

        // Read input to hidden weights
        for (i in 0 until hiddenSize) {
            for (j in 0 until featureSize) {
                inputToHidden[i][j] = buffer.getFloat()
            }
        }

        // Read hidden to output weights
        for (i in 0 until outputSize) {
            for (j in 0 until hiddenSize) {
                hiddenToOutput[i][j] = buffer.getFloat()
            }
        }

        return Pair(inputToHidden, hiddenToOutput)
    }
}
