package com.example.myapplication.core.network.spatial

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.compose.ui.geometry.Offset
import com.example.myapplication.core.data.model.Habit
import com.example.myapplication.core.data.model.HabitCompletion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * Implements spatial computing features for habit visualization in AR
 */
@Singleton
class SpatialComputing @Inject constructor(
    private val context: Context
) : SensorEventListener, LocationListener {
    companion object {
        private const val TAG = "SpatialComputing"

        // Constants for spatial mapping
        private const val MAX_SPATIAL_OBJECTS = 50
        private const val LOCATION_UPDATE_DISTANCE = 5f // meters
        private const val LOCATION_UPDATE_TIME = 10000L // milliseconds

        // Earth radius in meters
        private const val EARTH_RADIUS = 6371000.0
    }

    // Sensor manager
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Location manager
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    // Sensors for spatial tracking
    private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    // Spatial data
    private val _devicePosition = MutableStateFlow(SpatialPosition(0.0, 0.0, 0.0))
    val devicePosition: StateFlow<SpatialPosition> = _devicePosition.asStateFlow()

    private val _deviceOrientation = MutableStateFlow(SpatialOrientation(0f, 0f, 0f))
    val deviceOrientation: StateFlow<SpatialOrientation> = _deviceOrientation.asStateFlow()

    private val _deviceLocation = MutableStateFlow<Location?>(null)
    val deviceLocation: StateFlow<Location?> = _deviceLocation.asStateFlow()

    // Spatial objects (habits, completions, etc. in AR space)
    private val _spatialObjects = MutableStateFlow<List<SpatialObject>>(emptyList())
    val spatialObjects: StateFlow<List<SpatialObject>> = _spatialObjects.asStateFlow()

    // Spatial mapping state
    private val _isSpatialTrackingActive = MutableStateFlow(false)
    val isSpatialTrackingActive: StateFlow<Boolean> = _isSpatialTrackingActive.asStateFlow()

    // Sensor data
    private val accelerometerData = FloatArray(3)
    private val gyroscopeData = FloatArray(3)
    private val rotationVectorData = FloatArray(5)
    private val magneticFieldData = FloatArray(3)

    // Rotation matrix and orientation values
    private val rotationMatrix = FloatArray(9)
    private val orientationValues = FloatArray(3)

    /**
     * Start spatial tracking
     */
    fun startSpatialTracking() {
        if (_isSpatialTrackingActive.value) return

        _isSpatialTrackingActive.value = true

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

        // Start location updates
        try {
            @SuppressLint("MissingPermission")
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                LOCATION_UPDATE_TIME,
                LOCATION_UPDATE_DISTANCE,
                this
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission not granted: ${e.message}")
        }

        Log.d(TAG, "Started spatial tracking")
    }

    /**
     * Stop spatial tracking
     */
    fun stopSpatialTracking() {
        if (!_isSpatialTrackingActive.value) return

        _isSpatialTrackingActive.value = false

        // Unregister sensor listeners
        sensorManager.unregisterListener(this)

        // Stop location updates
        locationManager.removeUpdates(this)

        Log.d(TAG, "Stopped spatial tracking")
    }

    /**
     * Handle sensor events
     */
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, accelerometerData, 0, 3)
                updatePosition()
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
     * Handle location updates
     */
    override fun onLocationChanged(location: Location) {
        _deviceLocation.value = location
        Log.d(TAG, "Location updated: ${location.latitude}, ${location.longitude}")
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // Not used
    }

    override fun onProviderEnabled(provider: String) {
        // Not used
    }

    override fun onProviderDisabled(provider: String) {
        // Not used
    }

    /**
     * Update device position based on sensor data
     */
    private fun updatePosition() {
        // In a real AR app, this would use ARCore or ARKit
        // For this demo, we'll use a simple approach based on accelerometer data

        // Simulate position updates
        val currentPosition = _devicePosition.value

        // Apply a simple physics model
        val dt = 0.1 // Time step in seconds

        // Remove gravity component (very simplified)
        val linearAccel = doubleArrayOf(
            accelerometerData[0].toDouble(),
            accelerometerData[1].toDouble() - 9.8,
            accelerometerData[2].toDouble()
        )

        // Apply rotation to get acceleration in world coordinates
        val worldAccel = rotateVector(linearAccel, _deviceOrientation.value)

        // Update position using simple physics (position += 0.5 * acceleration * dt^2)
        val newX = currentPosition.x + worldAccel[0] * 0.5 * dt * dt
        val newY = currentPosition.y + worldAccel[1] * 0.5 * dt * dt
        val newZ = currentPosition.z + worldAccel[2] * 0.5 * dt * dt

        _devicePosition.value = SpatialPosition(newX, newY, newZ)
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

        _deviceOrientation.value = SpatialOrientation(azimuth, pitch, roll)
    }

    /**
     * Rotate a vector based on device orientation
     */
    private fun rotateVector(vector: DoubleArray, orientation: SpatialOrientation): DoubleArray {
        // Convert degrees to radians
        val azimuth = Math.toRadians(orientation.azimuth.toDouble())
        val pitch = Math.toRadians(orientation.pitch.toDouble())
        val roll = Math.toRadians(orientation.roll.toDouble())

        // Create rotation matrices
        val rotX = doubleArrayOf(
            1.0, 0.0, 0.0,
            0.0, cos(pitch), -sin(pitch),
            0.0, sin(pitch), cos(pitch)
        )

        val rotY = doubleArrayOf(
            cos(roll), 0.0, sin(roll),
            0.0, 1.0, 0.0,
            -sin(roll), 0.0, cos(roll)
        )

        val rotZ = doubleArrayOf(
            cos(azimuth), -sin(azimuth), 0.0,
            sin(azimuth), cos(azimuth), 0.0,
            0.0, 0.0, 1.0
        )

        // Apply rotations
        val temp = multiplyMatrixVector(rotX, vector)
        val temp2 = multiplyMatrixVector(rotY, temp)
        return multiplyMatrixVector(rotZ, temp2)
    }

    /**
     * Multiply a 3x3 matrix by a 3D vector
     */
    private fun multiplyMatrixVector(matrix: DoubleArray, vector: DoubleArray): DoubleArray {
        val result = DoubleArray(3)

        for (i in 0 until 3) {
            result[i] = 0.0
            for (j in 0 until 3) {
                result[i] += matrix[i * 3 + j] * vector[j]
            }
        }

        return result
    }



    /**
     * Place a habit in AR space
     */
    fun placeHabitInSpace(habit: Habit, position: SpatialPosition? = null): String {
        val currentObjects = _spatialObjects.value

        // Limit the number of objects
        if (currentObjects.size >= MAX_SPATIAL_OBJECTS) {
            // Remove oldest object
            _spatialObjects.value = currentObjects.drop(1)
        }

        // Determine position for the object
        val objectPosition = position ?: _devicePosition.value

        // Create spatial object
        val objectId = UUID.randomUUID().toString()
        val spatialObject = SpatialObject(
            id = objectId,
            type = SpatialObjectType.HABIT,
            position = objectPosition,
            orientation = _deviceOrientation.value,
            scale = 1.0,
            color = getColorForHabit(habit),
            referenceId = habit.id,
            label = habit.name
        )

        _spatialObjects.value = _spatialObjects.value + spatialObject
        Log.d(TAG, "Placed habit in space: ${habit.name}")

        return objectId
    }

    /**
     * Place a habit completion in AR space
     */
    fun placeCompletionInSpace(completion: HabitCompletion, habit: Habit): String {
        val currentObjects = _spatialObjects.value

        // Limit the number of objects
        if (currentObjects.size >= MAX_SPATIAL_OBJECTS) {
            // Remove oldest object
            _spatialObjects.value = currentObjects.drop(1)
        }

        // Find habit object
        val habitObject = currentObjects.find { it.referenceId == habit.id && it.type == SpatialObjectType.HABIT }

        // Determine position for the completion
        val objectPosition = if (habitObject != null) {
            // Place near the habit
            val offset = SpatialPosition(
                x = (Math.random() - 0.5) * 2.0,
                y = (Math.random() - 0.5) * 2.0,
                z = (Math.random() - 0.5) * 2.0
            )
            SpatialPosition(
                x = habitObject.position.x + offset.x,
                y = habitObject.position.y + offset.y,
                z = habitObject.position.z + offset.z
            )
        } else {
            // Place near the device
            val devicePos = _devicePosition.value
            val offset = SpatialPosition(
                x = (Math.random() - 0.5) * 4.0,
                y = (Math.random() - 0.5) * 4.0,
                z = (Math.random() - 0.5) * 4.0
            )
            SpatialPosition(
                x = devicePos.x + offset.x,
                y = devicePos.y + offset.y,
                z = devicePos.z + offset.z
            )
        }

        // Create spatial object
        val objectId = UUID.randomUUID().toString()
        val spatialObject = SpatialObject(
            id = objectId,
            type = SpatialObjectType.COMPLETION,
            position = objectPosition,
            orientation = _deviceOrientation.value,
            scale = 0.7,
            color = getColorForHabit(habit),
            referenceId = completion.id,
            label = "Completed: ${habit.name}"
        )

        _spatialObjects.value = _spatialObjects.value + spatialObject
        Log.d(TAG, "Placed completion in space: ${habit.name}")

        return objectId
    }

    /**
     * Place a streak visualization in AR space
     */
    fun placeStreakVisualization(habit: Habit, streak: Int): String {
        val currentObjects = _spatialObjects.value

        // Find habit object
        val habitObject = currentObjects.find { it.referenceId == habit.id && it.type == SpatialObjectType.HABIT }

        // Determine position for the streak visualization
        val objectPosition = if (habitObject != null) {
            // Place above the habit
            SpatialPosition(
                x = habitObject.position.x,
                y = habitObject.position.y + 2.0,
                z = habitObject.position.z
            )
        } else {
            // Place in front of the device
            val devicePos = _devicePosition.value
            val deviceOri = _deviceOrientation.value

            // Calculate position 3 meters in front of the device
            val angle = Math.toRadians(deviceOri.azimuth.toDouble())
            SpatialPosition(
                x = devicePos.x + 3.0 * sin(angle),
                y = devicePos.y + 1.0,
                z = devicePos.z + 3.0 * cos(angle)
            )
        }

        // Create spatial object
        val objectId = UUID.randomUUID().toString()
        val spatialObject = SpatialObject(
            id = objectId,
            type = SpatialObjectType.STREAK,
            position = objectPosition,
            orientation = _deviceOrientation.value,
            scale = streak / 10.0 + 0.5, // Scale based on streak length
            color = getStreakColor(streak),
            referenceId = habit.id,
            label = "$streak day streak"
        )

        _spatialObjects.value = _spatialObjects.value + spatialObject
        Log.d(TAG, "Placed streak visualization: ${habit.name}, $streak days")

        return objectId
    }

    /**
     * Get objects visible in the current view
     */
    fun getVisibleObjects(fieldOfViewDegrees: Float = 60f): List<SpatialObject> {
        val devicePos = _devicePosition.value
        val deviceOri = _deviceOrientation.value

        // Convert field of view to radians
        val fovRadians = Math.toRadians(fieldOfViewDegrees.toDouble())

        // Calculate view direction vector
        val viewDirection = doubleArrayOf(
            sin(Math.toRadians(deviceOri.azimuth.toDouble())),
            sin(Math.toRadians(deviceOri.pitch.toDouble())),
            cos(Math.toRadians(deviceOri.azimuth.toDouble()))
        )

        // Normalize view direction
        val length = sqrt(viewDirection[0] * viewDirection[0] +
                         viewDirection[1] * viewDirection[1] +
                         viewDirection[2] * viewDirection[2])

        if (length > 0) {
            viewDirection[0] /= length
            viewDirection[1] /= length
            viewDirection[2] /= length
        }

        // Filter objects in the field of view
        return _spatialObjects.value.filter { obj ->
            // Calculate vector from device to object
            val toObject = doubleArrayOf(
                obj.position.x - devicePos.x,
                obj.position.y - devicePos.y,
                obj.position.z - devicePos.z
            )

            // Calculate distance
            val distance = sqrt(toObject[0] * toObject[0] +
                               toObject[1] * toObject[1] +
                               toObject[2] * toObject[2])

            // Normalize
            if (distance > 0) {
                toObject[0] /= distance
                toObject[1] /= distance
                toObject[2] /= distance
            }

            // Calculate dot product (cosine of angle between vectors)
            val dotProduct = viewDirection[0] * toObject[0] +
                            viewDirection[1] * toObject[1] +
                            viewDirection[2] * toObject[2]

            // Check if object is within field of view
            val angleRadians = acos(dotProduct.coerceIn(-1.0, 1.0))
            val isInView = angleRadians <= fovRadians / 2

            // Also check distance - only show objects within 20 meters
            isInView && distance < 20.0
        }
    }

    /**
     * Get the closest object to a screen position
     */
    fun getObjectAtScreenPosition(screenPosition: Offset, maxDistance: Double = 5.0): SpatialObject? {
        val visibleObjects = getVisibleObjects()
        if (visibleObjects.isEmpty()) return null

        // Convert screen position to world ray
        val rayOrigin = _devicePosition.value
        val rayDirection = screenToWorldDirection(screenPosition)

        // Find closest object that intersects with the ray
        var closestObject: SpatialObject? = null
        var closestDistance = Double.MAX_VALUE

        for (obj in visibleObjects) {
            // Calculate vector from ray origin to object
            val toObject = doubleArrayOf(
                obj.position.x - rayOrigin.x,
                obj.position.y - rayOrigin.y,
                obj.position.z - rayOrigin.z
            )

            // Calculate distance along ray
            val dot = toObject[0] * rayDirection[0] +
                     toObject[1] * rayDirection[1] +
                     toObject[2] * rayDirection[2]

            // Skip objects behind the ray
            if (dot < 0) continue

            // Calculate closest point on ray to object
            val closestPoint = doubleArrayOf(
                rayOrigin.x + rayDirection[0] * dot,
                rayOrigin.y + rayDirection[1] * dot,
                rayOrigin.z + rayDirection[2] * dot
            )

            // Calculate distance from closest point to object
            val distance = sqrt(
                (closestPoint[0] - obj.position.x) * (closestPoint[0] - obj.position.x) +
                (closestPoint[1] - obj.position.y) * (closestPoint[1] - obj.position.y) +
                (closestPoint[2] - obj.position.z) * (closestPoint[2] - obj.position.z)
            )

            // Check if this is the closest object within the max distance
            if (distance < maxDistance && distance < closestDistance) {
                closestObject = obj
                closestDistance = distance
            }
        }

        return closestObject
    }

    /**
     * Convert screen position to world direction
     */
    fun screenToWorldDirection(screenPosition: Offset): DoubleArray {
        val deviceOri = _deviceOrientation.value

        // Convert screen position to normalized device coordinates (-1 to 1)
        val ndcX = screenPosition.x * 2 - 1
        val ndcY = 1 - screenPosition.y * 2

        // Calculate azimuth and pitch offsets based on screen position
        // This is a simplified approach - in a real AR app, you would use proper projection matrices
        val azimuthOffset = ndcX * 30.0 // 30 degrees field of view horizontally
        val pitchOffset = ndcY * 20.0   // 20 degrees field of view vertically

        // Calculate final direction angles
        val azimuth = Math.toRadians(deviceOri.azimuth + azimuthOffset)
        val pitch = Math.toRadians(deviceOri.pitch + pitchOffset)

        // Convert to direction vector
        val direction = doubleArrayOf(
            sin(azimuth) * cos(pitch),
            sin(pitch),
            cos(azimuth) * cos(pitch)
        )

        // Normalize
        val length = sqrt(direction[0] * direction[0] + direction[1] * direction[1] + direction[2] * direction[2])
        if (length > 0) {
            direction[0] /= length
            direction[1] /= length
            direction[2] /= length
        }

        return direction
    }

    /**
     * Convert screen position to world position
     */
    fun screenToWorldPosition(screenPosition: Offset, depth: Double): SpatialPosition {
        val devicePos = _devicePosition.value
        val direction = screenToWorldDirection(screenPosition)

        // Calculate position at specified depth
        return SpatialPosition(
            x = devicePos.x + direction[0] * depth,
            y = devicePos.y + direction[1] * depth,
            z = devicePos.z + direction[2] * depth
        )
    }

    /**
     * Move a spatial object
     */
    fun moveObject(objectId: String, newPosition: SpatialPosition): Boolean {
        val objects = _spatialObjects.value
        val objectIndex = objects.indexOfFirst { it.id == objectId }

        if (objectIndex >= 0) {
            val updatedObject = objects[objectIndex].copy(position = newPosition)
            val updatedObjects = objects.toMutableList()
            updatedObjects[objectIndex] = updatedObject
            _spatialObjects.value = updatedObjects
            return true
        }

        return false
    }

    /**
     * Remove a spatial object
     */
    fun removeObject(objectId: String): Boolean {
        val objects = _spatialObjects.value
        val updatedObjects = objects.filter { it.id != objectId }

        if (updatedObjects.size < objects.size) {
            _spatialObjects.value = updatedObjects
            return true
        }

        return false
    }

    /**
     * Convert screen position to world position
     * @deprecated Use screenToWorldPosition instead
     */
    @Deprecated("Use screenToWorldPosition instead", ReplaceWith("screenToWorldPosition(screenPosition, depth)"))
    fun screenToWorldPosition(screenPosition: Offset, depth: Double, oldMethod: Boolean): SpatialPosition {
        val devicePos = _devicePosition.value
        val deviceOri = _deviceOrientation.value

        // Convert screen position to normalized device coordinates (-1 to 1)
        val ndcX = screenPosition.x * 2 - 1
        val ndcY = 1 - screenPosition.y * 2

        // Create a ray direction based on device orientation
        val rayDir = doubleArrayOf(
            ndcX.toDouble(),
            ndcY.toDouble(),
            -1.0 // Forward direction
        )

        // Rotate ray direction based on device orientation
        val worldRayDir = rotateVector(rayDir, deviceOri)

        // Normalize direction
        val length = sqrt(worldRayDir[0] * worldRayDir[0] +
                         worldRayDir[1] * worldRayDir[1] +
                         worldRayDir[2] * worldRayDir[2])

        if (length > 0) {
            worldRayDir[0] /= length
            worldRayDir[1] /= length
            worldRayDir[2] /= length
        }

        // Calculate world position
        return SpatialPosition(
            x = devicePos.x + worldRayDir[0] * depth,
            y = devicePos.y + worldRayDir[1] * depth,
            z = devicePos.z + worldRayDir[2] * depth
        )
    }

    /**
     * Get color for a habit
     */
    private fun getColorForHabit(habit: Habit): Int {
        // Generate a consistent color based on habit ID
        return habit.id.hashCode() and 0xFFFFFF or 0xFF000000.toInt()
    }

    /**
     * Get color for a streak
     */
    private fun getStreakColor(streak: Int): Int {
        return when {
            streak < 3 -> 0xFF4CAF50.toInt() // Green
            streak < 7 -> 0xFF2196F3.toInt() // Blue
            streak < 14 -> 0xFFFF9800.toInt() // Orange
            streak < 30 -> 0xFF9C27B0.toInt() // Purple
            else -> 0xFFFF5722.toInt() // Deep Orange
        }
    }


}

/**
 * 3D position in space
 */
data class SpatialPosition(
    val x: Double,
    val y: Double,
    val z: Double
)

/**
 * 3D orientation (Euler angles in degrees)
 */
data class SpatialOrientation(
    val azimuth: Float, // Rotation around Z axis
    val pitch: Float,   // Rotation around X axis
    val roll: Float     // Rotation around Y axis
)

/**
 * Object in AR space
 */
data class SpatialObject(
    val id: String,
    val type: SpatialObjectType,
    val position: SpatialPosition,
    val orientation: SpatialOrientation,
    val scale: Double,
    val color: Int,
    val referenceId: String, // ID of the referenced habit or completion
    val label: String
)

/**
 * Types of spatial objects
 */
enum class SpatialObjectType {
    HABIT,
    COMPLETION,
    STREAK,
    GOAL,
    MOTIVATION
}
