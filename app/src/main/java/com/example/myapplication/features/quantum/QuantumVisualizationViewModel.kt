package com.example.myapplication.features.quantum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.model.HabitCompletion
import com.example.myapplication.core.network.quantum.QuantumEntanglement
import com.example.myapplication.core.network.quantum.QuantumParticle
import com.example.myapplication.core.network.quantum.QuantumVisualizer
import com.example.myapplication.core.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Quantum Visualization screen
 */
@HiltViewModel
class QuantumVisualizationViewModel @Inject constructor(
    private val quantumVisualizer: QuantumVisualizer,
    private val habitRepository: HabitRepository
) : ViewModel() {

    // Quantum state
    val particles: StateFlow<List<QuantumParticle>> = quantumVisualizer.particles
    val entanglements: StateFlow<List<QuantumEntanglement>> = quantumVisualizer.entanglements
    val energyLevels: StateFlow<Map<String, Int>> = quantumVisualizer.energyLevels

    // Habits
    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    // Current habit ID
    private val _currentHabitId = MutableStateFlow<String?>(null)
    val currentHabitId: StateFlow<String?> = _currentHabitId.asStateFlow()

    // Habit completions
    private val _completions = MutableStateFlow<Map<String, List<HabitCompletion>>>(emptyMap())

    // Optimal schedule
    private val _optimalSchedule = MutableStateFlow<List<Pair<Habit, Double>>>(emptyList())
    val optimalSchedule: StateFlow<List<Pair<Habit, Double>>> = _optimalSchedule.asStateFlow()

    // Simulation state
    private val _isSimulationRunning = MutableStateFlow(false)
    val isSimulationRunning: StateFlow<Boolean> = _isSimulationRunning.asStateFlow()

    // Error message
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    // Simulation job
    private var simulationJob: Job? = null

    init {
        // Load habits and completions
        loadHabitsAndCompletions()
    }

    /**
     * Load habits and completions
     */
    private fun loadHabitsAndCompletions() {
        viewModelScope.launch {
            try {
                // Load habits
                habitRepository.getAllHabits()
                    .collect { habits ->
                        _habits.value = habits

                        // Load completions for each habit
                        val completions = mutableMapOf<String, List<HabitCompletion>>()
                        habits.forEach { habit ->
                            habitRepository.getHabitCompletions(habit.id)
                                .collect { habitCompletions ->
                                    completions[habit.id] = habitCompletions
                                }
                        }
                        _completions.value = completions

                        // Initialize quantum state
                        initializeQuantumState()
                    }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load habits: ${e.message}"
            }
        }
    }

    /**
     * Set current habit ID
     */
    fun setCurrentHabitId(habitId: String) {
        _currentHabitId.value = habitId
    }

    /**
     * Select a particle
     */
    fun selectParticle(particleId: String) {
        // Implementation would depend on the QuantumVisualizer API
        // This is a placeholder
        viewModelScope.launch {
            try {
                // Example implementation
                val particles = this@QuantumVisualizationViewModel.particles.value
                val selectedParticle = particles.find { it.id == particleId }

                selectedParticle?.let { particle ->
                    // Do something with the selected particle
                    _errorMessage.value = "Selected particle: ${particle.id}"

                    // Clear message after a delay
                    delay(2000)
                    _errorMessage.value = ""
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to select particle: ${e.message}"
            }
        }
    }

    /**
     * Initialize quantum state
     */
    fun initializeQuantumState() {
        viewModelScope.launch {
            try {
                val habits = _habits.value
                val completions = _completions.value

                if (habits.isNotEmpty()) {
                    quantumVisualizer.initializeQuantumState(habits, completions)

                    // Get optimal schedule
                    updateOptimalSchedule()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to initialize quantum state: ${e.message}"
            }
        }
    }

    /**
     * Start quantum simulation
     */
    fun startSimulation() {
        if (_isSimulationRunning.value) return

        _isSimulationRunning.value = true

        simulationJob = viewModelScope.launch {
            try {
                while (_isSimulationRunning.value) {
                    quantumVisualizer.updateSimulation()
                    delay(16) // ~60fps
                }
            } catch (e: Exception) {
                _errorMessage.value = "Simulation error: ${e.message}"
                _isSimulationRunning.value = false
            }
        }
    }

    /**
     * Stop quantum simulation
     */
    fun stopSimulation() {
        _isSimulationRunning.value = false
        simulationJob?.cancel()
        simulationJob = null
    }

    /**
     * Reset quantum simulation
     */
    fun resetSimulation() {
        viewModelScope.launch {
            try {
                // Stop simulation
                stopSimulation()

                // Reinitialize quantum state
                initializeQuantumState()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to reset simulation: ${e.message}"
            }
        }
    }

    /**
     * Apply quantum effect to habits
     */
    fun applyQuantumEffect() {
        viewModelScope.launch {
            try {
                val habits = _habits.value
                val completions = _completions.value

                // Apply quantum effect
                val results = quantumVisualizer.applyQuantumEffect(habits, completions)

                // Update optimal schedule
                updateOptimalSchedule()

                // Show success message
                _errorMessage.value = "Quantum effect applied successfully"

                // Clear message after a delay
                delay(2000)
                _errorMessage.value = ""
            } catch (e: Exception) {
                _errorMessage.value = "Failed to apply quantum effect: ${e.message}"
            }
        }
    }

    /**
     * Update optimal habit schedule
     */
    private fun updateOptimalSchedule() {
        viewModelScope.launch {
            try {
                val habits = _habits.value

                // Get optimal schedule
                val schedule = quantumVisualizer.getOptimalHabitSchedule(habits)
                _optimalSchedule.value = schedule
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update optimal schedule: ${e.message}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopSimulation()
    }
}
