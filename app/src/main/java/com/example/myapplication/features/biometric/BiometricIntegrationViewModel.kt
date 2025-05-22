package com.example.myapplication.features.biometric

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.biometric.BiometricIntegration
import com.example.myapplication.data.model.BiometricReading
import com.example.myapplication.data.model.BiometricType
import com.example.myapplication.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

/**
 * ViewModel for the Biometric Integration screen
 */
@HiltViewModel
class BiometricIntegrationViewModel @Inject constructor(
    private val biometricIntegration: BiometricIntegration,
    private val habitRepository: HabitRepository
) : ViewModel() {

    // Current habit ID
    private val _currentHabitId = MutableStateFlow<String?>(null)
    val currentHabitId: StateFlow<String?> = _currentHabitId.asStateFlow()

    // Biometric readings
    private val _biometricReadings = MutableStateFlow<List<BiometricReading>>(emptyList())
    val biometricReadings: StateFlow<List<BiometricReading>> = _biometricReadings.asStateFlow()

    // Heart rate
    private val _heartRate = MutableStateFlow(70)
    val heartRate: StateFlow<Int> = _heartRate.asStateFlow()

    // Blood pressure
    private val _bloodPressure = MutableStateFlow(Pair(120, 80))
    val bloodPressure: StateFlow<Pair<Int, Int>> = _bloodPressure.asStateFlow()

    // Sleep data
    private val _sleepData = MutableStateFlow<Map<String, Float>>(
        mapOf(
            "Deep" to 1.5f,
            "Light" to 4.0f,
            "REM" to 1.2f,
            "Awake" to 0.3f
        )
    )
    val sleepData: StateFlow<Map<String, Float>> = _sleepData.asStateFlow()

    // Stress level
    private val _stressLevel = MutableStateFlow(3)
    val stressLevel: StateFlow<Int> = _stressLevel.asStateFlow()

    // Measurement state
    private val _isMeasuring = MutableStateFlow(false)
    val isMeasuring: StateFlow<Boolean> = _isMeasuring.asStateFlow()

    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Load biometric data
        loadBiometricData()
    }

    /**
     * Set current habit ID
     */
    fun setCurrentHabitId(habitId: String) {
        _currentHabitId.value = habitId
    }

    /**
     * Load biometric data
     */
    fun loadBiometricData() {
        viewModelScope.launch {
            try {
                // Get biometric readings
                val readings = biometricIntegration.getBiometricReadings()
                _biometricReadings.value = readings.sortedByDescending { it.timestamp }

                // Update current values
                updateCurrentValues(readings)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load biometric data: ${e.message}"
            }
        }
    }

    /**
     * Update current values from readings
     */
    private fun updateCurrentValues(readings: List<BiometricReading>) {
        // Update heart rate
        readings.find { it.type == BiometricType.HEART_RATE }?.let {
            _heartRate.value = it.value.toInt()
        }

        // Update blood pressure
        readings.find { it.type == BiometricType.BLOOD_PRESSURE_SYSTOLIC }?.let { systolic ->
            readings.find { it.type == BiometricType.BLOOD_PRESSURE_DIASTOLIC }?.let { diastolic ->
                _bloodPressure.value = Pair(systolic.value.toInt(), diastolic.value.toInt())
            }
        }

        // Update stress level
        readings.find { it.type == BiometricType.STRESS_LEVEL }?.let {
            _stressLevel.value = it.value.toInt()
        }
    }

    /**
     * Measure heart rate
     */
    suspend fun measureHeartRate() {
        _isMeasuring.value = true
        try {
            // Simulate measurement
            delay(2000)

            // Get heart rate from biometric integration
            val heartRate = biometricIntegration.measureHeartRate()
            _heartRate.value = heartRate

            // Add reading
            val reading = BiometricReading(
                id = UUID.randomUUID().toString(),
                type = BiometricType.HEART_RATE,
                value = heartRate.toFloat(),
                unit = "BPM",
                normalRange = Pair(60f, 100f),
                timestamp = System.currentTimeMillis()
            )

            biometricIntegration.addBiometricReading(reading)
            _biometricReadings.value = listOf(reading) + _biometricReadings.value

            // Associate with current habit if available
            _currentHabitId.value?.let { habitId ->
                biometricIntegration.associateBiometricWithHabit(reading.id, habitId)
            }
        } catch (e: Exception) {
            _errorMessage.value = "Failed to measure heart rate: ${e.message}"
        } finally {
            _isMeasuring.value = false
        }
    }

    /**
     * Measure blood pressure
     */
    suspend fun measureBloodPressure() {
        _isMeasuring.value = true
        try {
            // Simulate measurement
            delay(3000)

            // Get blood pressure from biometric integration
            val bloodPressure = biometricIntegration.measureBloodPressure()
            _bloodPressure.value = bloodPressure

            // Add readings
            val systolicReading = BiometricReading(
                id = UUID.randomUUID().toString(),
                type = BiometricType.BLOOD_PRESSURE_SYSTOLIC,
                value = bloodPressure.first.toFloat(),
                unit = "mmHg",
                normalRange = Pair(90f, 140f),
                timestamp = System.currentTimeMillis()
            )

            val diastolicReading = BiometricReading(
                id = UUID.randomUUID().toString(),
                type = BiometricType.BLOOD_PRESSURE_DIASTOLIC,
                value = bloodPressure.second.toFloat(),
                unit = "mmHg",
                normalRange = Pair(60f, 90f),
                timestamp = System.currentTimeMillis()
            )

            biometricIntegration.addBiometricReading(systolicReading)
            biometricIntegration.addBiometricReading(diastolicReading)

            _biometricReadings.value = listOf(systolicReading, diastolicReading) + _biometricReadings.value

            // Associate with current habit if available
            _currentHabitId.value?.let { habitId ->
                biometricIntegration.associateBiometricWithHabit(systolicReading.id, habitId)
                biometricIntegration.associateBiometricWithHabit(diastolicReading.id, habitId)
            }
        } catch (e: Exception) {
            _errorMessage.value = "Failed to measure blood pressure: ${e.message}"
        } finally {
            _isMeasuring.value = false
        }
    }

    /**
     * Measure stress level
     */
    suspend fun measureStressLevel() {
        _isMeasuring.value = true
        try {
            // Simulate measurement
            delay(2500)

            // Get stress level from biometric integration
            val stressLevel = biometricIntegration.measureStressLevel()
            _stressLevel.value = stressLevel

            // Add reading
            val reading = BiometricReading(
                id = UUID.randomUUID().toString(),
                type = BiometricType.STRESS_LEVEL,
                value = stressLevel.toFloat(),
                unit = "level",
                normalRange = Pair(1f, 6f),
                timestamp = System.currentTimeMillis()
            )

            biometricIntegration.addBiometricReading(reading)
            _biometricReadings.value = listOf(reading) + _biometricReadings.value

            // Associate with current habit if available
            _currentHabitId.value?.let { habitId ->
                biometricIntegration.associateBiometricWithHabit(reading.id, habitId)
            }
        } catch (e: Exception) {
            _errorMessage.value = "Failed to measure stress level: ${e.message}"
        } finally {
            _isMeasuring.value = false
        }
    }

    /**
     * Analyze sleep data
     */
    suspend fun analyzeSleepData() {
        _isMeasuring.value = true
        try {
            // Simulate analysis
            delay(2000)

            // Get sleep data from biometric integration
            val sleepData = biometricIntegration.analyzeSleepData()
            _sleepData.value = sleepData
        } catch (e: Exception) {
            _errorMessage.value = "Failed to analyze sleep data: ${e.message}"
        } finally {
            _isMeasuring.value = false
        }
    }

    /**
     * Calculate sleep score
     */
    fun calculateSleepScore(sleepData: Map<String, Float>): Int {
        // Calculate total sleep time
        val totalSleep = sleepData.values.sum()

        // Calculate score based on sleep composition
        val deepSleepRatio = (sleepData["Deep"] ?: 0f) / totalSleep
        val remSleepRatio = (sleepData["REM"] ?: 0f) / totalSleep
        val awakeSleepRatio = (sleepData["Awake"] ?: 0f) / totalSleep

        // Ideal ratios: Deep 20-25%, REM 20-25%, Awake < 5%
        val deepSleepScore = (deepSleepRatio * 100).coerceIn(0f, 25f) * 4 // Max 100
        val remSleepScore = (remSleepRatio * 100).coerceIn(0f, 25f) * 4 // Max 100
        val awakeSleepScore = ((0.05f - awakeSleepRatio.coerceAtMost(0.05f)) / 0.05f) * 100 // Max 100

        // Total sleep time score (7-9 hours is ideal)
        val totalSleepScore = when {
            totalSleep >= 7f && totalSleep <= 9f -> 100
            totalSleep >= 6f && totalSleep < 7f -> 80
            totalSleep > 9f && totalSleep <= 10f -> 80
            totalSleep >= 5f && totalSleep < 6f -> 60
            totalSleep > 10f -> 60
            totalSleep >= 4f && totalSleep < 5f -> 40
            else -> 20
        }

        // Calculate final score
        return ((deepSleepScore + remSleepScore + awakeSleepScore + totalSleepScore) / 4).toInt()
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
