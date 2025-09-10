package com.example.myapplication.features.biometric

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * ViewModel for biometric visualization
 */
@HiltViewModel
class BiometricVisualizationViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Habit ID
    private val habitId: String? = savedStateHandle["habitId"]

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Monitoring state
    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    // Biometric data
    private val _heartRate = MutableStateFlow(0)
    val heartRate: StateFlow<Int> = _heartRate.asStateFlow()

    private val _respirationRate = MutableStateFlow(0)
    val respirationRate: StateFlow<Int> = _respirationRate.asStateFlow()

    private val _stressLevel = MutableStateFlow(0f)
    val stressLevel: StateFlow<Float> = _stressLevel.asStateFlow()

    private val _energyLevel = MutableStateFlow(0f)
    val energyLevel: StateFlow<Float> = _energyLevel.asStateFlow()

    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Habit name
    private val _habitName = MutableStateFlow("")
    val habitName: StateFlow<String> = _habitName.asStateFlow()

    init {
        loadHabitData()
    }

    /**
     * Load habit data
     */
    private fun loadHabitData() {
        viewModelScope.launch {
            if (habitId != null) {
                try {
                    val habit = habitRepository.getHabitById(habitId).first()
                    if (habit != null) {
                        _habitName.value = habit.name
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Error loading habit: ${e.message}"
                }
            }
        }
    }

    /**
     * Start biometric monitoring
     */
    fun startMonitoring() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Simulate initializing sensors
                delay(1500)

                // Start monitoring
                _isMonitoring.value = true

                // Initial values
                _heartRate.value = 75
                _respirationRate.value = 16
                _stressLevel.value = 0.3f
                _energyLevel.value = 0.7f

                // Start updating biometric data
                monitorBiometricData()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to start monitoring: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Monitor biometric data
     */
    private fun monitorBiometricData() {
        viewModelScope.launch {
            while (_isMonitoring.value) {
                // Simulate biometric data changes
                updateBiometricData()

                // Update every second
                delay(1000)
            }
        }
    }

    /**
     * Update biometric data
     */
    private fun updateBiometricData() {
        // Simulate heart rate changes
        val heartRateChange = Random.nextInt(-2, 3)
        _heartRate.value = max(60, min(100, _heartRate.value + heartRateChange))

        // Simulate respiration rate changes
        if (Random.nextFloat() < 0.2f) {
            val respirationRateChange = Random.nextInt(-1, 2)
            _respirationRate.value = max(12, min(20, _respirationRate.value + respirationRateChange))
        }

        // Simulate stress level changes
        val stressLevelChange = (Random.nextFloat() - 0.5f) * 0.05f
        _stressLevel.value = max(0f, min(1f, _stressLevel.value + stressLevelChange))

        // Simulate energy level changes
        val energyLevelChange = (Random.nextFloat() - 0.5f) * 0.03f
        _energyLevel.value = max(0f, min(1f, _energyLevel.value + energyLevelChange))

        // If habit is provided, adjust biometrics based on habit data
        habitId?.let { id ->
            viewModelScope.launch {
                try {
                    val habit = habitRepository.getHabitById(id).first()
                    val completions = habitRepository.getHabitCompletions(id).first()

                    // Adjust stress level based on streak
                    if (habit != null && habit.streak > 5) {
                        _stressLevel.value = max(0f, _stressLevel.value - 0.01f)
                    }

                    // Adjust energy level based on recent completions
                    if (completions.isNotEmpty()) {
                        // Find the most recent completion
                        var latestTimestamp = 0L
                        for (completion in completions) {
                            if (completion.completionDate > latestTimestamp) {
                                latestTimestamp = completion.completionDate
                            }
                        }

                        // Check if it's recent (within 24 hours)
                        if (latestTimestamp > 0 &&
                            System.currentTimeMillis() - latestTimestamp < 24 * 60 * 60 * 1000) {
                            _energyLevel.value = min(1f, _energyLevel.value + 0.01f)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore errors during updates
                }
            }
        }
    }

    /**
     * Stop biometric monitoring
     */
    fun stopMonitoring() {
        _isMonitoring.value = false
    }

    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Clean up resources
     */
    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}
