package com.example.myapplication.core.network.ai

import android.util.Log
import com.example.myapplication.core.data.model.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okio.Buffer
import okio.BufferedSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AIService using OpenAI API
 */
@Singleton
class OpenAIService @Inject constructor(
    private val openAIApiClient: OpenAIApiClient,
    private val gson: Gson
) : AIService {
    companion object {
        private const val TAG = "OpenAIService"
        private const val MODEL = "gpt-4o" // Using the latest model for better responses
        // In a real app, this would be stored securely and not hardcoded
        private const val API_KEY = "sk-demo-key-replace-with-real-key-in-production"
        private const val SYSTEM_PROMPT = """You are an AI assistant specialized in habit formation and behavior change.
            You help users build sustainable habits by providing personalized advice, motivation, and insights.
            Your responses should be evidence-based, practical, and focused on helping users develop and maintain positive habits.
            Keep your responses concise, friendly, and actionable.

            When providing suggestions, consider the user's:
            1. Current habits and their completion patterns
            2. Personal preferences and settings

            Tailor your responses to be maximally helpful and personalized."""
    }

    /**
     * Generate a response to a user question about habits
     */
    override suspend fun generateResponse(
        question: String,
        userHabits: List<Habit>?,
        habitCompletions: List<HabitCompletion>?,
        personalization: AIAssistantPersonalization
    ): String = withContext(Dispatchers.IO) {
        try {
            // Build context from habits
            val habitContext = if (userHabits != null && userHabits.isNotEmpty()) {
                "User's current habits: ${formatHabitsForPrompt(userHabits)}"
            } else {
                "User has not created any habits yet."
            }

            // Build context from habit completions
            val completionContext = if (habitCompletions != null && habitCompletions.isNotEmpty()) {
                "Recent habit completions: ${formatCompletionsForPrompt(habitCompletions, userHabits)}"
            } else {
                "No recent habit completions."
            }
            val messages = listOf(
                Message("system", SYSTEM_PROMPT),
                Message("system", habitContext),
                Message("system", completionContext),
                Message("user", question)
            )

            val request = ChatCompletionRequest(
                model = MODEL,
                messages = messages,
                temperature = 0.7f,
                stream = false
            )

            val response = openAIApiClient.createChatCompletion(
                authorization = "Bearer $API_KEY",
                request = request
            )

            if (response.isSuccessful && response.body() != null) {
                val content = response.body()?.choices?.firstOrNull()?.message?.content ?:
                    "I'm sorry, I couldn't generate a response at this time."
                return@withContext content
            } else {
                Log.e(TAG, "Error generating response: ${response.errorBody()?.string()}")
                return@withContext "I'm sorry, I couldn't generate a response at this time. Please try again later."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception generating response", e)
            return@withContext "I'm sorry, I encountered an error while processing your request. Please try again later."
        }
    }

    /**
     * Generate a streaming response to a user question about habits
     */
    override fun generateStreamingResponse(
        question: String,
        userHabits: List<Habit>?,
        habitCompletions: List<HabitCompletion>?,
        personalization: AIAssistantPersonalization
    ): Flow<String> = flow {
        try {
            // Build context from habits
            val habitContext = if (userHabits != null && userHabits.isNotEmpty()) {
                "User's current habits: ${formatHabitsForPrompt(userHabits)}"
            } else {
                "User has not created any habits yet."
            }

            // Build context from habit completions
            val completionContext = if (habitCompletions != null && habitCompletions.isNotEmpty()) {
                "Recent habit completions: ${formatCompletionsForPrompt(habitCompletions, userHabits)}"
            } else {
                "No recent habit completions."
            }
            val messages = listOf(
                Message("system", SYSTEM_PROMPT),
                Message("system", habitContext),
                Message("system", completionContext),
                Message("user", question)
            )

            val request = ChatCompletionRequest(
                model = MODEL,
                messages = messages,
                temperature = 0.7f,
                stream = true
            )

            val response = openAIApiClient.createStreamingChatCompletion(
                authorization = "Bearer $API_KEY",
                request = request
            )

            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body() ?: return@withContext
                val source = responseBody.source()

                // Process the streaming response
                processStreamingResponse(source) { content ->
                    emit(content)
                }
            } else {
                Log.e(TAG, "Error generating streaming response: ${response.errorBody()?.string()}")
                emit("I'm sorry, I couldn't generate a response at this time. Please try again later.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception generating streaming response", e)

            // Fallback to local service if API fails
            try {
                // Generate a full response first
                val localResponse = generateResponse(
                    question,
                    userHabits,
                    habitCompletions,
                    personalization
                )

                // Split the response into words and emit them with a delay to simulate streaming
                val words = localResponse.split(" ")
                for (i in words.indices) {
                    val chunk = if (i == 0) words[i] else " ${words[i]}"
                    emit(chunk)
                    delay(50) // 50ms delay between words
                }
            } catch (fallbackException: Exception) {
                Log.e(TAG, "Fallback also failed", fallbackException)
                emit("I'm sorry, I encountered an error while processing your request. Please try again later.")
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Process streaming response from OpenAI API
     */
    private suspend fun processStreamingResponse(source: BufferedSource, onContent: suspend (String) -> Unit) {
        try {
            // Buffer to read lines
            val buffer = Buffer()

            // Process the stream line by line
            while (!source.exhausted()) {
                // Read a line
                source.readUtf8Line()?.let { line ->
                    // Skip empty lines
                    if (line.isEmpty()) return@let

                    // Parse SSE line
                    val data = SSEParser.parseSSELine(line) ?: return@let

                    // Check for [DONE] marker
                    if (data == "[DONE]") return

                    try {
                        // Parse the JSON data
                        val streamingResponse = gson.fromJson(data, StreamingChatCompletionResponse::class.java)

                        // Extract content from the delta
                        val content = streamingResponse.choices.firstOrNull()?.delta?.content

                        // Emit content if available
                        if (content != null) {
                            onContent(content)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing streaming response: ${e.message}")
                    }
                } ?: break // Break if readUtf8Line returns null (end of stream)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing streaming response", e)
            throw e
        }
    }

    /**
     * Generate personalized habit suggestions
     */
    override suspend fun generateSuggestions(
        userHabits: List<Habit>,
        previousSuggestion: AISuggestion?,
        habitCompletions: List<HabitCompletion>?,
        personalization: AIAssistantPersonalization
    ): List<AISuggestion> = withContext(Dispatchers.IO) {
        try {
            val habitContext = formatHabitsForPrompt(userHabits)
            val previousContext = if (previousSuggestion != null) {
                "Previous suggestion: ${previousSuggestion.title} - ${previousSuggestion.description} (Type: ${previousSuggestion.type})"
            } else {
                "No previous suggestion."
            }

            // Add completion context if available
            val completionContext = if (habitCompletions != null && habitCompletions.isNotEmpty()) {
                "Recent habit completions: ${formatCompletionsForPrompt(habitCompletions, userHabits)}"
            } else {
                "No recent habit completions."
            }

            val prompt = """
                Based on the user's habits, completion history, and any previous suggestion, generate 3-4 personalized habit suggestions.
                For each suggestion, include a title, description, suggestion type, and confidence score (0.0-1.0).

                User's habits: $habitContext
                $previousContext
                $completionContext

                Format your response as a JSON array of objects with the following structure:
                [
                  {
                    "title": "Suggestion title",
                    "description": "Brief description",
                    "type": "One of: NEW_HABIT, HABIT_IMPROVEMENT, STREAK_PROTECTION, SCHEDULE_OPTIMIZATION, HABIT_CHAIN, MOTIVATION, INSIGHT",
                    "confidence": 0.85
                  }
                ]
            """.trimIndent()

            val messages = listOf(
                Message("system", SYSTEM_PROMPT),
                Message("user", prompt)
            )

            val request = ChatCompletionRequest(
                model = MODEL,
                messages = messages,
                temperature = 0.7f
            )

            val response = openAIApiClient.createChatCompletion(
                authorization = "Bearer $API_KEY",
                request = request
            )

            if (response.isSuccessful && response.body() != null) {
                val content = response.body()?.choices?.firstOrNull()?.message?.content ?: ""
                // Extract JSON from the response
                val jsonContent = extractJsonFromString(content)

                // Parse JSON into suggestions
                return@withContext try {
                    val suggestionDtos = gson.fromJson(jsonContent, Array<SuggestionDto>::class.java)
                    suggestionDtos.map { dto ->
                        AISuggestion(
                            id = UUID.randomUUID().toString(),
                            title = dto.title,
                            description = dto.description,
                            type = parseSuggestionType(dto.type),
                            confidence = dto.confidence
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing suggestions JSON", e)
                    generateFallbackSuggestions()
                }
            } else {
                Log.e(TAG, "Error generating suggestions: ${response.errorBody()?.string()}")
                return@withContext generateFallbackSuggestions()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception generating suggestions", e)
            return@withContext generateFallbackSuggestions()
        }
    }

    /**
     * Generate a new habit suggestion
     */
    override suspend fun generateNewHabitSuggestion(
        userHabits: List<Habit>,
        habitCompletions: List<HabitCompletion>?,
        personalization: AIAssistantPersonalization
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            Based on the user's current habits, suggest a new habit they could adopt.
            Provide a detailed explanation of why this habit would be beneficial and how to implement it.

            User's habits: ${formatHabitsForPrompt(userHabits)}
            ${if (habitCompletions != null && habitCompletions.isNotEmpty())
                "Recent habit completions: ${formatCompletionsForPrompt(habitCompletions, userHabits)}"
              else ""}
        """.trimIndent()

        return@withContext generateResponse(prompt, userHabits, habitCompletions, personalization)
    }

    /**
     * Generate schedule optimization advice
     */
    override suspend fun generateScheduleOptimization(
        userHabits: List<Habit>,
        habitCompletions: List<HabitCompletion>?,
        personalization: AIAssistantPersonalization
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            Based on the user's current habits, provide advice on how to optimize their habit schedule.
            Consider habit stacking, time blocking, and other scheduling techniques.

            User's habits: ${formatHabitsForPrompt(userHabits)}
            ${if (habitCompletions != null && habitCompletions.isNotEmpty())
                "Recent habit completions: ${formatCompletionsForPrompt(habitCompletions, userHabits)}"
              else ""}
        """.trimIndent()

        return@withContext generateResponse(prompt, userHabits, habitCompletions, personalization)
    }

    /**
     * Generate motivation tips
     */
    override suspend fun generateMotivationTips(
        userHabits: List<Habit>,
        habitCompletions: List<HabitCompletion>?,
        personalization: AIAssistantPersonalization
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            Provide motivation tips to help the user stay consistent with their habits.
            Include strategies for overcoming common obstacles and maintaining motivation.

            User's habits: ${formatHabitsForPrompt(userHabits)}
            ${if (habitCompletions != null && habitCompletions.isNotEmpty())
                "Recent habit completions: ${formatCompletionsForPrompt(habitCompletions, userHabits)}"
              else ""}
        """.trimIndent()

        return@withContext generateResponse(prompt, userHabits, habitCompletions, personalization)
    }

    /**
     * Generate habit improvement tips
     */
    override suspend fun generateHabitImprovementTips(
        userHabits: List<Habit>,
        habitCompletions: List<HabitCompletion>?,
        personalization: AIAssistantPersonalization
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            Suggest ways the user could improve their existing habits.
            Focus on making habits more effective, enjoyable, or easier to maintain.

            User's habits: ${formatHabitsForPrompt(userHabits)}
            ${if (habitCompletions != null && habitCompletions.isNotEmpty())
                "Recent habit completions: ${formatCompletionsForPrompt(habitCompletions, userHabits)}"
              else ""}
        """.trimIndent()

        return@withContext generateResponse(prompt, userHabits, habitCompletions, personalization)
    }

    /**
     * Generate streak protection tips
     */
    override suspend fun generateStreakProtectionTips(
        userHabits: List<Habit>,
        habitCompletions: List<HabitCompletion>?,
        personalization: AIAssistantPersonalization
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            Provide strategies for protecting habit streaks and avoiding breaking the chain.
            Include contingency plans for busy days and recovery strategies for when streaks are broken.

            User's habits: ${formatHabitsForPrompt(userHabits)}
            ${if (habitCompletions != null && habitCompletions.isNotEmpty())
                "Recent habit completions: ${formatCompletionsForPrompt(habitCompletions, userHabits)}"
              else ""}
        """.trimIndent()

        return@withContext generateResponse(prompt, userHabits, habitCompletions, personalization)
    }

    /**
     * Generate habit chain suggestions
     */
    override suspend fun generateHabitChainSuggestions(
        userHabits: List<Habit>,
        habitCompletions: List<HabitCompletion>?,
        personalization: AIAssistantPersonalization
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            Suggest habit chains or routines the user could implement based on their current habits.
            Explain how these chains can make habits more effective and easier to maintain.

            User's habits: ${formatHabitsForPrompt(userHabits)}
            ${if (habitCompletions != null && habitCompletions.isNotEmpty())
                "Recent habit completions: ${formatCompletionsForPrompt(habitCompletions, userHabits)}"
              else ""}
        """.trimIndent()

        return@withContext generateResponse(prompt, userHabits, habitCompletions, personalization)
    }

    /**
     * Generate insight analysis
     */
    override suspend fun generateInsightAnalysis(
        userHabits: List<Habit>,
        habitCompletions: List<HabitCompletion>?,
        personalization: AIAssistantPersonalization
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            Analyze the user's habits and provide insights on patterns, potential improvements, and overall habit strategy.
            Include observations about habit variety, balance, and potential blind spots.

            User's habits: ${formatHabitsForPrompt(userHabits)}
            ${if (habitCompletions != null && habitCompletions.isNotEmpty())
                "Recent habit completions: ${formatCompletionsForPrompt(habitCompletions, userHabits)}"
              else ""}
        """.trimIndent()

        return@withContext generateResponse(prompt, userHabits, habitCompletions, personalization)
    }

    /**
     * Format habits for inclusion in prompts
     */
    private fun formatHabitsForPrompt(habits: List<Habit>): String {
        if (habits.isEmpty()) return "No habits yet."

        return habits.joinToString(", ") { habit ->
            "${habit.name} (${habit.description ?: "No description"}, Frequency: ${habit.frequency}, " +
            "Streak: ${habit.streak}, Goal: ${habit.goal})"
        }
    }

    /**
     * Format habit completions for inclusion in prompts
     */
    private fun formatCompletionsForPrompt(completions: List<HabitCompletion>, habits: List<Habit>?): String {
        if (completions.isEmpty()) return "No recent completions."

        // Create a map of habit IDs to habit names for quick lookup
        val habitMap = habits?.associateBy { it.id } ?: emptyMap()

        // Sort completions by date (most recent first)
        val sortedCompletions = completions.sortedByDescending { it.completionDate }

        // Take the 10 most recent completions
        val recentCompletions = sortedCompletions.take(10)

        return recentCompletions.joinToString("\n") { completion ->
            val habitName = habitMap[completion.habitId]?.name ?: "Unknown habit"
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(Date(completion.completionDate))
            val mood = completion.mood?.let { "Mood: $it/5" } ?: ""
            val note = completion.note?.let { "Note: \"$it\"" } ?: ""

            "$habitName completed on $date. $mood $note"
        }
    }

    /**
     * Extract JSON from a string that might contain additional text
     */
    private fun extractJsonFromString(input: String): String {
        val jsonStart = input.indexOf('[')
        val jsonEnd = input.lastIndexOf(']')

        return if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            input.substring(jsonStart, jsonEnd + 1)
        } else {
            "[]" // Return empty array if no JSON found
        }
    }

    /**
     * Parse suggestion type from string
     */
    private fun parseSuggestionType(typeString: String): SuggestionType {
        return try {
            SuggestionType.valueOf(typeString.uppercase())
        } catch (e: Exception) {
            // Default to NEW_HABIT if parsing fails
            SuggestionType.NEW_HABIT
        }
    }

    /**
     * Generate fallback suggestions when API fails
     */
    private fun generateFallbackSuggestions(): List<AISuggestion> {
        return listOf(
            AISuggestion(
                id = UUID.randomUUID().toString(),
                title = "Start a daily meditation practice",
                description = "Begin with just 2 minutes each morning",
                type = SuggestionType.NEW_HABIT,
                confidence = 0.95f
            ),
            AISuggestion(
                id = UUID.randomUUID().toString(),
                title = "Stack habits for better consistency",
                description = "Connect new habits to existing routines",
                type = SuggestionType.HABIT_IMPROVEMENT,
                confidence = 0.88f
            ),
            AISuggestion(
                id = UUID.randomUUID().toString(),
                title = "Create a habit emergency plan",
                description = "Have a minimal version of each habit for busy days",
                type = SuggestionType.STREAK_PROTECTION,
                confidence = 0.85f
            )
        )
    }

    /**
     * Save personalization settings
     */
    override suspend fun savePersonalizationSettings(settings: AIAssistantPersonalization) {
        // Delegate to the repository
        // This would be implemented with a repository in a real app
    }

    /**
     * Get personalization settings
     */
    override suspend fun getPersonalizationSettings(): AIAssistantPersonalization {
        // Return default settings
        // This would be implemented with a repository in a real app
        return AIAssistantPersonalization()
    }
}

/**
 * DTO for parsing suggestion JSON
 */
private data class SuggestionDto(
    val title: String,
    val description: String,
    val type: String,
    val confidence: Float
)
