package com.example.myapplication.core.network.ml

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
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Collects contextual features for neural network training
 * Uses only on-device sensors and free APIs
 */
@Singleton
class ContextFeatureCollector @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorEventListener, LocationListener {
    companion object {
        private const val TAG = "ContextFeatures"
        private const val LOCATION_UPDATE_INTERVAL = 30 * 60 * 1000L // 30 minutes
        private const val LOCATION_UPDATE_DISTANCE = 500f // 500 meters
        
        // Feature indices
        const val FEATURE_TIME_OF_DAY = 0
        const val FEATURE_DAY_OF_WEEK = 1
        const val FEATURE_ACTIVITY_LEVEL = 2
        const val FEATURE_LIGHT_LEVEL = 3
        const val FEATURE_TEMPERATURE = 4
        const val FEATURE_WEATHER_CONDITION = 5
        const val FEATURE_LOCATION_HOME = 6
        const val FEATURE_LOCATION_WORK = 7
        const val FEATURE_BATTERY_LEVEL = 8
        const val FEATURE_DEVICE_USAGE = 9
    }
    
    private val sensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    
    private val locationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    
    // Context feature values
    private val _contextFeatures = MutableStateFlow(FloatArray(10) { 0f })
    val contextFeatures: StateFlow<FloatArray> = _contextFeatures.asStateFlow()
    
    // Location data
    private var currentLocation: Location? = null
    private var homeLocation: Location? = null
    private var workLocation: Location? = null
    
    // Sensor data
    private var lightLevel = 0f
    private var temperature = 0f
    private var activityLevel = 0f
    
    // Weather data (simplified)
    private var weatherCondition = 0f // 0 = unknown, 0.25 = bad, 0.5 = neutral, 0.75 = good, 1 = excellent
    
    // Device usage
    private var deviceUsage = 0f
    
    // Battery level
    private var batteryLevel = 0f
    
    /**
     * Start collecting context features
     */
    fun startCollecting() {
        try {
            // Register sensors
            val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
            if (lightSensor != null) {
                sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
            
            val temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
            if (temperatureSensor != null) {
                sensorManager.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
            
            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
            }
            
            // Register location updates if permission is granted
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                @SuppressLint("MissingPermission")
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_INTERVAL,
                    LOCATION_UPDATE_DISTANCE,
                    this
                )
            }
            
            // Start periodic updates
            updateTimeFeatures()
            updateDeviceUsage()
            updateBatteryLevel()
            
            Log.d(TAG, "Started collecting context features")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting context collection: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Stop collecting context features
     */
    fun stopCollecting() {
        try {
            sensorManager.unregisterListener(this)
            locationManager.removeUpdates(this)
            Log.d(TAG, "Stopped collecting context features")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping context collection: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Update time-based features
     */
    private fun updateTimeFeatures() {
        val calendar = Calendar.getInstance()
        
        // Time of day (0-1 scale, 0 = midnight, 0.5 = noon, 1 = midnight)
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minuteOfHour = calendar.get(Calendar.MINUTE)
        val timeOfDay = (hourOfDay + minuteOfHour / 60.0f) / 24.0f
        
        // Day of week (0-1 scale, 0 = Monday, 1 = Sunday)
        val dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY) / 6.0f
        
        // Update features
        val features = _contextFeatures.value.clone()
        features[FEATURE_TIME_OF_DAY] = timeOfDay
        features[FEATURE_DAY_OF_WEEK] = dayOfWeek
        _contextFeatures.value = features
    }
    
    /**
     * Update device usage statistics
     */
    private fun updateDeviceUsage() {
        // This is a simplified implementation
        // In a real app, you would use UsageStatsManager to get actual usage data
        // For now, we'll use a random value that changes throughout the day
        
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        
        // Simulate higher usage during waking hours
        deviceUsage = when {
            hourOfDay < 6 -> 0.1f // Night (low usage)
            hourOfDay < 9 -> 0.6f // Morning (medium-high usage)
            hourOfDay < 17 -> 0.8f // Day (high usage)
            hourOfDay < 23 -> 0.7f // Evening (medium-high usage)
            else -> 0.3f // Late night (low-medium usage)
        }
        
        // Add some randomness
        deviceUsage += (Random().nextFloat() - 0.5f) * 0.2f
        deviceUsage = deviceUsage.coerceIn(0f, 1f)
        
        // Update feature
        val features = _contextFeatures.value.clone()
        features[FEATURE_DEVICE_USAGE] = deviceUsage
        _contextFeatures.value = features
    }
    
    /**
     * Update battery level
     */
    private fun updateBatteryLevel() {
        try {
            // This is a simplified implementation
            // In a real app, you would register a BroadcastReceiver for ACTION_BATTERY_CHANGED
            // For now, we'll use a random value that decreases throughout the day
            
            val calendar = Calendar.getInstance()
            val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
            
            // Simulate battery drain throughout the day
            batteryLevel = 1.0f - (hourOfDay / 24.0f)
            
            // Add some randomness
            batteryLevel += (Random().nextFloat() - 0.5f) * 0.1f
            batteryLevel = batteryLevel.coerceIn(0f, 1f)
            
            // Update feature
            val features = _contextFeatures.value.clone()
            features[FEATURE_BATTERY_LEVEL] = batteryLevel
            _contextFeatures.value = features
        } catch (e: Exception) {
            Log.e(TAG, "Error updating battery level: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Set home location
     */
    fun setHomeLocation(location: Location) {
        homeLocation = location
        updateLocationFeatures()
    }
    
    /**
     * Set work location
     */
    fun setWorkLocation(location: Location) {
        workLocation = location
        updateLocationFeatures()
    }
    
    /**
     * Update location-based features
     */
    private fun updateLocationFeatures() {
        if (currentLocation == null) {
            return
        }
        
        val features = _contextFeatures.value.clone()
        
        // Calculate distance to home (if set)
        if (homeLocation != null) {
            val distanceToHome = currentLocation!!.distanceTo(homeLocation!!)
            // Convert to 0-1 scale (0 = at home, 1 = far from home)
            features[FEATURE_LOCATION_HOME] = (1.0f - (distanceToHome / 1000.0f).coerceIn(0f, 1f))
        }
        
        // Calculate distance to work (if set)
        if (workLocation != null) {
            val distanceToWork = currentLocation!!.distanceTo(workLocation!!)
            // Convert to 0-1 scale (0 = at work, 1 = far from work)
            features[FEATURE_LOCATION_WORK] = (1.0f - (distanceToWork / 1000.0f).coerceIn(0f, 1f))
        }
        
        _contextFeatures.value = features
    }
    
    /**
     * Update weather condition
     * In a real app, you would use a weather API
     * For this implementation, we'll simulate weather based on time and temperature
     */
    private fun updateWeatherCondition() {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        
        // Simulate seasonal variations
        val seasonalFactor = when (month) {
            in 2..4 -> 0.7f // Spring
            in 5..7 -> 0.9f // Summer
            in 8..10 -> 0.6f // Fall
            else -> 0.3f // Winter
        }
        
        // Factor in temperature
        val temperatureFactor = when {
            temperature < 0 -> 0.2f // Very cold
            temperature < 10 -> 0.4f // Cold
            temperature < 20 -> 0.7f // Mild
            temperature < 30 -> 0.9f // Warm
            else -> 0.5f // Hot
        }
        
        // Calculate weather condition
        weatherCondition = (seasonalFactor * 0.6f + temperatureFactor * 0.4f).coerceIn(0f, 1f)
        
        // Update feature
        val features = _contextFeatures.value.clone()
        features[FEATURE_WEATHER_CONDITION] = weatherCondition
        _contextFeatures.value = features
    }
    
    // SensorEventListener implementation
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_LIGHT -> {
                lightLevel = event.values[0] / 10000f // Normalize to 0-1
                lightLevel = lightLevel.coerceIn(0f, 1f)
                
                val features = _contextFeatures.value.clone()
                features[FEATURE_LIGHT_LEVEL] = lightLevel
                _contextFeatures.value = features
            }
            Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                temperature = event.values[0]
                
                // Normalize temperature to 0-1 scale (assuming range -20 to 40 degrees Celsius)
                val normalizedTemp = ((temperature + 20) / 60).coerceIn(0f, 1f)
                
                val features = _contextFeatures.value.clone()
                features[FEATURE_TEMPERATURE] = normalizedTemp
                _contextFeatures.value = features
                
                // Update weather condition based on temperature
                updateWeatherCondition()
            }
            Sensor.TYPE_ACCELEROMETER -> {
                // Calculate activity level based on accelerometer data
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                
                // Calculate magnitude of acceleration
                val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                
                // Normalize to 0-1 scale (assuming range 0-20)
                activityLevel = (magnitude / 20f).coerceIn(0f, 1f)
                
                val features = _contextFeatures.value.clone()
                features[FEATURE_ACTIVITY_LEVEL] = activityLevel
                _contextFeatures.value = features
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not used
    }
    
    // LocationListener implementation
    override fun onLocationChanged(location: Location) {
        currentLocation = location
        updateLocationFeatures()
    }
    
    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        // Not used
    }
    
    override fun onProviderEnabled(provider: String) {
        // Not used
    }
    
    override fun onProviderDisabled(provider: String) {
        // Not used
    }
}
