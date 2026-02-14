package com.example.myapplication.features.ml

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.network.ml.MultiModalLearning
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.model.HabitCompletion
import com.example.myapplication.core.data.model.HabitFrequency
import com.example.myapplication.core.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for multi-modal learning
 */
@HiltViewModel
class MultiModalLearningViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val habitRepository: HabitRepository,
    private val multiModalLearning: MultiModalLearning
) : ViewModel(), SensorEventListener {

    companion object {
        private const val TAG = "MultiModalLearningVM"
    }

    // State
    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> = _imageUri.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _hasSensorData = MutableStateFlow(false)
    val hasSensorData: StateFlow<Boolean> = _hasSensorData.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _processingResult = MutableStateFlow<String?>(null)
    val processingResult: StateFlow<String?> = _processingResult.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Sensor data
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var sensorData = FloatArray(12) { 0f } // 3 axes for accelerometer, 3 for gyroscope, 6 for derived features

    init {
        // Initialize sensor manager
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    /**
     * Prepare image capture by creating a file and URI
     */
    fun prepareImageCapture(): Uri? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_${timeStamp}_"
            val storageDir = context.getExternalFilesDir("Pictures")
            val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )

            _imageUri.value = uri
            uri
        } catch (e: Exception) {
            Log.e(TAG, "Error creating image file: ${e.message}")
            _errorMessage.value = "Error creating image file: ${e.message}"
            null
        }
    }

    /**
     * Called when image is captured
     */
    fun onImageCaptured() {
        Log.d(TAG, "Image captured: ${_imageUri.value}")
    }

    /**
     * Capture image
     */
    fun captureImage() {
        prepareImageCapture()
    }

    /**
     * Set notes
     */
    fun setNotes(text: String) {
        _notes.value = text
    }

    /**
     * Collect sensor data
     */
    fun collectSensorData() {
        try {
            // Register sensor listeners
            accelerometer?.let {
                sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            }

            gyroscope?.let {
                sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            }

            // Set flag to true after a short delay to allow sensors to collect data
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    Thread.sleep(1000) // Collect data for 1 second

                    // Unregister listeners
                    sensorManager?.unregisterListener(this@MultiModalLearningViewModel)

                    // Set flag
                    _hasSensorData.value = true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error collecting sensor data: ${e.message}")
            _errorMessage.value = "Error collecting sensor data: ${e.message}"
        }
    }

    /**
     * Process multi-modal data
     */
    suspend fun processMultiModalData() {
        _isProcessing.value = true
        _processingResult.value = null
        _errorMessage.value = null

        try {
            // Create a sample habit and completion for demonstration
            val habit = Habit(
                id = UUID.randomUUID().toString(),
                name = "Sample Habit",
                description = "This is a sample habit for multi-modal learning",
                frequency = HabitFrequency.DAILY,
                streak = 5,
                goal = 10,
                goalProgress = 7
            )

            val completion = HabitCompletion(
                id = UUID.randomUUID().toString(),
                habitId = habit.id,
                completionDate = System.currentTimeMillis(),
                note = _notes.value
            )

            // Process multi-modal data
            val result = multiModalLearning.processMultiModalData(
                habit = habit,
                completion = completion,
                imageUri = _imageUri.value,
                notes = _notes.value,
                sensorData = if (_hasSensorData.value) sensorData else null
            )

            // Generate result message
            val resultMessage = buildString {
                appendLine("Multi-modal processing complete!")
                appendLine()
                appendLine("Feature dimensions:")
                result.imageFeatures?.let { imageFeatures ->
                    appendLine("- Image features: ${imageFeatures.size} dimensions")
                }
                result.textFeatures?.let { textFeatures ->
                    appendLine("- Text features: ${textFeatures.size} dimensions")
                }
                result.sensorFeatures?.let { sensorFeatures ->
                    appendLine("- Sensor features: ${sensorFeatures.size} dimensions")
                }
                appendLine("- Fused features: ${result.fusedFeatures.size} dimensions")
                appendLine()
                appendLine("The system has learned from your multi-modal data and will use these insights to provide better recommendations for your habits.")
            }

            _processingResult.value = resultMessage
        } catch (e: Exception) {
            Log.e(TAG, "Error processing multi-modal data: ${e.message}")
            _errorMessage.value = "Error processing multi-modal data: ${e.message}"
        } finally {
            _isProcessing.value = false
        }
    }

    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Handle sensor event
     */
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    // Store accelerometer data
                    sensorData[0] = it.values[0] // X-axis
                    sensorData[1] = it.values[1] // Y-axis
                    sensorData[2] = it.values[2] // Z-axis

                    // Calculate magnitude (derived feature)
                    val magnitude = Math.sqrt(
                        (it.values[0] * it.values[0] +
                         it.values[1] * it.values[1] +
                         it.values[2] * it.values[2]).toDouble()
                    ).toFloat()
                    sensorData[6] = magnitude
                }
                Sensor.TYPE_GYROSCOPE -> {
                    // Store gyroscope data
                    sensorData[3] = it.values[0] // X-axis rotation
                    sensorData[4] = it.values[1] // Y-axis rotation
                    sensorData[5] = it.values[2] // Z-axis rotation

                    // Calculate rotation magnitude (derived feature)
                    val rotationMagnitude = Math.sqrt(
                        (it.values[0] * it.values[0] +
                         it.values[1] * it.values[1] +
                         it.values[2] * it.values[2]).toDouble()
                    ).toFloat()
                    sensorData[7] = rotationMagnitude
                }
            }
        }
    }

    /**
     * Handle sensor accuracy change
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    /**
     * Clean up resources
     */
    override fun onCleared() {
        super.onCleared()
        sensorManager?.unregisterListener(this)
    }
}
