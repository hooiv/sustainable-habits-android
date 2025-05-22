package com.example.myapplication.data.model

/**
 * Represents a voice command
 */
data class VoiceCommand(
    val text: String,
    val intent: VoiceIntent,
    val entities: List<VoiceEntity>,
    val confidence: Float,
    val timestamp: Long
)

/**
 * Represents a voice entity
 */
data class VoiceEntity(
    val type: EntityType,
    val value: String,
    val confidence: Float
)

/**
 * Types of voice intents
 */
enum class VoiceIntent {
    CREATE_HABIT,
    COMPLETE_HABIT,
    VIEW_HABIT,
    VIEW_STATS,
    SET_REMINDER,
    CHECK_PROGRESS,
    UNKNOWN
}

/**
 * Types of entities
 */
enum class EntityType {
    HABIT_NAME,
    FREQUENCY,
    TIME,
    DATE,
    DURATION,
    UNKNOWN
}
