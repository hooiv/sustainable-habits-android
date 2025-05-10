package com.example.myapplication.data.biometric

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Implements biometric integration using camera and sensors
 */
@Singleton
class BiometricIntegration @Inject constructor(
    private val context: Context
) : SensorEventListener {
    companion object {
        private const val TAG = "BiometricIntegration"
        
        // Heart rate measurement constants
        private const val HEART_RATE_SAMPLE_PERIOD = 20L // milliseconds
        private const val HEART_RATE_SAMPLE_COUNT = 300 // 6 seconds of data
        private const val RED_THRESHOLD = 200 // Minimum red value to consider for heart rate

        // Constants for calorie calculation
        private const val USER_WEIGHT_KG = 70.0 // TODO: Make this dynamic from user profile
        private const val LIGHT_ACTIVITY_METS = 1.5 // Assuming light activity for this measurement context
    }
    
    // Camera components
    private var cameraExecutor: ExecutorService? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var cameraProvider: ProcessCameraProvider? = null
    
    // Flash control
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null
    
    // Sensor manager
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    
    // Available sensors
    private val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    // Biometric data
    private val _heartRate = MutableStateFlow(0)
    val heartRate: StateFlow<Int> = _heartRate.asStateFlow()
    
    private val _heartRateConfidence = MutableStateFlow(0f)
    val heartRateConfidence: StateFlow<Float> = _heartRateConfidence.asStateFlow()
    
    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount.asStateFlow()
    
    private val _caloriesBurned = MutableStateFlow(0)
    val caloriesBurned: StateFlow<Int> = _caloriesBurned.asStateFlow()
    
    private val _stressLevel = MutableStateFlow(0f)
    val stressLevel: StateFlow<Float> = _stressLevel.asStateFlow()
    
    private val _sleepQuality = MutableStateFlow<Float?>(null)
    val sleepQuality: StateFlow<Float?> = _sleepQuality.asStateFlow()

    private val _energyLevel = MutableStateFlow<Float?>(null)
    val energyLevel: StateFlow<Float?> = _energyLevel.asStateFlow()

    private val _focusLevel = MutableStateFlow<Float?>(null)
    val focusLevel: StateFlow<Float?> = _focusLevel.asStateFlow()

    private val _mood = MutableStateFlow<Float?>(null)
    val mood: StateFlow<Float?> = _mood.asStateFlow()
    
    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()
    
    // Heart rate measurement
    private val redIntensities = mutableListOf<Int>()
    private val heartRateHistory = mutableListOf<Int>()
    private var lastHeartRateMeasurement = 0L
    private val handler = Handler(Looper.getMainLooper())
    
    // Accelerometer data for step counting and stress level
    private val accelerometerData = FloatArray(3)
    private val accelerometerHistory = mutableListOf<FloatArray>()
    
    init {
        // Find camera with flash
        try {
            for (id in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                if (hasFlash) {
                    cameraId = id
                    break
                }
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Error accessing camera: ${e.message}")
        }
    }
    
    /**
     * Start heart rate monitoring
     */
    fun startHeartRateMonitoring(lifecycleOwner: LifecycleOwner) {
        if (_isMonitoring.value) return
        
        _isMonitoring.value = true
        redIntensities.clear()
        
        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor()
        
        // Set up image analysis
        imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(cameraExecutor!!, HeartRateAnalyzer())
            }
        
        // Start camera with flash
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            
            try {
                // Unbind any bound use cases
                cameraProvider?.unbindAll()
                
                // Select back camera
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                // Bind camera to lifecycle
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    imageAnalyzer
                )
                
                // Turn on flash
                turnOnFlash()
                
                // Start heart rate calculation
                startHeartRateCalculation()
                
            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
                stopHeartRateMonitoring()
            }
            
        }, ContextCompat.getMainExecutor(context))
        
        // Register sensor listeners
        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        
        accelerometerSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        
        // Simulate step count if sensor not available
        if (stepCounterSensor == null) {
            simulateStepCount()
        }
        
        // Set initial placeholder values for metrics
        simulateSleepQuality()
        _energyLevel.value = null
        _focusLevel.value = null
        _mood.value = null
        
        Log.d(TAG, "Started heart rate monitoring")
    }
    
    /**
     * Stop heart rate monitoring
     */
    fun stopHeartRateMonitoring() {
        if (!_isMonitoring.value) return
        
        _isMonitoring.value = false
        
        // Turn off flash
        turnOffFlash()
        
        // Stop camera
        cameraProvider?.unbindAll()
        cameraExecutor?.shutdown()
        cameraExecutor = null
        imageAnalyzer = null
        
        // Unregister sensor listeners
        sensorManager.unregisterListener(this)
        
        // Stop heart rate calculation
        handler.removeCallbacksAndMessages(null)
        
        Log.d(TAG, "Stopped heart rate monitoring")
    }
    
    /**
     * Turn on camera flash
     */
    private fun turnOnFlash() {
        try {
            cameraId?.let {
                cameraManager.setTorchMode(it, true)
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Error turning on flash: ${e.message}")
        }
    }
    
    /**
     * Turn off camera flash
     */
    private fun turnOffFlash() {
        try {
            cameraId?.let {
                cameraManager.setTorchMode(it, false)
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Error turning off flash: ${e.message}")
        }
    }
    
    /**
     * Start heart rate calculation
     */
    private fun startHeartRateCalculation() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                calculateHeartRate()
                if (_isMonitoring.value) {
                    handler.postDelayed(this, 1000) // Update every second
                }
            }
        }, 1000)
    }
    
    /**
     * Calculate heart rate from red intensity data
     */
    private fun calculateHeartRate() {
        if (redIntensities.size < 10) return
        
        try {
            // Find peaks in the red intensity data
            val peaks = findPeaks(redIntensities)
            
            // Calculate heart rate from peaks
            if (peaks.size >= 2) {
                val timePerPeak = HEART_RATE_SAMPLE_PERIOD * redIntensities.size / peaks.size
                val beatsPerMinute = (60 * 1000.0 / timePerPeak.toDouble()).toInt() // Use toInt() for Double
                
                // Filter out unreasonable values
                if (beatsPerMinute in 40..200) {
                    _heartRate.value = beatsPerMinute
                    
                    // Add to history
                    heartRateHistory.add(beatsPerMinute)
                    if (heartRateHistory.size > 10) {
                        heartRateHistory.removeAt(0)
                    }
                    
                    // Calculate confidence based on consistency
                    val avg = heartRateHistory.average()
                    val variance = heartRateHistory.map { abs(it - avg) }.average()
                    val confidence = (1.0 - variance / avg).coerceIn(0.0, 1.0)
                    _heartRateConfidence.value = confidence.toFloat()
                    
                    // Update calories burned based on heart rate
                    updateCaloriesBurned()
                    
                    Log.d(TAG, "Heart rate: ${_heartRate.value} BPM, confidence: ${_heartRateConfidence.value}")
                }
            }
            
            // Clear old data
            if (redIntensities.size > HEART_RATE_SAMPLE_COUNT) {
                redIntensities.subList(0, redIntensities.size - HEART_RATE_SAMPLE_COUNT / 2).clear()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating heart rate: ${e.message}")
        }
    }
    
    /**
     * Find peaks in the signal
     */
    private fun findPeaks(signal: List<Int>): List<Int> {
        val peaks = mutableListOf<Int>()
        
        // Simple peak detection
        for (i in 1 until signal.size - 1) {
            if (signal[i] > signal[i - 1] && signal[i] > signal[i + 1] && signal[i] > RED_THRESHOLD) {
                peaks.add(i)
            }
        }
        
        return peaks
    }
    
    /**
     * Update calories burned based on heart rate
     */
    private fun updateCaloriesBurned() {
        // Simple formula: Calories/min = (METs * bodyWeightKg * 3.5) / 200
        // This function is called when heart rate is updated (roughly per second if HR calc is per second)
        val caloriesPerMinute = (LIGHT_ACTIVITY_METS * USER_WEIGHT_KG * 3.5) / 200
        val caloriesThisUpdate = caloriesPerMinute / 60.0 // Calories for one second interval
        
        _caloriesBurned.value += caloriesThisUpdate.toInt() // Fix: use toInt() instead of roundToInt() for Double
    }
    
    /**
     * Simulate step count
     */
    private fun simulateStepCount() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Simulate 10-20 steps per minute
                val stepIncrement = (Math.random() * 10 + 10).toInt() / 60
                _stepCount.value = _stepCount.value + stepIncrement
                
                if (_isMonitoring.value) {
                    handler.postDelayed(this, 1000) // Update every second
                }
            }
        }, 1000)
    }
    
    /**
     * Simulate sleep quality
     */
    private fun simulateSleepQuality() {
        // Simulate sleep quality (0-1 scale)
        _sleepQuality.value = null // Placeholder: Real sleep quality measurement is complex and not implemented.
        Log.i(TAG, "Sleep quality is a placeholder (null). Real implementation needed.")
    }
    
    /**
     * Handle sensor events
     */
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val steps = event.values[0].toInt()
                _stepCount.value = steps
            }
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, accelerometerData, 0, 3)
                accelerometerHistory.add(event.values.clone())
                if (accelerometerHistory.size > 100) {
                    accelerometerHistory.removeAt(0)
                }
                
                // Calculate stress level from accelerometer data
                calculateStressLevel()
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not used
    }
    
    /**
     * Calculate stress level from accelerometer data
     */
    private fun calculateStressLevel() {
        if (accelerometerHistory.size < 10) return
        
        // Calculate movement variability
        var totalVariability = 0f
        for (i in 1 until accelerometerHistory.size) {
            val prev = accelerometerHistory[i - 1]
            val curr = accelerometerHistory[i]
            
            var sum = 0f
            for (j in 0 until 3) {
                val diff = curr[j] - prev[j]
                sum += diff * diff
            }
            
            totalVariability += Math.sqrt(sum.toDouble()).toFloat()
        }
        
        val avgVariability = totalVariability / (accelerometerHistory.size - 1)
        
        // Map variability to stress level (0-1 scale)
        val stressLevel = (avgVariability / 5f).coerceIn(0f, 1f)
        _stressLevel.value = stressLevel
    }
    
    /**
     * Image analyzer for heart rate detection
     */
    private inner class HeartRateAnalyzer : ImageAnalysis.Analyzer {
        override fun analyze(image: ImageProxy) {
            // Only process every 20ms
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastHeartRateMeasurement < HEART_RATE_SAMPLE_PERIOD) {
                image.close()
                return
            }
            lastHeartRateMeasurement = currentTime
            
            // Get image data
            val buffer = image.planes[0].buffer
            val redIntensity = calculateRedIntensity(buffer, image.width, image.height)
            
            // Add to intensity list
            redIntensities.add(redIntensity)
            
            image.close()
        }
        
        /**
         * Calculate average red intensity from image data
         */
        private fun calculateRedIntensity(buffer: ByteBuffer, width: Int, height: Int): Int {
            val data = ByteArray(buffer.remaining())
            buffer.get(data)
            
            var redSum = 0
            var pixelCount = 0
            
            // Sample pixels from the center of the image
            val centerX = width / 2
            val centerY = height / 2
            val sampleRadius = Math.min(width, height) / 4
            
            for (y in centerY - sampleRadius until centerY + sampleRadius step 4) {
                for (x in centerX - sampleRadius until centerX + sampleRadius step 4) {
                    if (y < 0 || y >= height || x < 0 || x >= width) continue
                    
                    val index = y * width + x
                    if (index >= 0 && index < data.size) {
                        // Get red component (assuming YUV format)
                        val red = data[index].toInt() and 0xFF
                        redSum += red
                        pixelCount++
                    }
                }
            }
            
            return if (pixelCount > 0) redSum / pixelCount else 0
        }
    }
    
    /**
     * Get biometric data for habit tracking
     */
    fun getBiometricData(): BiometricData {
        return BiometricData(
            heartRate = _heartRate.value,
            heartRateConfidence = _heartRateConfidence.value,
            stepCount = _stepCount.value,
            caloriesBurned = _caloriesBurned.value,
            stressLevel = _stressLevel.value,
            sleepQuality = _sleepQuality.value,
            energyLevel = _energyLevel.value,
            focusLevel = _focusLevel.value,
            mood = _mood.value
        )
    }
}

/**
 * Biometric data for habit tracking
 */
data class BiometricData(
    val heartRate: Int,
    val heartRateConfidence: Float,
    val stepCount: Int,
    val caloriesBurned: Int,
    val stressLevel: Float,
    val sleepQuality: Float?, // Updated to nullable
    val energyLevel: Float?,   // Added
    val focusLevel: Float?,    // Added
    val mood: Float?           // Added
)
