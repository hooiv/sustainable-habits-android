package com.hooiv.habitflow.core.data.model

/**
 * Data class for AI Assistant personalization settings.
 */
data class AIAssistantPersonalization(
    // General settings
    val useStreaming: Boolean = true,

    // Appearance settings
    val showAnimations: Boolean = true,
    val darkTheme: Boolean = false,

    // Privacy settings
    val saveConversationHistory: Boolean = true,
    val shareHabitData: Boolean = true
)
