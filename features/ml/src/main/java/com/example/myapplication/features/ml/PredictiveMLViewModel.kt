package com.example.myapplication.features.ml

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
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

    private fun loadPredictions() {
        viewModelScope.launch {
            try {
                if (_modelAccuracy.value > 0.6f) {
                    _predictions.value = generateSamplePredictions()
                }
            } catch (e: Exception) {
                showToast("Error loading predictions: ${e.message}")
            }
        }
    }

    fun refreshPredictions() {
        loadPredictions()
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun startTraining() {
        if (_isTraining.value) return
        _isTraining.value = true
        _trainingProgress.value = 0f

        trainingJob = viewModelScope.launch {
            try {
                val steps = 100
                for (i in 1..steps) {
                    _trainingProgress.value = i.toFloat() / steps
                    delay(50)
                }
                val improvement = Random.nextFloat() * 0.1f
                _modelAccuracy.value = (_modelAccuracy.value + improvement).coerceAtMost(0.98f)
                _predictions.value = generateSamplePredictions()
                showToast("Model training completed")
            } catch (e: Exception) {
                showToast("Error during training: ${e.message}")
            } finally {
                _isTraining.value = false
            }
        }
    }

    fun cancelTraining() {
        trainingJob?.cancel()
        _isTraining.value = false
        showToast("Training cancelled")
    }

    fun resetModel() {
        viewModelScope.launch {
            _modelAccuracy.value = 0.5f
            _predictions.value = emptyList()
            showToast("Model reset")
        }
    }

    private suspend fun generateSamplePredictions(): List<HabitPrediction> {
        val habits = habitRepository.getAllHabits().first()
        if (habits.isEmpty()) return emptyList()

        val predictions = mutableListOf<HabitPrediction>()

        for (habit in habits) {
            predictions.add(
                HabitPrediction(
                    habitId = habit.id,
                    habitName = habit.name,
                    predictionType = PredictionType.COMPLETION_LIKELIHOOD,
                    probability = 0.6f + Random.nextFloat() * 0.3f,
                    timeframe = "Today",
                    confidenceInterval = Pair(0.65f, 0.85f),
                    factors = listOf(
                        PredictionFactorDetail("Time of Day", 0.4f, 0.8f),
                        PredictionFactorDetail("Mood", 0.2f, 0.7f)
                    )
                )
            )

            when (Random.nextInt(2)) {
                0 -> predictions.add(
                    HabitPrediction(
                        habitId = habit.id,
                        habitName = habit.name,
                        predictionType = PredictionType.STREAK_CONTINUATION,
                        probability = 0.5f + Random.nextFloat() * 0.4f,
                        timeframe = "Next 7 days",
                        confidenceInterval = Pair(0.55f, 0.75f),
                        factors = listOf(
                            PredictionFactorDetail("Previous Habit", 0.3f, 0.8f),
                            PredictionFactorDetail("Location", 0.1f, 0.6f)
                        )
                    )
                )
                1 -> predictions.add(
                    HabitPrediction(
                        habitId = habit.id,
                        habitName = habit.name,
                        predictionType = PredictionType.OPTIMAL_TIME,
                        probability = 0.7f + Random.nextFloat() * 0.25f,
                        timeframe = "Daily",
                        confidenceInterval = Pair(0.75f, 0.9f),
                        factors = listOf(
                            PredictionFactorDetail("Time Patterns", 0.5f, 0.9f)
                        )
                    )
                )
            }
        }

        return predictions.shuffled()
    }

    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
