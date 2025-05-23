package com.example.myapplication.features.ml

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ml.HabitPrediction
import com.example.myapplication.data.ml.PredictionFactor
import com.example.myapplication.data.ml.PredictionType
import com.example.myapplication.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

/**
 * ViewModel for the Predictive ML screen
 */
@HiltViewModel
class PredictiveMLViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    // State
    private val _predictions = MutableStateFlow<List<HabitPrediction>>(emptyList())
    val predictions: StateFlow<List<HabitPrediction>> = _predictions.asStateFlow()
    
    private val _isTraining = MutableStateFlow(false)
    val isTraining: StateFlow<Boolean> = _isTraining.asStateFlow()
    
    private val _trainingProgress = MutableStateFlow(0f)
    val trainingProgress: StateFlow<Float> = _trainingProgress.asStateFlow()
    
    private val _modelAccuracy = MutableStateFlow(0.65f)
    val modelAccuracy: StateFlow<Float> = _modelAccuracy.asStateFlow()
    
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()
    
    private var trainingJob: Job? = null
    
    init {
        loadPredictions()
    }
    
    /**
     * Load predictions
     */
    private fun loadPredictions() {
        viewModelScope.launch {
            try {
                // In a real app, this would load from a repository
                // For now, we'll generate sample predictions if the model accuracy is high enough
                if (_modelAccuracy.value > 0.7f) {
                    _predictions.value = generateSamplePredictions()
                }
            } catch (e: Exception) {
                showToast("Error loading predictions: ${e.message}")
            }
        }
    }
    
    /**
     * Refresh predictions
     */
    fun refreshPredictions() {
        loadPredictions()
    }
    
    /**
     * Set selected tab
     */
    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }
    
    /**
     * Start model training
     */
    fun startTraining() {
        if (_isTraining.value) return
        
        _isTraining.value = true
        _trainingProgress.value = 0f
        
        trainingJob = viewModelScope.launch {
            try {
                // Simulate training process
                val steps = 100
                for (i in 1..steps) {
                    _trainingProgress.value = i.toFloat() / steps
                    delay(50) // Simulate work
                }
                
                // Update model accuracy
                val previousAccuracy = _modelAccuracy.value
                val improvement = Random.nextFloat() * 0.1f
                _modelAccuracy.value = (previousAccuracy + improvement).coerceAtMost(0.98f)
                
                // Generate new predictions
                _predictions.value = generateSamplePredictions()
                
                showToast("Model training completed")
            } catch (e: Exception) {
                showToast("Error during training: ${e.message}")
            } finally {
                _isTraining.value = false
            }
        }
    }
    
    /**
     * Cancel model training
     */
    fun cancelTraining() {
        trainingJob?.cancel()
        _isTraining.value = false
        showToast("Training cancelled")
    }
    
    /**
     * Reset model
     */
    fun resetModel() {
        viewModelScope.launch {
            _modelAccuracy.value = 0.5f
            _predictions.value = emptyList()
            showToast("Model reset")
        }
    }
    
    /**
     * Generate sample predictions
     */
    private suspend fun generateSamplePredictions(): List<HabitPrediction> {
        val habits = habitRepository.getAllHabits().value
        if (habits.isEmpty()) return emptyList()
        
        val predictions = mutableListOf<HabitPrediction>()
        
        // Generate predictions for each habit
        for (habit in habits) {
            // Completion likelihood prediction
            predictions.add(
                HabitPrediction(
                    id = UUID.randomUUID().toString(),
                    habitId = habit.id,
                    habitName = habit.name,
                    predictionType = PredictionType.COMPLETION_LIKELIHOOD,
                    probability = 0.6f + Random.nextFloat() * 0.3f,
                    description = "You're likely to complete this habit today",
                    timeframe = "Today",
                    confidenceInterval = Pair(0.65f, 0.85f),
                    factors = listOf(
                        PredictionFactor.TIME_OF_DAY,
                        PredictionFactor.DAY_OF_WEEK,
                        PredictionFactor.MOOD
                    ),
                    timestamp = System.currentTimeMillis()
                )
            )
            
            // Streak continuation prediction
            predictions.add(
                HabitPrediction(
                    id = UUID.randomUUID().toString(),
                    habitId = habit.id,
                    habitName = habit.name,
                    predictionType = PredictionType.STREAK_CONTINUATION,
                    probability = 0.5f + Random.nextFloat() * 0.4f,
                    description = "Your current streak is likely to continue for another week",
                    timeframe = "Next 7 days",
                    confidenceInterval = Pair(0.55f, 0.75f),
                    factors = listOf(
                        PredictionFactor.PREVIOUS_HABIT,
                        PredictionFactor.MOOD,
                        PredictionFactor.LOCATION
                    ),
                    timestamp = System.currentTimeMillis()
                )
            )
            
            // Optimal time prediction
            predictions.add(
                HabitPrediction(
                    id = UUID.randomUUID().toString(),
                    habitId = habit.id,
                    habitName = habit.name,
                    predictionType = PredictionType.OPTIMAL_TIME,
                    probability = 0.7f + Random.nextFloat() * 0.25f,
                    description = "Your optimal time for this habit is in the morning",
                    timeframe = "Daily",
                    confidenceInterval = Pair(0.75f, 0.9f),
                    factors = listOf(
                        PredictionFactor.TIME_OF_DAY,
                        PredictionFactor.MOOD,
                        PredictionFactor.PREVIOUS_HABIT
                    ),
                    timestamp = System.currentTimeMillis()
                )
            )
            
            // Only add one more prediction type per habit to avoid too many
            val randomType = when (Random.nextInt(3)) {
                0 -> PredictionType.HABIT_FORMATION
                1 -> PredictionType.HABIT_ABANDONMENT
                else -> PredictionType.DIFFICULTY_CHANGE
            }
            
            when (randomType) {
                PredictionType.HABIT_FORMATION -> {
                    predictions.add(
                        HabitPrediction(
                            id = UUID.randomUUID().toString(),
                            habitId = habit.id,
                            habitName = habit.name,
                            predictionType = PredictionType.HABIT_FORMATION,
                            probability = 0.4f + Random.nextFloat() * 0.4f,
                            description = "This habit is likely to become automatic in about 3 weeks",
                            timeframe = "3 weeks",
                            confidenceInterval = Pair(0.5f, 0.7f),
                            factors = listOf(
                                PredictionFactor.PREVIOUS_HABIT,
                                PredictionFactor.SOCIAL_CONTEXT
                            ),
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
                PredictionType.HABIT_ABANDONMENT -> {
                    predictions.add(
                        HabitPrediction(
                            id = UUID.randomUUID().toString(),
                            habitId = habit.id,
                            habitName = habit.name,
                            predictionType = PredictionType.HABIT_ABANDONMENT,
                            probability = 0.2f + Random.nextFloat() * 0.3f,
                            description = "There's a risk of abandoning this habit if you miss more than 3 days in a row",
                            timeframe = "Next month",
                            confidenceInterval = Pair(0.25f, 0.45f),
                            factors = listOf(
                                PredictionFactor.PREVIOUS_HABIT,
                                PredictionFactor.MOOD,
                                PredictionFactor.SOCIAL_CONTEXT
                            ),
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
                PredictionType.DIFFICULTY_CHANGE -> {
                    predictions.add(
                        HabitPrediction(
                            id = UUID.randomUUID().toString(),
                            habitId = habit.id,
                            habitName = habit.name,
                            predictionType = PredictionType.DIFFICULTY_CHANGE,
                            probability = 0.6f + Random.nextFloat() * 0.3f,
                            description = "This habit will become easier to maintain over time",
                            timeframe = "Next 2 months",
                            confidenceInterval = Pair(0.65f, 0.85f),
                            factors = listOf(
                                PredictionFactor.PREVIOUS_HABIT,
                                PredictionFactor.TIME_OF_DAY
                            ),
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
                else -> {}
            }
        }
        
        return predictions.shuffled()
    }
    
    /**
     * Show a toast message
     */
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
