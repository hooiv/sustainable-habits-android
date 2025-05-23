package com.example.myapplication.data.ai

import com.example.myapplication.features.ai.AISuggestion
import com.example.myapplication.data.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Interface for AI service that provides natural language processing capabilities
 */
interface AIService {
    /**
     * Generate a response to a user question about habits
     *
     * @param question The user's question
     * @param userHabits Optional list of user habits for context
     * @param habitCompletions Optional list of habit completions for additional context
     * @param moodData Optional list of mood entries for emotional context
     * @param locationData Optional list of location data for spatial context
     * @param timePatterns Optional list of time patterns for temporal context
     * @param personalization Optional personalization settings
     * @return AI-generated response
     */
    suspend fun generateResponse(
        question: String,
        userHabits: List<Habit>? = null,
        habitCompletions: List<HabitCompletion>? = null,
        moodData: List<MoodEntry>? = null,
        locationData: List<LocationContext>? = null,
        timePatterns: List<TimePattern>? = null,
        personalization: AIAssistantPersonalization = AIAssistantPersonalization()
    ): String

    /**
     * Generate a streaming response to a user question about habits
     *
     * @param question The user's question
     * @param userHabits Optional list of user habits for context
     * @param habitCompletions Optional list of habit completions for additional context
     * @param moodData Optional list of mood entries for emotional context
     * @param locationData Optional list of location data for spatial context
     * @param timePatterns Optional list of time patterns for temporal context
     * @param personalization Optional personalization settings
     * @return Flow of response chunks as they are generated
     */
    fun generateStreamingResponse(
        question: String,
        userHabits: List<Habit>? = null,
        habitCompletions: List<HabitCompletion>? = null,
        moodData: List<MoodEntry>? = null,
        locationData: List<LocationContext>? = null,
        timePatterns: List<TimePattern>? = null,
        personalization: AIAssistantPersonalization = AIAssistantPersonalization()
    ): Flow<String>

    /**
     * Generate personalized habit suggestions based on user context
     *
     * @param userHabits List of user's current habits
     * @param previousSuggestion Optional previous suggestion for context
     * @param habitCompletions Optional list of habit completions for additional context
     * @param moodData Optional list of mood entries for emotional context
     * @param locationData Optional list of location data for spatial context
     * @param timePatterns Optional list of time patterns for temporal context
     * @param personalization Optional personalization settings
     * @return List of AI-generated suggestions
     */
    suspend fun generateSuggestions(
        userHabits: List<Habit>,
        previousSuggestion: AISuggestion? = null,
        habitCompletions: List<HabitCompletion>? = null,
        moodData: List<MoodEntry>? = null,
        locationData: List<LocationContext>? = null,
        timePatterns: List<TimePattern>? = null,
        personalization: AIAssistantPersonalization = AIAssistantPersonalization()
    ): List<AISuggestion>

    /**
     * Generate a new habit suggestion
     *
     * @param userHabits List of user's current habits
     * @param habitCompletions Optional list of habit completions for additional context
     * @param moodData Optional list of mood entries for emotional context
     * @param locationData Optional list of location data for spatial context
     * @param timePatterns Optional list of time patterns for temporal context
     * @param personalization Optional personalization settings
     * @return AI-generated habit suggestion
     */
    suspend fun generateNewHabitSuggestion(
        userHabits: List<Habit>,
        habitCompletions: List<HabitCompletion>? = null,
        moodData: List<MoodEntry>? = null,
        locationData: List<LocationContext>? = null,
        timePatterns: List<TimePattern>? = null,
        personalization: AIAssistantPersonalization = AIAssistantPersonalization()
    ): String

    /**
     * Generate schedule optimization advice
     *
     * @param userHabits List of user's current habits
     * @param habitCompletions Optional list of habit completions for additional context
     * @param moodData Optional list of mood entries for emotional context
     * @param locationData Optional list of location data for spatial context
     * @param timePatterns Optional list of time patterns for temporal context
     * @param personalization Optional personalization settings
     * @return AI-generated schedule optimization advice
     */
    suspend fun generateScheduleOptimization(
        userHabits: List<Habit>,
        habitCompletions: List<HabitCompletion>? = null,
        moodData: List<MoodEntry>? = null,
        locationData: List<LocationContext>? = null,
        timePatterns: List<TimePattern>? = null,
        personalization: AIAssistantPersonalization = AIAssistantPersonalization()
    ): String

    /**
     * Generate motivation tips
     *
     * @param userHabits List of user's current habits
     * @param habitCompletions Optional list of habit completions for additional context
     * @param moodData Optional list of mood entries for emotional context
     * @param locationData Optional list of location data for spatial context
     * @param timePatterns Optional list of time patterns for temporal context
     * @param personalization Optional personalization settings
     * @return AI-generated motivation tips
     */
    suspend fun generateMotivationTips(
        userHabits: List<Habit>,
        habitCompletions: List<HabitCompletion>? = null,
        moodData: List<MoodEntry>? = null,
        locationData: List<LocationContext>? = null,
        timePatterns: List<TimePattern>? = null,
        personalization: AIAssistantPersonalization = AIAssistantPersonalization()
    ): String

    /**
     * Generate habit improvement tips
     *
     * @param userHabits List of user's current habits
     * @param habitCompletions Optional list of habit completions for additional context
     * @param moodData Optional list of mood entries for emotional context
     * @param locationData Optional list of location data for spatial context
     * @param timePatterns Optional list of time patterns for temporal context
     * @param personalization Optional personalization settings
     * @return AI-generated habit improvement tips
     */
    suspend fun generateHabitImprovementTips(
        userHabits: List<Habit>,
        habitCompletions: List<HabitCompletion>? = null,
        moodData: List<MoodEntry>? = null,
        locationData: List<LocationContext>? = null,
        timePatterns: List<TimePattern>? = null,
        personalization: AIAssistantPersonalization = AIAssistantPersonalization()
    ): String

    /**
     * Generate streak protection tips
     *
     * @param userHabits List of user's current habits
     * @param habitCompletions Optional list of habit completions for additional context
     * @param moodData Optional list of mood entries for emotional context
     * @param locationData Optional list of location data for spatial context
     * @param timePatterns Optional list of time patterns for temporal context
     * @param personalization Optional personalization settings
     * @return AI-generated streak protection tips
     */
    suspend fun generateStreakProtectionTips(
        userHabits: List<Habit>,
        habitCompletions: List<HabitCompletion>? = null,
        moodData: List<MoodEntry>? = null,
        locationData: List<LocationContext>? = null,
        timePatterns: List<TimePattern>? = null,
        personalization: AIAssistantPersonalization = AIAssistantPersonalization()
    ): String

    /**
     * Generate habit chain suggestions
     *
     * @param userHabits List of user's current habits
     * @param habitCompletions Optional list of habit completions for additional context
     * @param moodData Optional list of mood entries for emotional context
     * @param locationData Optional list of location data for spatial context
     * @param timePatterns Optional list of time patterns for temporal context
     * @param personalization Optional personalization settings
     * @return AI-generated habit chain suggestions
     */
    suspend fun generateHabitChainSuggestions(
        userHabits: List<Habit>,
        habitCompletions: List<HabitCompletion>? = null,
        moodData: List<MoodEntry>? = null,
        locationData: List<LocationContext>? = null,
        timePatterns: List<TimePattern>? = null,
        personalization: AIAssistantPersonalization = AIAssistantPersonalization()
    ): String

    /**
     * Generate insight analysis
     *
     * @param userHabits List of user's current habits
     * @param habitCompletions Optional list of habit completions for additional context
     * @param moodData Optional list of mood entries for emotional context
     * @param locationData Optional list of location data for spatial context
     * @param timePatterns Optional list of time patterns for temporal context
     * @param personalization Optional personalization settings
     * @return AI-generated insight analysis
     */
    suspend fun generateInsightAnalysis(
        userHabits: List<Habit>,
        habitCompletions: List<HabitCompletion>? = null,
        moodData: List<MoodEntry>? = null,
        locationData: List<LocationContext>? = null,
        timePatterns: List<TimePattern>? = null,
        personalization: AIAssistantPersonalization = AIAssistantPersonalization()
    ): String

    /**
     * Save personalization settings
     *
     * @param settings The personalization settings to save
     */
    suspend fun savePersonalizationSettings(
        settings: AIAssistantPersonalization
    )

    /**
     * Get personalization settings
     *
     * @return The current personalization settings
     */
    suspend fun getPersonalizationSettings(): AIAssistantPersonalization
}
