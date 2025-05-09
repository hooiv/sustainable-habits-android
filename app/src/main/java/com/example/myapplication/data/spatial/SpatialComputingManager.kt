package com.example.myapplication.data.spatial

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Manages spatial computing features including AR, spatial mapping, and gesture recognition
 */
@Singleton
class SpatialComputingManager @Inject constructor(
    private val context: Context
) : SensorEventListener {
    companion object {
        private const val TAG = "SpatialComputing"
        
        // Constants for spatial mapping
        private const val MAX_SPATIAL_ANCHORS = 20
        private const val MAX_VIRTUAL_OBJECTS = 50
        
        // Constants for gesture recognition
        private const val GESTURE_THRESHOLD = 2.0f
        private const val GESTURE_HISTORY_SIZE = 20
    }
    
    // Sensor manager
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    
    // Sensors for spatial tracking
    private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    
    // Spatial data
    private val _devicePosition = MutableStateFlow(Position(0f, 0f, 0f))
    val devicePosition: StateFlow<Position> = _devicePosition.asStateFlow()
    
    private val _deviceOrientation = MutableStateFlow(Orientation(0f, 0f, 0f))
    val deviceOrientation: StateFlow<Orientation> = _deviceOrientation.asStateFlow()
    
    // Spatial anchors (fixed points in the environment)
    private val _spatialAnchors = MutableStateFlow<List<SpatialAnchor>>(emptyList())
    val spatialAnchors: StateFlow<List<SpatialAnchor>> = _spatialAnchors.asStateFlow()
    
    // Virtual objects (placed in the environment)
    private val _virtualObjects = MutableStateFlow<List<VirtualObject>>(emptyList())
    val virtualObjects: StateFlow<List<VirtualObject>> = _virtualObjects.asStateFlow()
    
    // Recognized gestures
    private val _recognizedGesture = MutableStateFlow<Gesture?>(null)
    val recognizedGesture: StateFlow<Gesture?> = _recognizedGesture.asStateFlow()
    
    // Spatial mapping state
    private val _isMappingActive = MutableStateFlow(false)
    val isMappingActive: StateFlow<Boolean> = _isMappingActive.asStateFlow()
    
    // Sensor data
    private val accelerometerData = FloatArray(3)
    private val gyroscopeData = FloatArray(3)
    private val rotationVectorData = FloatArray(5)
    private val magneticFieldData = FloatArray(3)
    
    // Rotation matrix and orientation values
    private val rotationMatrix = FloatArray(9)
    private val orientationValues = FloatArray(3)
    
    // Gesture history
    private val gestureHistory = mutableListOf<FloatArray>()
    
    /**
     * Start spatial tracking
     */
    fun startSpatialTracking() {
        // Register sensor listeners
        accelerometerSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        
        gyroscopeSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        
        rotationVectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        
        magneticFieldSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        
        Log.d(TAG, "Started spatial tracking")
    }
    
    /**
     * Stop spatial tracking
     */
    fun stopSpatialTracking() {
        sensorManager.unregisterListener(this)
        Log.d(TAG, "Stopped spatial tracking")
    }
    
    /**
     * Start spatial mapping
     */
    fun startSpatialMapping() {
        _isMappingActive.value = true
        Log.d(TAG, "Started spatial mapping")
    }
    
    /**
     * Stop spatial mapping
     */
    fun stopSpatialMapping() {
        _isMappingActive.value = false
        Log.d(TAG, "Stopped spatial mapping")
    }
    
    /**
     * Handle sensor events
     */
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, accelerometerData, 0, 3)
                updatePosition()
                detectGesture()
            }
            Sensor.TYPE_GYROSCOPE -> {
                System.arraycopy(event.values, 0, gyroscopeData, 0, 3)
            }
            Sensor.TYPE_ROTATION_VECTOR -> {
                System.arraycopy(event.values, 0, rotationVectorData, 0, event.values.size)
                updateOrientation()
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, magneticFieldData, 0, 3)
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not used
    }
    
    /**
     * Update device position based on sensor data
     */
    private fun updatePosition() {
        // In a real app, this would use sensor fusion and SLAM techniques
        // For this demo, we'll use a simple approach based on accelerometer data
        
        // Simulate position updates
        val currentPosition = _devicePosition.value
        
        // Apply a simple physics model
        val dt = 0.1f // Time step in seconds
        
        // Remove gravity component (very simplified)
        val linearAccel = FloatArray(3)
        linearAccel[0] = accelerometerData[0]
        linearAccel[1] = accelerometerData[1] - 9.8f
        linearAccel[2] = accelerometerData[2]
        
        // Apply rotation to get acceleration in world coordinates
        val worldAccel = rotateVector(linearAccel, _deviceOrientation.value)
        
        // Update position using simple physics (position += velocity * dt + 0.5 * acceleration * dt^2)
        val newX = currentPosition.x + worldAccel[0] * 0.5f * dt * dt
        val newY = currentPosition.y + worldAccel[1] * 0.5f * dt * dt
        val newZ = currentPosition.z + worldAccel[2] * 0.5f * dt * dt
        
        _devicePosition.value = Position(newX, newY, newZ)
        
        // If mapping is active, create spatial anchors occasionally
        if (_isMappingActive.value && Math.random() < 0.01) {
            createSpatialAnchor()
        }
    }
    
    /**
     * Update device orientation based on sensor data
     */
    private fun updateOrientation() {
        // Convert rotation vector to orientation angles
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVectorData)
        SensorManager.getOrientation(rotationMatrix, orientationValues)
        
        // Convert radians to degrees
        val azimuth = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
        val pitch = Math.toDegrees(orientationValues[1].toDouble()).toFloat()
        val roll = Math.toDegrees(orientationValues[2].toDouble()).toFloat()
        
        _deviceOrientation.value = Orientation(azimuth, pitch, roll)
    }
    
    /**
     * Rotate a vector based on device orientation
     */
    private fun rotateVector(vector: FloatArray, orientation: Orientation): FloatArray {
        // Convert degrees to radians
        val azimuth = Math.toRadians(orientation.azimuth.toDouble())
        val pitch = Math.toRadians(orientation.pitch.toDouble())
        val roll = Math.toRadians(orientation.roll.toDouble())
        
        // Create rotation matrices
        val rotX = floatArrayOf(
            1f, 0f, 0f,
            0f, cos(pitch).toFloat(), -sin(pitch).toFloat(),
            0f, sin(pitch).toFloat(), cos(pitch).toFloat()
        )
        
        val rotY = floatArrayOf(
            cos(roll).toFloat(), 0f, sin(roll).toFloat(),
            0f, 1f, 0f,
            -sin(roll).toFloat(), 0f, cos(roll).toFloat()
        )
        
        val rotZ = floatArrayOf(
            cos(azimuth).toFloat(), -sin(azimuth).toFloat(), 0f,
            sin(azimuth).toFloat(), cos(azimuth).toFloat(), 0f,
            0f, 0f, 1f
        )
        
        // Apply rotations
        val temp = multiplyMatrixVector(rotX, vector)
        val temp2 = multiplyMatrixVector(rotY, temp)
        return multiplyMatrixVector(rotZ, temp2)
    }
    
    /**
     * Multiply a 3x3 matrix by a 3D vector
     */
    private fun multiplyMatrixVector(matrix: FloatArray, vector: FloatArray): FloatArray {
        val result = FloatArray(3)
        
        for (i in 0 until 3) {
            result[i] = 0f
            for (j in 0 until 3) {
                result[i] += matrix[i * 3 + j] * vector[j]
            }
        }
        
        return result
    }
    
    /**
     * Detect gestures based on accelerometer data
     */
    private fun detectGesture() {
        // Add current accelerometer data to history
        gestureHistory.add(accelerometerData.clone())
        if (gestureHistory.size > GESTURE_HISTORY_SIZE) {
            gestureHistory.removeAt(0)
        }
        
        // Need enough history to detect gestures
        if (gestureHistory.size < GESTURE_HISTORY_SIZE) return
        
        // Calculate acceleration magnitude over time
        val magnitudes = gestureHistory.map { data ->
            sqrt(data[0] * data[0] + data[1] * data[1] + data[2] * data[2])
        }
        
        // Detect swipe gesture (simplified)
        val xDiff = gestureHistory.last()[0] - gestureHistory.first()[0]
        val yDiff = gestureHistory.last()[1] - gestureHistory.first()[1]
        val zDiff = gestureHistory.last()[2] - gestureHistory.first()[2]
        
        // Check for significant movement
        if (Math.abs(xDiff) > GESTURE_THRESHOLD) {
            val gesture = if (xDiff > 0) Gesture.SWIPE_RIGHT else Gesture.SWIPE_LEFT
            _recognizedGesture.value = gesture
        } else if (Math.abs(yDiff) > GESTURE_THRESHOLD) {
            val gesture = if (yDiff > 0) Gesture.SWIPE_UP else Gesture.SWIPE_DOWN
            _recognizedGesture.value = gesture
        } else if (Math.abs(zDiff) > GESTURE_THRESHOLD) {
            val gesture = if (zDiff > 0) Gesture.PUSH else Gesture.PULL
            _recognizedGesture.value = gesture
        } else {
            // Check for shake gesture
            val variability = magnitudes.zipWithNext { a, b -> Math.abs(a - b) }.average()
            if (variability > GESTURE_THRESHOLD) {
                _recognizedGesture.value = Gesture.SHAKE
            } else {
                _recognizedGesture.value = null
            }
        }
    }
    
    /**
     * Create a spatial anchor at the current position
     */
    private fun createSpatialAnchor() {
        val currentAnchors = _spatialAnchors.value
        
        // Limit the number of anchors
        if (currentAnchors.size >= MAX_SPATIAL_ANCHORS) return
        
        // Create a new anchor with a small random offset from current position
        val position = _devicePosition.value
        val offsetX = (Math.random() * 2 - 1).toFloat() * 0.5f
        val offsetY = (Math.random() * 2 - 1).toFloat() * 0.5f
        val offsetZ = (Math.random() * 2 - 1).toFloat() * 0.5f
        
        val anchor = SpatialAnchor(
            id = UUID.randomUUID().toString(),
            position = Position(
                position.x + offsetX,
                position.y + offsetY,
                position.z + offsetZ
            ),
            confidence = (Math.random() * 0.5 + 0.5).toFloat()
        )
        
        _spatialAnchors.value = currentAnchors + anchor
        Log.d(TAG, "Created spatial anchor: $anchor")
    }
    
    /**
     * Place a virtual object in the environment
     */
    fun placeVirtualObject(type: VirtualObjectType, position: Position? = null, anchorId: String? = null): String? {
        val currentObjects = _virtualObjects.value
        
        // Limit the number of objects
        if (currentObjects.size >= MAX_VIRTUAL_OBJECTS) return null
        
        // Determine position for the object
        val objectPosition = when {
            position != null -> position
            anchorId != null -> {
                val anchor = _spatialAnchors.value.find { it.id == anchorId }
                anchor?.position ?: _devicePosition.value
            }
            else -> _devicePosition.value
        }
        
        // Create virtual object
        val objectId = UUID.randomUUID().toString()
        val virtualObject = VirtualObject(
            id = objectId,
            type = type,
            position = objectPosition,
            orientation = _deviceOrientation.value,
            scale = 1.0f,
            anchorId = anchorId
        )
        
        _virtualObjects.value = currentObjects + virtualObject
        Log.d(TAG, "Placed virtual object: $virtualObject")
        
        return objectId
    }
    
    /**
     * Remove a virtual object
     */
    fun removeVirtualObject(objectId: String): Boolean {
        val currentObjects = _virtualObjects.value
        val objectToRemove = currentObjects.find { it.id == objectId } ?: return false
        
        _virtualObjects.value = currentObjects - objectToRemove
        Log.d(TAG, "Removed virtual object: $objectToRemove")
        
        return true
    }
    
    /**
     * Get virtual objects near a position
     */
    fun getVirtualObjectsNear(position: Position, radius: Float): List<VirtualObject> {
        return _virtualObjects.value.filter { obj ->
            val distance = calculateDistance(position, obj.position)
            distance <= radius
        }
    }
    
    /**
     * Calculate distance between two positions
     */
    private fun calculateDistance(pos1: Position, pos2: Position): Float {
        val dx = pos1.x - pos2.x
        val dy = pos1.y - pos2.y
        val dz = pos1.z - pos2.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
    
    /**
     * Convert screen position to world position
     */
    fun screenToWorldPosition(screenPosition: Offset, depth: Float): Position {
        // In a real app, this would use proper ray casting
        // For this demo, we'll use a simplified approach
        
        val devicePos = _devicePosition.value
        val deviceOri = _deviceOrientation.value
        
        // Convert screen position to normalized device coordinates (-1 to 1)
        val ndcX = screenPosition.x * 2 - 1
        val ndcY = 1 - screenPosition.y * 2
        
        // Create a ray direction based on device orientation
        val rayDir = FloatArray(3)
        rayDir[0] = ndcX
        rayDir[1] = ndcY
        rayDir[2] = -1f // Forward direction
        
        // Rotate ray direction based on device orientation
        val worldRayDir = rotateVector(rayDir, deviceOri)
        
        // Calculate world position
        return Position(
            devicePos.x + worldRayDir[0] * depth,
            devicePos.y + worldRayDir[1] * depth,
            devicePos.z + worldRayDir[2] * depth
        )
    }
}

/**
 * 3D position
 */
data class Position(
    val x: Float,
    val y: Float,
    val z: Float
)

/**
 * 3D orientation (Euler angles in degrees)
 */
data class Orientation(
    val azimuth: Float, // Rotation around Z axis
    val pitch: Float,   // Rotation around X axis
    val roll: Float     // Rotation around Y axis
)

/**
 * Spatial anchor (fixed point in the environment)
 */
data class SpatialAnchor(
    val id: String,
    val position: Position,
    val confidence: Float // 0-1 scale
)

/**
 * Virtual object placed in the environment
 */
data class VirtualObject(
    val id: String,
    val type: VirtualObjectType,
    val position: Position,
    val orientation: Orientation,
    val scale: Float,
    val anchorId: String? = null
)

/**
 * Types of virtual objects
 */
enum class VirtualObjectType {
    HABIT_MARKER,
    COMPLETION_INDICATOR,
    STREAK_VISUALIZATION,
    GOAL_INDICATOR,
    MOTIVATION_OBJECT
}

/**
 * Recognized gestures
 */
enum class Gesture {
    SWIPE_LEFT,
    SWIPE_RIGHT,
    SWIPE_UP,
    SWIPE_DOWN,
    PUSH,
    PULL,
    SHAKE
}
