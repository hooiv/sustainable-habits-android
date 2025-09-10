package com.example.myapplication.core.network.ml

import android.util.Log
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.model.HabitCompletion
import com.example.myapplication.core.data.model.ActionType
import com.example.myapplication.core.data.model.ReinforcementAction
import com.example.myapplication.core.data.model.ReinforcementState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max

/**
 * Reinforcement Learning agent for habit optimization
 * Implements Q-learning algorithm without requiring any cloud services
 */
@Singleton
class ReinforcementLearningAgent @Inject constructor() {
    companion object {
        private const val TAG = "RLAgent"
        private const val LEARNING_RATE = 0.1f
        private const val DISCOUNT_FACTOR = 0.9f
        private const val EXPLORATION_RATE_INITIAL = 0.9f
        private const val EXPLORATION_RATE_MIN = 0.1f
        private const val EXPLORATION_DECAY = 0.995f

        // State dimensions
        private const val TIME_BUCKETS = 8 // 3-hour buckets
        private const val DAY_BUCKETS = 7 // Days of week
        private const val STREAK_BUCKETS = 5 // Streak levels
        private const val CONTEXT_BUCKETS = 3 // Context levels (low, medium, high)

        // Action dimensions
        private const val NUM_ACTIONS = 5 // Number of possible actions
    }

    // Q-table: maps state-action pairs to expected rewards
    private val qTable = HashMap<Pair<ReinforcementState, ReinforcementAction>, Float>()

    // Current exploration rate
    private var explorationRate = EXPLORATION_RATE_INITIAL

    // Episode counter
    private var episodes = 0

    // Current state
    private var currentState: ReinforcementState? = null

    // Recommended action
    private val _recommendedAction = MutableStateFlow<ReinforcementAction?>(null)
    val recommendedAction: StateFlow<ReinforcementAction?> = _recommendedAction.asStateFlow()

    /**
     * Initialize the agent for a specific habit
     */
    fun initialize(habit: Habit, completions: List<HabitCompletion>) {
        // Reset Q-table if it's for a different habit
        if (currentState?.habitId != habit.id) {
            qTable.clear()
            explorationRate = EXPLORATION_RATE_INITIAL
            episodes = 0
        }

        // Learn from historical completions
        learnFromHistory(habit, completions)

        Log.d(TAG, "Initialized RL agent for habit: ${habit.name}, Q-table size: ${qTable.size}")
    }

    /**
     * Learn from historical habit completions
     */
    private fun learnFromHistory(habit: Habit, completions: List<HabitCompletion>) {
        if (completions.isEmpty()) {
            return
        }

        // Sort completions by date
        val sortedCompletions = completions.sortedBy { it.completionDate }

        // For each completion, simulate a reinforcement learning episode
        for (i in 0 until sortedCompletions.size - 1) {
            val completion = sortedCompletions[i]
            val nextCompletion = sortedCompletions[i + 1]

            // Create state from completion
            val state = createStateFromCompletion(habit, completion)

            // Infer action from the time difference between completions
            val action = inferActionFromCompletions(completion, nextCompletion)

            // Calculate reward based on streak maintenance and time consistency
            val reward = calculateReward(habit, completion, nextCompletion)

            // Create next state from next completion
            val nextState = createStateFromCompletion(habit, nextCompletion)

            // Update Q-table
            updateQValue(state, action, reward, nextState)

            episodes++
        }

        // Update exploration rate based on number of episodes
        updateExplorationRate()
    }

    /**
     * Create a state representation from a habit completion
     */
    private fun createStateFromCompletion(habit: Habit, completion: HabitCompletion): ReinforcementState {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = completion.completionDate

        // Extract time bucket (0-7, representing 3-hour blocks)
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val timeBucket = hourOfDay / 3

        // Extract day bucket (0-6, representing days of week)
        val dayBucket = (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY) % 7

        // Extract streak bucket (0-4, representing streak levels)
        val streakBucket = when {
            habit.streak <= 0 -> 0
            habit.streak <= 3 -> 1
            habit.streak <= 7 -> 2
            habit.streak <= 14 -> 3
            else -> 4
        }

        // Extract context bucket (simplified)
        val contextBucket = completion.mood?.div(2) ?: 1

        return ReinforcementState(
            habitId = habit.id,
            timeBucket = timeBucket,
            dayBucket = dayBucket,
            streakBucket = streakBucket,
            contextBucket = contextBucket
        )
    }

    /**
     * Infer the action taken between two completions
     */
    private fun inferActionFromCompletions(
        completion: HabitCompletion,
        nextCompletion: HabitCompletion
    ): ReinforcementAction {
        // Calculate time difference in hours
        val timeDiff = (nextCompletion.completionDate - completion.completionDate) / (1000 * 60 * 60)

        // Infer action based on time difference
        val actionId = when {
            timeDiff <= 12 -> 0 // Complete soon
            timeDiff <= 24 -> 1 // Complete same day
            timeDiff <= 48 -> 2 // Complete next day
            timeDiff <= 72 -> 3 // Complete within 3 days
            else -> 4 // Complete later
        }

        return ReinforcementAction(
            id = UUID.randomUUID().toString(),
            habitId = "",  // This will be set later
            actionType = ActionType.values()[actionId % ActionType.values().size],
            parameters = emptyMap(),
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * Calculate reward for a state-action-next_state transition
     */
    private fun calculateReward(
        habit: Habit,
        completion: HabitCompletion,
        nextCompletion: HabitCompletion
    ): Float {
        var reward = 0f

        // Calculate time difference in days
        val timeDiffDays = (nextCompletion.completionDate - completion.completionDate) / (1000 * 60 * 60 * 24)

        // Reward for maintaining streak
        reward += when (habit.frequency) {
            com.example.myapplication.core.data.model.HabitFrequency.DAILY -> {
                if (timeDiffDays <= 1) 10f else -5f
            }
            com.example.myapplication.core.data.model.HabitFrequency.WEEKLY -> {
                if (timeDiffDays <= 7) 10f else -5f
            }
            com.example.myapplication.core.data.model.HabitFrequency.MONTHLY -> {
                if (timeDiffDays <= 30) 10f else -5f
            }
            else -> 0f
        }

        // Reward for consistency (completing at similar times)
        val calendar1 = Calendar.getInstance()
        calendar1.timeInMillis = completion.completionDate
        val hourOfDay1 = calendar1.get(Calendar.HOUR_OF_DAY)

        val calendar2 = Calendar.getInstance()
        calendar2.timeInMillis = nextCompletion.completionDate
        val hourOfDay2 = calendar2.get(Calendar.HOUR_OF_DAY)

        val hourDiff = Math.abs(hourOfDay1 - hourOfDay2)
        reward += if (hourDiff <= 2) 5f else 0f

        // Reward for mood improvement
        val moodDiff = (nextCompletion.mood ?: 3) - (completion.mood ?: 3)
        reward += moodDiff

        return reward
    }

    /**
     * Update Q-value for a state-action pair
     */
    private fun updateQValue(
        state: ReinforcementState,
        action: ReinforcementAction,
        reward: Float,
        nextState: ReinforcementState
    ) {
        // Get current Q-value
        val currentQValue = qTable.getOrDefault(Pair(state, action), 0f)

        // Find maximum Q-value for next state
        val maxNextQValue = getMaxQValue(nextState)

        // Update Q-value using Q-learning formula
        val newQValue = currentQValue + LEARNING_RATE * (reward + DISCOUNT_FACTOR * maxNextQValue - currentQValue)

        // Store updated Q-value
        qTable[Pair(state, action)] = newQValue
    }

    /**
     * Get maximum Q-value for a state across all actions
     */
    private fun getMaxQValue(state: ReinforcementState): Float {
        var maxQValue = 0f

        for (actionId in 0 until NUM_ACTIONS) {
            val action = ReinforcementAction(
                id = UUID.randomUUID().toString(),
                habitId = "",
                actionType = ActionType.values()[actionId % ActionType.values().size],
                parameters = emptyMap(),
                timestamp = System.currentTimeMillis()
            )
            val qValue = qTable.getOrDefault(Pair(state, action), 0f)
            maxQValue = max(maxQValue, qValue)
        }

        return maxQValue
    }

    /**
     * Update exploration rate
     */
    private fun updateExplorationRate() {
        explorationRate = max(
            EXPLORATION_RATE_MIN,
            EXPLORATION_RATE_INITIAL * exp(-EXPLORATION_DECAY * episodes)
        )
    }

    /**
     * Get the best action for a given state
     */
    fun getBestAction(state: ReinforcementState): ReinforcementAction {
        // With probability explorationRate, choose a random action (exploration)
        if (Random().nextFloat() < explorationRate) {
            val randomActionId = Random().nextInt(NUM_ACTIONS)
            return ReinforcementAction(
                id = UUID.randomUUID().toString(),
                habitId = "",
                actionType = ActionType.values()[randomActionId % ActionType.values().size],
                parameters = emptyMap(),
                timestamp = System.currentTimeMillis()
            )
        }

        // Otherwise, choose the action with the highest Q-value (exploitation)
        var bestAction = ReinforcementAction(
            id = UUID.randomUUID().toString(),
            habitId = "",
            actionType = ActionType.values()[0],
            parameters = emptyMap(),
            timestamp = System.currentTimeMillis()
        )
        var bestQValue = Float.NEGATIVE_INFINITY

        for (actionId in 0 until NUM_ACTIONS) {
            val action = ReinforcementAction(
                id = UUID.randomUUID().toString(),
                habitId = "",
                actionType = ActionType.values()[actionId % ActionType.values().size],
                parameters = emptyMap(),
                timestamp = System.currentTimeMillis()
            )
            val qValue = qTable.getOrDefault(Pair(state, action), 0f)

            if (qValue > bestQValue) {
                bestQValue = qValue
                bestAction = action
            }
        }

        return bestAction
    }

    /**
     * Update current state and get recommended action
     */
    fun updateState(habit: Habit, contextFeatures: FloatArray) {
        // Create state from current context
        val calendar = Calendar.getInstance()

        // Extract time bucket (0-7, representing 3-hour blocks)
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val timeBucket = hourOfDay / 3

        // Extract day bucket (0-6, representing days of week)
        val dayBucket = (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY) % 7

        // Extract streak bucket (0-4, representing streak levels)
        val streakBucket = when {
            habit.streak <= 0 -> 0
            habit.streak <= 3 -> 1
            habit.streak <= 7 -> 2
            habit.streak <= 14 -> 3
            else -> 4
        }

        // Extract context bucket from context features
        val activityLevel = contextFeatures[ContextFeatureCollector.FEATURE_ACTIVITY_LEVEL]
        val contextBucket = when {
            activityLevel < 0.3f -> 0 // Low activity
            activityLevel < 0.7f -> 1 // Medium activity
            else -> 2 // High activity
        }

        // Create state
        val state = ReinforcementState(
            habitId = habit.id,
            timeBucket = timeBucket,
            dayBucket = dayBucket,
            streakBucket = streakBucket,
            contextBucket = contextBucket
        )

        currentState = state

        // Get best action for current state
        val action = getBestAction(state)
        _recommendedAction.value = action

        Log.d(TAG, "Updated state: $state, recommended action: $action")
    }

    /**
     * Provide feedback for the last action
     */
    fun provideFeedback(reward: Float) {
        val state = currentState ?: return
        val action = _recommendedAction.value ?: return

        // Update Q-value with the provided reward
        val currentQValue = qTable.getOrDefault(Pair(state, action), 0f)
        val newQValue = currentQValue + LEARNING_RATE * (reward - currentQValue)
        qTable[Pair(state, action)] = newQValue

        Log.d(TAG, "Updated Q-value for state: $state, action: $action, reward: $reward, new Q-value: $newQValue")
    }

    /**
     * Get action description
     */
    fun getActionDescription(action: ReinforcementAction): String {
        return when (action.actionType) {
            ActionType.SEND_NOTIFICATION -> "Complete habit within the next few hours"
            ActionType.ADJUST_DIFFICULTY -> "Complete habit today"
            ActionType.SUGGEST_PAIRING -> "Complete habit tomorrow"
            ActionType.PROVIDE_ENCOURAGEMENT -> "Complete habit within the next few days"
            ActionType.SUGGEST_ENVIRONMENT_CHANGE -> "Schedule habit for later this week"
            ActionType.SUGGEST_TIME_CHANGE -> "Try a different time for your habit"
            ActionType.SUGGEST_SOCIAL_SUPPORT -> "Get support from friends for your habit"
        }
    }

    /**
     * Get Q-table size
     */
    fun getQTableSize(): Int {
        return qTable.size
    }
}
