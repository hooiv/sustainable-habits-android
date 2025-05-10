package com.example.myapplication.data.model

import java.util.UUID

/**
 * Enum for voice command intents
 */
enum class VoiceIntent {
    CREATE_HABIT,
    COMPLETE_HABIT,
    VIEW_HABIT,
    VIEW_STATS,
    SET_REMINDER,
    UNKNOWN
}

/**
 * Enum for entity types in voice commands
 */
enum class EntityType {
    HABIT_NAME,
    FREQUENCY,
    TIME,
    DATE,
    CATEGORY,
    DURATION,
    QUANTITY
}

/**
 * Data class for voice entities
 */
data class VoiceEntity(
    val id: String = UUID.randomUUID().toString(),
    val type: EntityType,
    val value: String,
    val confidence: Float, // 0.0 to 1.0
    val startIndex: Int = 0,
    val endIndex: Int = 0
)

/**
 * Data class for voice commands
 */
data class VoiceCommand(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val intent: VoiceIntent,
    val confidence: Float, // 0.0 to 1.0
    val entities: List<VoiceEntity> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
