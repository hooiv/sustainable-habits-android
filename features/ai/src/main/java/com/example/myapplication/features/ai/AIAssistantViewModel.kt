package com.example.myapplication.features.ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.network.ai.AIService
import com.example.myapplication.core.data.model.*
import com.example.myapplication.core.data.repository.HabitRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.util.*
import javax.inject.Inject
import com.example.myapplication.core.di.IoDispatcher

/**
 * ViewModel for the AI Assistant screen
 */
@HiltViewModel
class AIAssistantViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val aiService: AIService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    // Suggestions for the user
    private val _suggestions = MutableStateFlow<List<AISuggestion>>(emptyList())
    val suggestions: StateFlow<List<AISuggestion>> = _suggestions.asStateFlow()

    // Responses from the AI
    private val _responses = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val responses: StateFlow<List<Pair<String, String>>> = _responses.asStateFlow()

    // Processing state
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // Streaming state
    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    // Current streaming response
    private val _streamingResponse = MutableStateFlow("")
    val streamingResponse: StateFlow<String> = _streamingResponse.asStateFlow()

    // User habits for context
    private val _userHabits = MutableStateFlow<List<Habit>>(emptyList())

    // Habit completions for additional context
    private val _habitCompletions = MutableStateFlow<List<HabitCompletion>>(emptyList())

    // Personalization settings
    private val _personalizationSettings = MutableStateFlow(AIAssistantPersonalization())
    val personalizationSettings: StateFlow<AIAssistantPersonalization> = _personalizationSettings.asStateFlow()

    init {
        // Load user habits and completions
        loadUserData()

        // Initialize with default suggestions
        viewModelScope.launch {
            _suggestions.value = aiService.generateSuggestions(
                _userHabits.value,
                null,
                _habitCompletions.value,
                _personalizationSettings.value
            )
        }
    }

    /**
     * Load user habits and completions for context
     */
    private fun loadUserData() {
        // Load habits — independent top-level collector
        viewModelScope.launch(ioDispatcher) {
            try {
                habitRepository.getAllHabits().collect { habits ->
                    _userHabits.value = habits
                }
            } catch (e: Exception) {
                _userHabits.value = emptyList()
            }
        }
        // Load completions — independent top-level collector (not nested inside habits)
        loadHabitCompletions()
    }

    /**
     * Load habit completions for additional context
     */
    private fun loadHabitCompletions() {
        viewModelScope.launch(ioDispatcher) {
            try {
                // Load all completions
                habitRepository.getAllCompletions().collect { completions ->
                    _habitCompletions.value = completions
                }
            } catch (e: Exception) {
                // If we can't load completions, just use an empty list
                _habitCompletions.value = emptyList()
            }
        }
    }

    /**
     * Process a suggestion selected by the user
     */
    fun processSuggestion(suggestion: AISuggestion, useStreaming: Boolean = true) {
        viewModelScope.launch {
            _isProcessing.value = true

            try {
                if (useStreaming) {
                    // Use streaming response
                    processStreamingSuggestion(suggestion)
                } else {
                    // Generate response based on suggestion type
                    val response = when (suggestion.type) {
                        SuggestionType.NEW_HABIT ->
                            aiService.generateNewHabitSuggestion(
                                _userHabits.value,
                                _habitCompletions.value,
                                _personalizationSettings.value
                            )
                        SuggestionType.SCHEDULE_OPTIMIZATION ->
                            aiService.generateScheduleOptimization(
                                _userHabits.value,
                                _habitCompletions.value,
                                _personalizationSettings.value
                            )
                        SuggestionType.MOTIVATION ->
                            aiService.generateMotivationTips(
                                _userHabits.value,
                                _habitCompletions.value,
                                _personalizationSettings.value
                            )
                        SuggestionType.HABIT_IMPROVEMENT ->
                            aiService.generateHabitImprovementTips(
                                _userHabits.value,
                                _habitCompletions.value,
                                _personalizationSettings.value
                            )
                        SuggestionType.STREAK_PROTECTION ->
                            aiService.generateStreakProtectionTips(
                                _userHabits.value,
                                _habitCompletions.value,
                                _personalizationSettings.value
                            )
                        SuggestionType.HABIT_CHAIN ->
                            aiService.generateHabitChainSuggestions(
                                _userHabits.value,
                                _habitCompletions.value,
                                _personalizationSettings.value
                            )
                        SuggestionType.INSIGHT ->
                            aiService.generateInsightAnalysis(
                                _userHabits.value,
                                _habitCompletions.value,
                                _personalizationSettings.value
                            )
                    }

                    // Add to responses
                    _responses.value = _responses.value + Pair(suggestion.title, response)

                    // Generate new suggestions based on context
                    _suggestions.value = aiService.generateSuggestions(
                        _userHabits.value,
                        suggestion,
                        _habitCompletions.value,
                        _personalizationSettings.value
                    )
                }
            } catch (e: Exception) {
                // Handle errors gracefully
                val errorMessage = "I'm sorry, I encountered an error while processing your request. Please try again later."
                _responses.value = _responses.value + Pair(suggestion.title, errorMessage)

                // Error handled without voice
            } finally {
                if (!useStreaming) {
                    _isProcessing.value = false
                }
            }
        }
    }

    /**
     * Process a suggestion with streaming response
     */
    private suspend fun processStreamingSuggestion(suggestion: AISuggestion) {
        try {
            _isStreaming.value = true
            _streamingResponse.value = ""

            // Get the prompt based on suggestion type
            val prompt = when (suggestion.type) {
                SuggestionType.NEW_HABIT -> "Suggest a new habit for me"
                SuggestionType.SCHEDULE_OPTIMIZATION -> "Help me optimize my habit schedule"
                SuggestionType.MOTIVATION -> "How can I stay motivated with my habits?"
                SuggestionType.HABIT_IMPROVEMENT -> "How can I improve my existing habits?"
                SuggestionType.STREAK_PROTECTION -> "How can I protect my habit streaks?"
                SuggestionType.HABIT_CHAIN -> "Suggest habit chains I could implement"
                SuggestionType.INSIGHT -> "Analyze my habit progress and provide insights"
            }

            // Collect streaming response
            aiService.generateStreamingResponse(
                prompt,
                _userHabits.value,
                _habitCompletions.value,
                _personalizationSettings.value
            ).flowOn(ioDispatcher).collect { chunk ->
                // Append chunk to streaming response
                _streamingResponse.value += chunk
            }

            // Add completed response to responses list
            _responses.value = _responses.value + Pair(suggestion.title, _streamingResponse.value)

            // Generate new suggestions based on context
            _suggestions.value = aiService.generateSuggestions(
                _userHabits.value,
                suggestion,
                _habitCompletions.value,
                _personalizationSettings.value
            )

        } finally {
            _isStreaming.value = false
            _isProcessing.value = false
        }
    }

    /**
     * Process a custom question from the user
     */
    fun askQuestion(question: String, useStreaming: Boolean = true) {
        viewModelScope.launch {
            _isProcessing.value = true

            try {
                if (useStreaming) {
                    // Use streaming response
                    processStreamingQuestion(question)
                } else {
                    // Generate response using AI service
                    val response = aiService.generateResponse(
                        question,
                        _userHabits.value,
                        _habitCompletions.value,
                        _personalizationSettings.value
                    )

                    // Add to responses
                    _responses.value = _responses.value + Pair(question, response)

                    // Generate new suggestions based on the question and response
                    val customSuggestion = AISuggestion(
                        id = UUID.randomUUID().toString(),
                        title = question,
                        description = "Custom question",
                        type = SuggestionType.INSIGHT,
                        confidence = 0.8f
                    )

                    _suggestions.value = aiService.generateSuggestions(
                        _userHabits.value,
                        customSuggestion,
                        _habitCompletions.value,
                        _personalizationSettings.value
                    )

                    // Removed voice feature
                }
            } catch (e: Exception) {
                // Handle errors gracefully
                val errorMessage = "I'm sorry, I encountered an error while processing your request. Please try again later."
                _responses.value = _responses.value + Pair(question, errorMessage)

                // Error handling without voice
            } finally {
                if (!useStreaming) {
                    _isProcessing.value = false
                }
            }
        }
    }

    /**
     * Process a question with streaming response
     */
    private suspend fun processStreamingQuestion(question: String) {
        try {
            _isStreaming.value = true
            _streamingResponse.value = ""

            // Collect streaming response
            aiService.generateStreamingResponse(
                question,
                _userHabits.value,
                _habitCompletions.value,
                _personalizationSettings.value
            ).flowOn(ioDispatcher).collect { chunk ->
                // Append chunk to streaming response
                _streamingResponse.value += chunk
            }

            // Add completed response to responses list
            _responses.value = _responses.value + Pair(question, _streamingResponse.value)

            // Generate new suggestions based on the question and response
            val customSuggestion = AISuggestion(
                id = UUID.randomUUID().toString(),
                title = question,
                description = "Custom question",
                type = SuggestionType.INSIGHT,
                confidence = 0.8f
            )

            _suggestions.value = aiService.generateSuggestions(
                _userHabits.value,
                customSuggestion,
                _habitCompletions.value,
                _personalizationSettings.value
            )

        } finally {
            _isStreaming.value = false
            _isProcessing.value = false
        }
    }

    /**
     * Save personalization settings
     */
    fun savePersonalizationSettings(settings: AIAssistantPersonalization) {
        viewModelScope.launch {
            _personalizationSettings.value = settings
        }
    }
}
