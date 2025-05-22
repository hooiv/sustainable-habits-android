package com.example.myapplication.ui.quantum

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Habit
import com.example.myapplication.data.model.HabitCompletion
import com.example.myapplication.data.quantum.QuantumVisualization
import com.example.myapplication.data.quantum.QuantumVisualizer
import com.example.myapplication.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for quantum visualization
 */
@HiltViewModel
class QuantumVisualizationViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val quantumVisualizer: QuantumVisualizer
) : ViewModel() {
    private val TAG = "QuantumViewModel"

    // Quantum visualization
    private val _quantumVisualization = MutableStateFlow<QuantumVisualization?>(null)
    val quantumVisualization: StateFlow<QuantumVisualization?> = _quantumVisualization.asStateFlow()

    // Habit success probabilities
    private val _habitSuccessProbabilities = MutableStateFlow<Map<String, Double>>(emptyMap())
    val habitSuccessProbabilities: StateFlow<Map<String, Double>> = _habitSuccessProbabilities.asStateFlow()

    // Optimal habit schedule
    private val _optimalHabitSchedule = MutableStateFlow<List<Pair<Habit, Double>>>(emptyList())
    val optimalHabitSchedule: StateFlow<List<Pair<Habit, Double>>> = _optimalHabitSchedule.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        initializeQuantumState()
    }

    /**
     * Initialize quantum state
     */
    private fun initializeQuantumState() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get all habits
                val habits = habitRepository.getAllHabits().first()

                // Get all completions
                val completions = mutableMapOf<String, List<HabitCompletion>>()
                habits.forEach { habit ->
                    val habitCompletions = habitRepository.getHabitCompletions(habit.id).first()
                    completions[habit.id] = habitCompletions
                }

                // Initialize quantum state
                quantumVisualizer.initializeQuantumState(habits, completions)

                // Get quantum visualization
                val visualization = quantumVisualizer.createQuantumVisualization()
                _quantumVisualization.value = visualization

                // Apply quantum effect
                val probabilities = quantumVisualizer.applyQuantumEffect(habits, completions)
                _habitSuccessProbabilities.value = probabilities

                // Get optimal habit schedule
                val schedule = quantumVisualizer.getOptimalHabitSchedule(habits)
                _optimalHabitSchedule.value = schedule

                Log.d(TAG, "Quantum state initialized with ${habits.size} habits")
            } catch (e: Exception) {
                _errorMessage.value = "Failed to initialize quantum state: ${e.message}"
                Log.e(TAG, "Error initializing quantum state", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update quantum simulation
     */
    fun updateQuantumSimulation() {
        try {
            // Update quantum simulation
            quantumVisualizer.updateSimulation()

            // Update quantum visualization
            quantumVisualizer.updateQuantumVisualization()

            // Get updated visualization
            val visualization = quantumVisualizer.createQuantumVisualization()
            _quantumVisualization.value = visualization
        } catch (e: Exception) {
            Log.e(TAG, "Error updating quantum simulation", e)
        }
    }

    /**
     * Reset visualization
     */
    fun resetVisualization() {
        viewModelScope.launch {
            initializeQuantumState()
        }
    }

    /**
     * Get quantum visualization for a habit
     */
    fun getQuantumVisualizationForHabit(habitId: String) {
        viewModelScope.launch {
            try {
                val visualization = quantumVisualizer.getQuantumVisualizationForHabit(habitId)
                if (visualization != null) {
                    _quantumVisualization.value = visualization
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting quantum visualization for habit", e)
            }
        }
    }

    /**
     * Predict habit success probability
     */
    fun predictHabitSuccess(habitId: String) {
        viewModelScope.launch {
            try {
                // Get habit
                val habits = habitRepository.getAllHabits().first()
                val habit = habits.find { it.id == habitId }
                if (habit != null) {
                    // Get completions
                    val completions = habitRepository.getHabitCompletions(habitId).first()

                    // Predict success probability
                    val probability = quantumVisualizer.predictHabitSuccess(habit, completions)

                    // Update probabilities
                    val updatedProbabilities = _habitSuccessProbabilities.value.toMutableMap()
                    updatedProbabilities[habitId] = probability
                    _habitSuccessProbabilities.value = updatedProbabilities
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error predicting habit success", e)
            }
        }
    }
}
