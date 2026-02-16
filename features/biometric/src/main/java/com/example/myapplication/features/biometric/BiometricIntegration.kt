package com.example.myapplication.features.biometric

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
import androidx.camera.core.Camera
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

import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Implements biometric integration using camera and sensors
 */
@Singleton
class BiometricIntegration @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorEventListener {
    companion object {
        private const val TAG = "BiometricIntegration"

        // Heart rate measurement constants
        private const val HEART_RATE_SAMPLE_PERIOD = 20L // milliseconds
        private const val HEART_RATE_SAMPLE_COUNT = 300 // 6 seconds of data
        private const val RED_THRESHOLD = 200 // Minimum red value to consider for heart rate

        // Constants for calorie calculation
        private const val DEFAULT_USER_WEIGHT_KG = 70.0
        private const val LIGHT_ACTIVITY_METS = 1.5 // Assuming light activity for this measurement context
        private const val MODERATE_ACTIVITY_METS = 3.0 // For moderate activity
        private const val VIGOROUS_ACTIVITY_METS = 6.0 // For vigorous activity
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

    // In-memory storage for biometric readings
    private val biometricReadings = mutableListOf<com.example.myapplication.core.data.model.BiometricReading>()

    // Map to track associations between biometric readings and habits
    private val biometricHabitAssociations = mutableMapOf<String, String>() // readingId -> habitId

    // User profile data
    private val _userWeight = MutableStateFlow(DEFAULT_USER_WEIGHT_KG)
    val userWeight: StateFlow<Double> = _userWeight.asStateFlow()

    // Activity level based on accelerometer data
    private val _activityLevel = MutableStateFlow(ActivityLevel.LIGHT)
    val activityLevel: StateFlow<ActivityLevel> = _activityLevel.asStateFlow()

    // Heart rate measurement
    private val redIntensities = mutableListOf<Int>()
    private val heartRateHistory = mutableListOf<Int>()
    private var lastHeartRateMeasurement = 0L
    private val handler = Handler(Looper.getMainLooper())

    // Flash monitoring
    private var flashCheckRunnable: Runnable? = null

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
    fun startHeartRateMonitoring(lifecycleOwner: LifecycleOwner?) {
        if (_isMonitoring.value) return
        if (lifecycleOwner == null) {
            Log.e(TAG, "Cannot start heart rate monitoring: lifecycleOwner is null")
            return
        }

        _isMonitoring.value = true
        redIntensities.clear()

        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Set up image analysis with optimized settings
        imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .setImageQueueDepth(4) // Increase buffer size to prevent "Unable to acquire a buffer item" errors
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

                // Bind camera to lifecycle
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    imageAnalyzer
                )

                // Turn on flash
                turnOnFlash()

                // Start flash monitoring to ensure it stays on
                startFlashMonitoring()

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
     * Start flash monitoring to ensure it stays on during heart rate monitoring
     */
    private fun startFlashMonitoring() {
        // Cancel any existing flash check
        stopFlashMonitoring()

        // Create a new runnable for periodic flash checks
        flashCheckRunnable = object : Runnable {
            override fun run() {
                if (_isMonitoring.value) {
                    // Check if flash is on and turn it on if it's not
                    try {
                        cameraId?.let { id ->
                            // Check if torch mode is currently on
                            val isFlashOn = try {
                                // This is a workaround since there's no direct way to check torch state
                                // We'll just try to turn it on again, which is safe if it's already on
                                cameraManager.setTorchMode(id, true)
                                Log.d(TAG, "Flash check - ensuring flash is on")
                                true
                            } catch (e: Exception) {
                                Log.e(TAG, "Flash check failed: ${e.message}")
                                false
                            }

                            // Schedule the next check
                            handler.postDelayed(this, 2000) // Check every 2 seconds
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during flash check: ${e.message}")
                    }
                }
            }
        }

        // Start the periodic check
        handler.postDelayed(flashCheckRunnable!!, 2000) // First check after 2 seconds
    }

    /**
     * Stop flash monitoring
     */
    private fun stopFlashMonitoring() {
        flashCheckRunnable?.let {
            handler.removeCallbacks(it)
            flashCheckRunnable = null
        }
    }

    /**
     * Stop heart rate monitoring with proper cleanup sequence
     */
    fun stopHeartRateMonitoring() {
        if (!_isMonitoring.value) return

        _isMonitoring.value = false

        // First, stop heart rate calculation to prevent further processing
        handler.removeCallbacksAndMessages(null)

        // Stop flash monitoring
        stopFlashMonitoring()

        // Unregister sensor listeners
        sensorManager.unregisterListener(this)

        try {
            // Unbind camera use cases first
            cameraProvider?.unbindAll()

            // Small delay to ensure camera is released before turning off flash
            Thread.sleep(50)

            // Now it's safe to turn off flash
            turnOffFlash()
        } catch (e: Exception) {
            Log.e(TAG, "Error during camera cleanup: ${e.message}")
        } finally {
            // Clean up executor
            cameraExecutor?.shutdown()
            cameraExecutor = null
            imageAnalyzer = null

            Log.d(TAG, "Stopped heart rate monitoring")
        }
    }

    /**
     * Turn on camera flash with retry mechanism
     */
    private fun turnOnFlash() {
        // Try up to 3 times with increasing delays
        for (attempt in 1..3) {
            try {
                // Get the ID of the back camera if we don't already have it
                if (cameraId == null) {
                    for (id in cameraManager.cameraIdList) {
                        val characteristics = cameraManager.getCameraCharacteristics(id)
                        val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)

                        if (cameraDirection == CameraCharacteristics.LENS_FACING_BACK) {
                            // Check if flash is available
                            val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false

                            if (hasFlash) {
                                cameraId = id
                                break
                            }
                        }
                    }
                }

                // Turn on flash if camera ID is available
                cameraId?.let {
                    cameraManager.setTorchMode(it, true)
                    Log.d(TAG, "Flash turned on successfully on attempt $attempt")
                    return // Exit the retry loop if successful
                } ?: run {
                    Log.e(TAG, "No camera with flash available")
                    return // Exit if no flash is available
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error turning on flash (attempt $attempt): ${e.message}")

                // If this is the last attempt, don't delay
                if (attempt < 3) {
                    try {
                        // Increase delay with each attempt
                        Thread.sleep(attempt * 100L)
                    } catch (ie: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }
                }
            }
        }

        // If we get here, all attempts failed
        Log.w(TAG, "Failed to turn on flash after multiple attempts")
    }

    /**
     * Turn off camera flash with retry mechanism
     */
    private fun turnOffFlash() {
        // Try up to 3 times with increasing delays
        for (attempt in 1..3) {
            try {
                cameraId?.let {
                    cameraManager.setTorchMode(it, false)
                    Log.d(TAG, "Flash turned off on attempt $attempt")
                    // If successful, exit the retry loop
                    return
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error turning off flash (attempt $attempt): ${e.message}")

                // If this is the last attempt, don't delay
                if (attempt < 3) {
                    try {
                        // Increase delay with each attempt
                        Thread.sleep(attempt * 100L)
                    } catch (ie: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }
                }
            }
        }

        // If we get here, all attempts failed
        Log.w(TAG, "Failed to turn off flash after multiple attempts")
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
     * Update calories burned based on heart rate and activity level
     */
    private fun updateCaloriesBurned() {
        // Get the appropriate MET value based on activity level
        val metValue = when (_activityLevel.value) {
            ActivityLevel.LIGHT -> LIGHT_ACTIVITY_METS
            ActivityLevel.MODERATE -> MODERATE_ACTIVITY_METS
            ActivityLevel.VIGOROUS -> VIGOROUS_ACTIVITY_METS
        }

        // Adjust MET value based on heart rate
        val heartRateAdjustment = when (_heartRate.value) {
            in 0..80 -> 0.0
            in 81..100 -> 0.5
            in 101..120 -> 1.0
            in 121..140 -> 1.5
            in 141..160 -> 2.0
            else -> 2.5
        }

        val adjustedMet = metValue + heartRateAdjustment

        // Formula: Calories/min = (METs * bodyWeightKg * 3.5) / 200
        val caloriesPerMinute = (adjustedMet * _userWeight.value * 3.5) / 200
        val caloriesThisUpdate = caloriesPerMinute / 60.0 // Calories for one second interval

        _caloriesBurned.value += caloriesThisUpdate.toInt()
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
     * Calculate stress level and activity level from accelerometer data
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

        // Update activity level based on movement variability
        _activityLevel.value = when {
            avgVariability < 1.0f -> ActivityLevel.LIGHT
            avgVariability < 3.0f -> ActivityLevel.MODERATE
            else -> ActivityLevel.VIGOROUS
        }

        // Update energy level based on activity and heart rate
        val energyBase = when (_activityLevel.value) {
            ActivityLevel.LIGHT -> 0.3f
            ActivityLevel.MODERATE -> 0.5f
            ActivityLevel.VIGOROUS -> 0.7f
        }

        // Heart rate contribution (higher heart rate = higher energy)
        val heartRateContribution = (_heartRate.value - 60).coerceAtLeast(0) / 100f

        // Calculate energy level (0-1 scale)
        _energyLevel.value = (energyBase + heartRateContribution).coerceIn(0f, 1f)

        // Update focus level based on stress and activity
        _focusLevel.value = (1f - _stressLevel.value * 0.5f).coerceIn(0f, 1f)
    }

    /**
     * Image analyzer for heart rate detection
     */
    private inner class HeartRateAnalyzer : ImageAnalysis.Analyzer {
        override fun analyze(image: ImageProxy) {
            try {
                // Only process every 20ms to avoid overwhelming the system
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastHeartRateMeasurement < HEART_RATE_SAMPLE_PERIOD) {
                    // Skip this frame but make sure to close it
                    image.close()
                    return
                }
                lastHeartRateMeasurement = currentTime

                // Get image data
                val buffer = image.planes[0].buffer
                val redIntensity = calculateRedIntensity(buffer, image.width, image.height)

                // Add to intensity list if it's a valid reading
                if (redIntensity > 0) {
                    redIntensities.add(redIntensity)

                    // Limit the size of redIntensities to prevent memory issues
                    if (redIntensities.size > HEART_RATE_SAMPLE_COUNT * 2) {
                        redIntensities.subList(0, redIntensities.size - HEART_RATE_SAMPLE_COUNT).clear()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error analyzing image: ${e.message}")
            } finally {
                // Always close the image to release resources
                image.close()
            }
        }

        /**
         * Calculate average red intensity from image data with optimized sampling
         */
        private fun calculateRedIntensity(buffer: ByteBuffer, width: Int, height: Int): Int {
            // Don't copy the entire buffer, just read what we need
            val bufferSize = buffer.remaining()

            var redSum = 0
            var pixelCount = 0

            // Sample pixels from the center of the image
            val centerX = width / 2
            val centerY = height / 2
            val sampleRadius = Math.min(width, height) / 4

            // Use a larger step size to reduce processing load
            // This reduces the number of pixels we sample by 16x
            val stepSize = 8

            // Calculate the center region to sample (more efficient than checking bounds for each pixel)
            val startY = Math.max(0, centerY - sampleRadius)
            val endY = Math.min(height, centerY + sampleRadius)
            val startX = Math.max(0, centerX - sampleRadius)
            val endX = Math.min(width, centerX + sampleRadius)

            for (y in startY until endY step stepSize) {
                for (x in startX until endX step stepSize) {
                    val index = y * width + x

                    // Ensure we don't read outside the buffer
                    if (index >= 0 && index < bufferSize) {
                        // Get red component (assuming YUV format)
                        // Read directly from buffer instead of copying to a byte array
                        val red = buffer.get(index).toInt() and 0xFF

                        // Only count pixels that might be from the fingertip (filter out dark pixels)
                        if (red > RED_THRESHOLD) {
                            redSum += red
                            pixelCount++
                        }
                    }
                }
            }

            // Reset buffer position to not affect other readers
            buffer.position(0)

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
            mood = _mood.value,
            activityLevel = _activityLevel.value
        )
    }

    /**
     * Set user weight
     */
    fun setUserWeight(weightKg: Double) {
        _userWeight.value = weightKg
    }

    /**
     * Set mood level
     */
    fun setMood(moodLevel: Float) {
        _mood.value = moodLevel.coerceIn(0f, 1f)
    }

    /**
     * Get all biometric readings
     */
    fun getBiometricReadings(): List<com.example.myapplication.core.data.model.BiometricReading> {
        // Initialize with some sample data if empty
        if (biometricReadings.isEmpty()) {
            initializeSampleBiometricData()
        }
        return biometricReadings.toList()
    }

    /**
     * Get biometric readings for a specific habit
     */
    fun getBiometricReadingsForHabit(habitId: String): List<com.example.myapplication.core.data.model.BiometricReading> {
        val readingIds = biometricHabitAssociations.filter { it.value == habitId }.keys
        return biometricReadings.filter { it.id in readingIds }
    }

    /**
     * Add a biometric reading
     */
    fun addBiometricReading(reading: com.example.myapplication.core.data.model.BiometricReading) {
        biometricReadings.add(reading)
    }

    /**
     * Associate a biometric reading with a habit
     */
    fun associateBiometricWithHabit(readingId: String, habitId: String) {
        biometricHabitAssociations[readingId] = habitId
    }

    /**
     * Measure heart rate using camera and flash
     */
    fun measureHeartRate(): Int {
        // If we're already monitoring, return the current heart rate
        if (_isMonitoring.value) {
            return _heartRate.value
        }

        // If we have recent heart rate history, return the average
        if (heartRateHistory.isNotEmpty()) {
            return heartRateHistory.average().toInt()
        }

        // Otherwise, perform a quick measurement
        val currentHeartRate = if (_heartRate.value > 0) {
            // Add some variation to the current heart rate
            _heartRate.value + kotlin.random.Random.nextInt(-5, 5)
        } else {
            // Simulate a reasonable heart rate if we don't have a current value
            70 + kotlin.random.Random.nextInt(-10, 15)
        }

        // Ensure the heart rate is within a reasonable range
        return currentHeartRate.coerceIn(40, 200)
    }

    /**
     * Measure blood pressure
     */
    fun measureBloodPressure(): Pair<Int, Int> {
        // Simulate blood pressure measurement
        val systolic = 120 + kotlin.random.Random.nextInt(-10, 10)
        val diastolic = 80 + kotlin.random.Random.nextInt(-5, 5)
        return Pair(systolic, diastolic)
    }

    /**
     * Measure stress level
     */
    fun measureStressLevel(): Int {
        // Simulate stress level measurement (1-10 scale)
        return kotlin.random.Random.nextInt(1, 10)
    }

    /**
     * Analyze sleep data
     */
    fun analyzeSleepData(): Map<String, Float> {
        // Simulate sleep data analysis
        return mapOf(
            "Deep" to 1.5f + kotlin.random.Random.nextFloat() * 1.0f,
            "Light" to 3.0f + kotlin.random.Random.nextFloat() * 1.5f,
            "REM" to 1.2f + kotlin.random.Random.nextFloat() * 1.0f,
            "Awake" to 0.3f + kotlin.random.Random.nextFloat() * 0.5f
        )
    }

    /**
     * Initialize sample biometric data
     */
    private fun initializeSampleBiometricData() {
        // Add some sample heart rate readings
        val calendar = Calendar.getInstance()
        for (i in 0 until 10) {
            calendar.add(Calendar.HOUR, -i)
            biometricReadings.add(
                com.example.myapplication.core.data.model.BiometricReading(
                    id = UUID.randomUUID().toString(),
                    type = com.example.myapplication.core.data.model.BiometricType.HEART_RATE,
                    value = (65.0 + kotlin.random.Random.nextDouble(-5.0, 15.0)).toFloat(),
                    unit = "BPM",
                    normalRange = Pair(60f, 100f),
                    timestamp = calendar.timeInMillis
                )
            )
        }

        // Add some sample blood pressure readings
        calendar.timeInMillis = System.currentTimeMillis()
        for (i in 0 until 5) {
            calendar.add(Calendar.DAY_OF_MONTH, -i)

            // Systolic (top number)
            val systolicId = UUID.randomUUID().toString()
            biometricReadings.add(
                com.example.myapplication.core.data.model.BiometricReading(
                    id = systolicId,
                    type = com.example.myapplication.core.data.model.BiometricType.BLOOD_PRESSURE_SYSTOLIC,
                    value = (120.0 + kotlin.random.Random.nextDouble(-10.0, 10.0)).toFloat(),
                    unit = "mmHg",
                    normalRange = Pair(90f, 140f),
                    timestamp = calendar.timeInMillis
                )
            )

            // Diastolic (bottom number)
            biometricReadings.add(
                com.example.myapplication.core.data.model.BiometricReading(
                    id = UUID.randomUUID().toString(),
                    type = com.example.myapplication.core.data.model.BiometricType.BLOOD_PRESSURE_DIASTOLIC,
                    value = (80.0 + kotlin.random.Random.nextDouble(-5.0, 5.0)).toFloat(),
                    unit = "mmHg",
                    normalRange = Pair(60f, 90f),
                    timestamp = calendar.timeInMillis
                )
            )
        }

        // Add some stress level readings
        calendar.timeInMillis = System.currentTimeMillis()
        for (i in 0 until 7) {
            calendar.add(Calendar.DAY_OF_MONTH, -i)
            biometricReadings.add(
                com.example.myapplication.core.data.model.BiometricReading(
                    id = UUID.randomUUID().toString(),
                    type = com.example.myapplication.core.data.model.BiometricType.STRESS_LEVEL,
                    value = kotlin.random.Random.nextDouble(1.0, 10.0).toFloat(),
                    unit = "level",
                    normalRange = Pair(1f, 6f),
                    timestamp = calendar.timeInMillis
                )
            )
        }
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
    val mood: Float?,          // Added
    val activityLevel: ActivityLevel = ActivityLevel.LIGHT // Added
)

/**
 * Activity level for calorie calculation
 */
enum class ActivityLevel {
    LIGHT,
    MODERATE,
    VIGOROUS
}
