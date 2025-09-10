package com.example.myapplication.core.data.model

import java.util.UUID

/**
 * Enum for biometric types
 */
enum class BiometricType {
    HEART_RATE,
    SLEEP_QUALITY,
    STRESS_LEVEL,
    ENERGY_LEVEL,
    MOOD,
    BLOOD_PRESSURE,
    BLOOD_PRESSURE_SYSTOLIC,
    BLOOD_PRESSURE_DIASTOLIC,
    BLOOD_OXYGEN,
    BODY_TEMPERATURE,
    RESPIRATORY_RATE,
    STEPS,
    STEP_COUNT,
    FOCUS_LEVEL,
    CALORIES_BURNED
}

/**
 * Enum for biometric trends
 */
enum class BiometricTrend {
    INCREASING,
    DECREASING,
    STABLE,
    FLUCTUATING
}

/**
 * Data class for biometric readings
 */
data class BiometricReading(
    val id: String = UUID.randomUUID().toString(),
    val type: BiometricType,
    val value: Float,
    val unit: String,
    val normalRange: Pair<Float, Float>,
    val trend: BiometricTrend = BiometricTrend.STABLE,
    val relatedHabitIds: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
