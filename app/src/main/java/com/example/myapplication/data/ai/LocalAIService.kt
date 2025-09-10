package com.example.myapplication.data.ai

import android.util.Log
import com.example.myapplication.core.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local implementation of AIService that doesn't require external API calls
 * This is used as a fallback when the API is not available or when the user doesn't want to use an external API
 */
@Singleton
class LocalAIService @Inject constructor() : AIService {
    companion object {
        private const val TAG = "LocalAIService"

        // Simulate processing delay for more realistic experience
        private const val PROCESSING_DELAY_MS = 1000L
    }

    override suspend fun generateResponse(question: String, userHabits: List<Habit>?, habitCompletions: List<HabitCompletion>?, moodData: List<MoodEntry>?, locationData: List<LocationContext>?, timePatterns: List<TimePattern>?, personalization: AIAssistantPersonalization): String {
        return "This is a local response to the question: $question"
    }

    override fun generateStreamingResponse(question: String, userHabits: List<Habit>?, habitCompletions: List<HabitCompletion>?, moodData: List<MoodEntry>?, locationData: List<LocationContext>?, timePatterns: List<TimePattern>?, personalization: AIAssistantPersonalization): Flow<String> {
        return flow {
            emit("This is a local streaming response to the question: $question")
        }
    }

    override suspend fun generateSuggestions(userHabits: List<Habit>, previousSuggestion: AISuggestion?, habitCompletions: List<HabitCompletion>?, moodData: List<MoodEntry>?, locationData: List<LocationContext>?, timePatterns: List<TimePattern>?, personalization: AIAssistantPersonalization): List<AISuggestion> {
        return emptyList()
    }

    override suspend fun generateNewHabitSuggestion(userHabits: List<Habit>, habitCompletions: List<HabitCompletion>?, moodData: List<MoodEntry>?, locationData: List<LocationContext>?, timePatterns: List<TimePattern>?, personalization: AIAssistantPersonalization): String {
        return "This is a new habit suggestion"
    }

    override suspend fun generateScheduleOptimization(userHabits: List<Habit>, habitCompletions: List<HabitCompletion>?, moodData: List<MoodEntry>?, locationData: List<LocationContext>?, timePatterns: List<TimePattern>?, personalization: AIAssistantPersonalization): String {
        return "This is a schedule optimization"
    }

    override suspend fun generateMotivationTips(userHabits: List<Habit>, habitCompletions: List<HabitCompletion>?, moodData: List<MoodEntry>?, locationData: List<LocationContext>?, timePatterns: List<TimePattern>?, personalization: AIAssistantPersonalization): String {
        return "These are motivation tips"
    }

    override suspend fun generateHabitImprovementTips(userHabits: List<Habit>, habitCompletions: List<HabitCompletion>?, moodData: List<MoodEntry>?, locationData: List<LocationContext>?, timePatterns: List<TimePattern>?, personalization: AIAssistantPersonalization): String {
        return "These are habit improvement tips"
    }

    override suspend fun generateStreakProtectionTips(userHabits: List<Habit>, habitCompletions: List<HabitCompletion>?, moodData: List<MoodEntry>?, locationData: List<LocationContext>?, timePatterns: List<TimePattern>?, personalization: AIAssistantPersonalization): String {
        return "These are streak protection tips"
    }

    override suspend fun generateHabitChainSuggestions(userHabits: List<Habit>, habitCompletions: List<HabitCompletion>?, moodData: List<MoodEntry>?, locationData: List<LocationContext>?, timePatterns: List<TimePattern>?, personalization: AIAssistantPersonalization): String {
        return "These are habit chain suggestions"
    }

    override suspend fun generateInsightAnalysis(userHabits: List<Habit>, habitCompletions: List<HabitCompletion>?, moodData: List<MoodEntry>?, locationData: List<LocationContext>?, timePatterns: List<TimePattern>?, personalization: AIAssistantPersonalization): String {
        return "This is an insight analysis"
    }

    override suspend fun savePersonalizationSettings(settings: AIAssistantPersonalization) {
        // No-op for local service
    }

    override suspend fun getPersonalizationSettings(): AIAssistantPersonalization {
        return AIAssistantPersonalization()
    }
}
