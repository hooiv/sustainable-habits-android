package com.example.myapplication.data.biometric

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

/**
 * Manages biometric authentication and health data collection
 */
@Singleton
class BiometricManager @Inject constructor(
    private val context: Context
) : SensorEventListener {
    companion object {
        private const val TAG = "BiometricManager"
        
        // Sampling parameters
        private const val HEART_RATE_SAMPLING_PERIOD_MS = 5000L // 5 seconds
        private const val STEP_COUNT_SAMPLING_PERIOD_MS = 1000L // 1 second
        private const val STRESS_LEVEL_SAMPLING_PERIOD_MS = 10000L // 10 seconds
    }
    
    // Biometric authentication
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    
    // Sensor manager
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    
    // Available sensors
    private val heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
    private val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    
    // Biometric data
    private val _heartRate = MutableStateFlow(0)
    val heartRate: StateFlow<Int> = _heartRate.asStateFlow()
    
    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount.asStateFlow()
    
    private val _stressLevel = MutableStateFlow(0f)
    val stressLevel: StateFlow<Float> = _stressLevel.asStateFlow()
    
    private val _sleepQuality = MutableStateFlow(0f)
    val sleepQuality: StateFlow<Float> = _sleepQuality.asStateFlow()
    
    private val _isCollectingData = MutableStateFlow(false)
    val isCollectingData: StateFlow<Boolean> = _isCollectingData.asStateFlow()
    
    // Authentication state
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    // Biometric data history
    private val _biometricHistory = MutableStateFlow<List<BiometricDataPoint>>(emptyList())
    val biometricHistory: StateFlow<List<BiometricDataPoint>> = _biometricHistory.asStateFlow()
    
    // Accelerometer and gyroscope data for stress level calculation
    private val accelerometerData = FloatArray(3)
    private val gyroscopeData = FloatArray(3)
    private val accelerometerHistory = mutableListOf<FloatArray>()
    private val gyroscopeHistory = mutableListOf<FloatArray>()
    
    // Handlers for periodic sampling
    private val handler = Handler(Looper.getMainLooper())
    private val heartRateRunnable = object : Runnable {
        override fun run() {
            simulateHeartRateReading()
            handler.postDelayed(this, HEART_RATE_SAMPLING_PERIOD_MS)
        }
    }
    
    private val stepCountRunnable = object : Runnable {
        override fun run() {
            simulateStepCountReading()
            handler.postDelayed(this, STEP_COUNT_SAMPLING_PERIOD_MS)
        }
    }
    
    private val stressLevelRunnable = object : Runnable {
        override fun run() {
            calculateStressLevel()
            handler.postDelayed(this, STRESS_LEVEL_SAMPLING_PERIOD_MS)
        }
    }
    
    /**
     * Initialize biometric authentication
     */
    fun initBiometricAuth(activity: FragmentActivity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        executor = ContextCompat.getMainExecutor(activity)
        
        biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    _isAuthenticated.value = false
                    onError("Authentication error: $errString")
                }
                
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    _isAuthenticated.value = true
                    onSuccess()
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    _isAuthenticated.value = false
                    onError("Authentication failed")
                }
            })
        
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()
    }
    
    /**
     * Show biometric authentication prompt
     */
    fun showBiometricPrompt() {
        biometricPrompt.authenticate(promptInfo)
    }
    
    /**
     * Start collecting biometric data
     */
    fun startCollectingData() {
        if (_isCollectingData.value) return
        
        _isCollectingData.value = true
        
        // Register sensor listeners
        heartRateSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        
        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        
        accelerometerSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        
        gyroscopeSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        
        // Start simulation if sensors are not available
        if (heartRateSensor == null) {
            handler.post(heartRateRunnable)
        }
        
        if (stepCounterSensor == null) {
            handler.post(stepCountRunnable)
        }
        
        // Start stress level calculation
        handler.post(stressLevelRunnable)
        
        // Simulate sleep quality
        simulateSleepQuality()
        
        Log.d(TAG, "Started collecting biometric data")
    }
    
    /**
     * Stop collecting biometric data
     */
    fun stopCollectingData() {
        if (!_isCollectingData.value) return
        
        _isCollectingData.value = false
        
        // Unregister sensor listeners
        sensorManager.unregisterListener(this)
        
        // Stop simulation
        handler.removeCallbacks(heartRateRunnable)
        handler.removeCallbacks(stepCountRunnable)
        handler.removeCallbacks(stressLevelRunnable)
        
        Log.d(TAG, "Stopped collecting biometric data")
    }
    
    /**
     * Handle sensor events
     */
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_HEART_RATE -> {
                val heartRate = event.values[0].toInt()
                _heartRate.value = heartRate
                addBiometricDataPoint(BiometricType.HEART_RATE, heartRate.toFloat())
            }
            Sensor.TYPE_STEP_COUNTER -> {
                val steps = event.values[0].toInt()
                _stepCount.value = steps
                addBiometricDataPoint(BiometricType.STEP_COUNT, steps.toFloat())
            }
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, accelerometerData, 0, 3)
                accelerometerHistory.add(event.values.clone())
                if (accelerometerHistory.size > 100) {
                    accelerometerHistory.removeAt(0)
                }
            }
            Sensor.TYPE_GYROSCOPE -> {
                System.arraycopy(event.values, 0, gyroscopeData, 0, 3)
                gyroscopeHistory.add(event.values.clone())
                if (gyroscopeHistory.size > 100) {
                    gyroscopeHistory.removeAt(0)
                }
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not used
    }
    
    /**
     * Simulate heart rate reading
     */
    private fun simulateHeartRateReading() {
        // Simulate heart rate between 60 and 100 BPM
        val baseHeartRate = 70
        val variation = (Math.random() * 20 - 10).toInt()
        val heartRate = baseHeartRate + variation
        
        _heartRate.value = heartRate
        addBiometricDataPoint(BiometricType.HEART_RATE, heartRate.toFloat())
    }
    
    /**
     * Simulate step count reading
     */
    private fun simulateStepCountReading() {
        // Simulate step count increasing by 10-20 steps per minute
        val stepIncrement = (Math.random() * 10 + 10).toInt() / 60
        _stepCount.value = _stepCount.value + stepIncrement
        
        addBiometricDataPoint(BiometricType.STEP_COUNT, _stepCount.value.toFloat())
    }
    
    /**
     * Calculate stress level from accelerometer and gyroscope data
     */
    private fun calculateStressLevel() {
        // In a real app, this would use a more sophisticated algorithm
        // For this demo, we'll use a simple approach based on movement variability
        
        var stressLevel = 0f
        
        if (accelerometerHistory.size > 10 && gyroscopeHistory.size > 10) {
            // Calculate movement variability
            val accVariability = calculateVariability(accelerometerHistory)
            val gyroVariability = calculateVariability(gyroscopeHistory)
            
            // Combine variabilities to estimate stress level (0-1 scale)
            stressLevel = (accVariability + gyroVariability) / 2f
            stressLevel = stressLevel.coerceIn(0f, 1f)
        } else {
            // Simulate stress level if not enough sensor data
            stressLevel = (Math.random() * 0.5 + 0.25).toFloat()
        }
        
        _stressLevel.value = stressLevel
        addBiometricDataPoint(BiometricType.STRESS_LEVEL, stressLevel)
    }
    
    /**
     * Simulate sleep quality
     */
    private fun simulateSleepQuality() {
        val placeholderSleepQuality = 0.0f // Using 0.0f as a placeholder
        _sleepQuality.value = placeholderSleepQuality
        addBiometricDataPoint(BiometricType.SLEEP_QUALITY, placeholderSleepQuality)
        Log.i(TAG, "Sleep quality is a placeholder (set to 0.0). Real implementation needed.")
    }
    
    /**
     * Calculate variability of sensor data
     */
    private fun calculateVariability(dataHistory: List<FloatArray>): Float {
        if (dataHistory.size < 2) return 0f
        
        var totalVariability = 0f
        var count = 0
        
        for (i in 1 until dataHistory.size) {
            val prev = dataHistory[i - 1]
            val curr = dataHistory[i]
            
            var sum = 0f
            for (j in 0 until 3) {
                val diff = curr[j] - prev[j]
                sum += diff * diff
            }
            
            totalVariability += sqrt(sum)
            count++
        }
        
        return if (count > 0) totalVariability / count else 0f
    }
    
    /**
     * Add biometric data point to history
     */
    private fun addBiometricDataPoint(type: BiometricType, value: Float) {
        val dataPoint = BiometricDataPoint(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            type = type,
            value = value
        )
        
        _biometricHistory.value = _biometricHistory.value + dataPoint
        
        // Limit history size
        if (_biometricHistory.value.size > 1000) {
            _biometricHistory.value = _biometricHistory.value.drop(100)
        }
    }
    
    /**
     * Get biometric data for a specific type
     */
    fun getBiometricData(type: BiometricType): List<BiometricDataPoint> {
        return _biometricHistory.value.filter { it.type == type }
    }
    
    /**
     * Get biometric data for a specific time range
     */
    fun getBiometricDataInRange(
        type: BiometricType,
        startTime: Long,
        endTime: Long
    ): List<BiometricDataPoint> {
        return _biometricHistory.value.filter {
            it.type == type && it.timestamp >= startTime && it.timestamp <= endTime
        }
    }
    
    /**
     * Get average value for a specific biometric type
     */
    fun getAverageValue(type: BiometricType): Float {
        val dataPoints = getBiometricData(type)
        if (dataPoints.isEmpty()) return 0f
        
        return dataPoints.map { it.value }.average().toFloat()
    }
}

/**
 * Biometric data point
 */
data class BiometricDataPoint(
    val id: String,
    val timestamp: Long,
    val type: BiometricType,
    val value: Float
)

/**
 * Biometric data types
 */
enum class BiometricType {
    HEART_RATE,
    STEP_COUNT,
    STRESS_LEVEL,
    SLEEP_QUALITY
}
