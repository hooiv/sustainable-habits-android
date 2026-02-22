package com.example.myapplication.core.data.model

/**
 * Data class for AI context that bundles habit data passed to the AI service.
 */
data class AIContext(
    val habits: List<Habit> = emptyList(),
    val completions: List<HabitCompletion> = emptyList(),
    val personalization: AIAssistantPersonalization = AIAssistantPersonalization()
)

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
