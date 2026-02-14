package com.example.myapplication.features.ai.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.network.ai.AIService
import com.example.myapplication.core.data.model.*
import com.example.myapplication.core.data.repository.AIContextRepository
import com.example.myapplication.core.data.repository.HabitRepository
import com.example.myapplication.features.voice.TextToSpeechService
import com.example.myapplication.features.voice.VoiceRecognitionService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for the AI Assistant screen
 */
@HiltViewModel
class AIAssistantViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val aiService: AIService,
    private val textToSpeechService: TextToSpeechService,
    private val voiceRecognitionService: VoiceRecognitionService,
    private val aiContextRepository: AIContextRepository
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

    // Voice input/output state
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _voiceInputText = MutableStateFlow("")
    val voiceInputText: StateFlow<String> = _voiceInputText.asStateFlow()

    private val _voiceAmplitude = MutableStateFlow(0f)
    val voiceAmplitude: StateFlow<Float> = _voiceAmplitude.asStateFlow()

    // User habits for context
    private val _userHabits = MutableStateFlow<List<Habit>>(emptyList())

    // Habit completions for additional context
    private val _habitCompletions = MutableStateFlow<List<HabitCompletion>>(emptyList())

    // Mood data for emotional context
    private val _moodData = MutableStateFlow<List<MoodEntry>>(emptyList())

    // Location data for spatial context
    private val _locationData = MutableStateFlow<List<LocationContext>>(emptyList())

    // Time patterns for temporal context
    private val _timePatterns = MutableStateFlow<List<TimePattern>>(emptyList())

    // Personalization settings
    private val _personalizationSettings = MutableStateFlow(AIAssistantPersonalization())
    val personalizationSettings: StateFlow<AIAssistantPersonalization> = _personalizationSettings.asStateFlow()

    init {
        // Initialize text-to-speech
        textToSpeechService.initialize()

        // Load user habits and completions
        loadUserData()

        // Load personalization settings
        loadPersonalizationSettings()

        // Initialize with default suggestions
        viewModelScope.launch {
            _suggestions.value = aiService.generateSuggestions(
                _userHabits.value,
                null,
                _habitCompletions.value,
                _moodData.value,
                _locationData.value,
                _timePatterns.value,
                _personalizationSettings.value
            )
        }
    }

    /**
     * Load user habits and completions for context
     */
    private fun loadUserData() {
        viewModelScope.launch {
            try {
                // Load habits
                habitRepository.getAllHabits().collect { habits ->
                    _userHabits.value = habits

                    // After loading habits, load completions
                    loadHabitCompletions()
                }
            } catch (e: Exception) {
                // If we can't load habits, just use an empty list
                _userHabits.value = emptyList()
            }
        }

        // Load additional context data
        loadMoodData()
        loadLocationData()
        loadTimePatterns()
    }

    /**
     * Load habit completions for additional context
     */
    private fun loadHabitCompletions() {
        viewModelScope.launch {
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
     * Load mood data for emotional context
     */
    private fun loadMoodData() {
        viewModelScope.launch {
            try {
                // Load mood data
                aiContextRepository.getMoodEntriesFlow().collect { moodEntries ->
                    _moodData.value = moodEntries
                }
            } catch (e: Exception) {
                // If we can't load mood data, just use an empty list
                _moodData.value = emptyList()
            }
        }
    }

    /**
     * Load location data for spatial context
     */
    private fun loadLocationData() {
        viewModelScope.launch {
            try {
                // Load location data
                aiContextRepository.getLocationContextsFlow().collect { locationContexts ->
                    _locationData.value = locationContexts
                }
            } catch (e: Exception) {
                // If we can't load location data, just use an empty list
                _locationData.value = emptyList()
            }
        }
    }

    /**
     * Load time patterns for temporal context
     */
    private fun loadTimePatterns() {
        viewModelScope.launch {
            try {
                // Load time patterns
                aiContextRepository.getTimePatternsFlow().collect { timePatterns ->
                    _timePatterns.value = timePatterns
                }
            } catch (e: Exception) {
                // If we can't load time patterns, just use an empty list
                _timePatterns.value = emptyList()
            }
        }
    }

    /**
     * Load personalization settings
     */
    private fun loadPersonalizationSettings() {
        viewModelScope.launch {
            try {
                // Load personalization settings
                aiContextRepository.getPersonalizationSettingsFlow().collect { settings ->
                    _personalizationSettings.value = settings

                    // Update text-to-speech settings
                    textToSpeechService.speak(settings.voiceSpeed.toString())
                    textToSpeechService.speak(settings.voicePitch.toString())
                }
            } catch (e: Exception) {
                // If we can't load settings, use defaults
                _personalizationSettings.value = AIAssistantPersonalization()
            }
        }
    }

    /**
     * Process a suggestion selected by the user
     */
    fun processSuggestion(suggestion: AISuggestion, useStreaming: Boolean = true, useVoice: Boolean = false) {
        viewModelScope.launch {
            _isProcessing.value = true

            try {
                if (useStreaming) {
                    // Use streaming response
                    processStreamingSuggestion(suggestion, useVoice)
                } else {
                    // Generate response based on suggestion type
                    val response = when (suggestion.type) {
                        SuggestionType.NEW_HABIT ->
                            aiService.generateNewHabitSuggestion(
                                _userHabits.value,
                                _habitCompletions.value,
                                _moodData.value,
                                _locationData.value,
                                _timePatterns.value,
                                _personalizationSettings.value
                            )
                        SuggestionType.SCHEDULE_OPTIMIZATION ->
                            aiService.generateScheduleOptimization(
                                _userHabits.value,
                                _habitCompletions.value,
                                _moodData.value,
                                _locationData.value,
                                _timePatterns.value,
                                _personalizationSettings.value
                            )
                        SuggestionType.MOTIVATION ->
                            aiService.generateMotivationTips(
                                _userHabits.value,
                                _habitCompletions.value,
                                _moodData.value,
                                _locationData.value,
                                _timePatterns.value,
                                _personalizationSettings.value
                            )
                        SuggestionType.HABIT_IMPROVEMENT ->
                            aiService.generateHabitImprovementTips(
                                _userHabits.value,
                                _habitCompletions.value,
                                _moodData.value,
                                _locationData.value,
                                _timePatterns.value,
                                _personalizationSettings.value
                            )
                        SuggestionType.STREAK_PROTECTION ->
                            aiService.generateStreakProtectionTips(
                                _userHabits.value,
                                _habitCompletions.value,
                                _moodData.value,
                                _locationData.value,
                                _timePatterns.value,
                                _personalizationSettings.value
                            )
                        SuggestionType.HABIT_CHAIN ->
                            aiService.generateHabitChainSuggestions(
                                _userHabits.value,
                                _habitCompletions.value,
                                _moodData.value,
                                _locationData.value,
                                _timePatterns.value,
                                _personalizationSettings.value
                            )
                        SuggestionType.INSIGHT ->
                            aiService.generateInsightAnalysis(
                                _userHabits.value,
                                _habitCompletions.value,
                                _moodData.value,
                                _locationData.value,
                                _timePatterns.value,
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
                        _moodData.value,
                        _locationData.value,
                        _timePatterns.value,
                        _personalizationSettings.value
                    )

                    // Speak the response if voice is enabled
                    if (useVoice) {
                        speakResponse(response)
                    }
                }
            } catch (e: Exception) {
                // Handle errors gracefully
                val errorMessage = "I'm sorry, I encountered an error while processing your request. Please try again later."
                _responses.value = _responses.value + Pair(suggestion.title, errorMessage)

                if (useVoice) {
                    speakResponse(errorMessage)
                }
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
    private suspend fun processStreamingSuggestion(suggestion: AISuggestion, useVoice: Boolean) {
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
                _moodData.value,
                _locationData.value,
                _timePatterns.value,
                _personalizationSettings.value
            ).collect { chunk ->
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
                _moodData.value,
                _locationData.value,
                _timePatterns.value,
                _personalizationSettings.value
            )

            // Speak the response if voice is enabled
            if (useVoice) {
                speakResponse(_streamingResponse.value)
            }
        } finally {
            _isStreaming.value = false
            _isProcessing.value = false
        }
    }

    /**
     * Process a custom question from the user
     */
    fun askQuestion(question: String, useStreaming: Boolean = true, useVoice: Boolean = false) {
        viewModelScope.launch {
            _isProcessing.value = true

            try {
                if (useStreaming) {
                    // Use streaming response
                    processStreamingQuestion(question, useVoice)
                } else {
                    // Generate response using AI service
                    val response = aiService.generateResponse(
                        question,
                        _userHabits.value,
                        _habitCompletions.value,
                        _moodData.value,
                        _locationData.value,
                        _timePatterns.value,
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
                        _moodData.value,
                        _locationData.value,
                        _timePatterns.value,
                        _personalizationSettings.value
                    )

                    // Speak the response if voice is enabled
                    if (useVoice) {
                        speakResponse(response)
                    }
                }
            } catch (e: Exception) {
                // Handle errors gracefully
                val errorMessage = "I'm sorry, I encountered an error while processing your request. Please try again later."
                _responses.value = _responses.value + Pair(question, errorMessage)

                if (useVoice) {
                    speakResponse(errorMessage)
                }
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
    private suspend fun processStreamingQuestion(question: String, useVoice: Boolean) {
        try {
            _isStreaming.value = true
            _streamingResponse.value = ""

            // Collect streaming response
            aiService.generateStreamingResponse(
                question,
                _userHabits.value,
                _habitCompletions.value,
                _moodData.value,
                _locationData.value,
                _timePatterns.value,
                _personalizationSettings.value
            ).collect { chunk ->
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
                _moodData.value,
                _locationData.value,
                _timePatterns.value,
                _personalizationSettings.value
            )

            // Speak the response if voice is enabled
            if (useVoice) {
                speakResponse(_streamingResponse.value)
            }
        } finally {
            _isStreaming.value = false
            _isProcessing.value = false
        }
    }

    /**
     * Start voice input
     * @param continuous Whether to continuously listen for commands
     * @param useWakeWord Whether to require a wake word to process commands
     */
    fun startVoiceInput(continuous: Boolean = false, useWakeWord: Boolean = false) {
        if (_isListening.value) return

        _isListening.value = true
        _voiceInputText.value = ""

        viewModelScope.launch {
            try {
                voiceRecognitionService.startListening(
                    continuous = continuous,
                    onAmplitudeChanged = { amplitude ->
                        _voiceAmplitude.value = amplitude
                    },
                    onPartialResult = { partialText ->
                        _voiceInputText.value = partialText
                    },
                    onFinalResult = { finalText ->
                        _voiceInputText.value = finalText

                        if (finalText.isNotEmpty()) {
                            if (useWakeWord) {
                                // Check if the text contains a wake word
                                if (voiceRecognitionService.containsWakeWord(finalText)) {
                                    // Extract the command part
                                    val command = voiceRecognitionService.extractCommand(finalText)
                                    if (command.isNotEmpty()) {
                                        processVoiceInput(command)
                                    }
                                }
                            } else {
                                // Process the entire text as a command
                                processVoiceInput(finalText)
                            }
                        }

                        if (!continuous) {
                            _isListening.value = false
                        }
                    },
                    onError = { error ->
                        Log.e("AIAssistantViewModel", "Voice recognition error: $error")
                        _isListening.value = false
                    }
                )
            } catch (e: Exception) {
                Log.e("AIAssistantViewModel", "Error starting voice recognition", e)
                _isListening.value = false
            }
        }
    }

    /**
     * Stop voice input
     */
    fun stopVoiceInput() {
        if (!_isListening.value) return

        viewModelScope.launch {
            try {
                val finalText = voiceRecognitionService.stopListening()
                _isListening.value = false

                // Process the final text if it wasn't already processed
                if (finalText.isNotEmpty() && finalText == _voiceInputText.value) {
                    processVoiceInput(finalText)
                }
            } catch (e: Exception) {
                Log.e("AIAssistantViewModel", "Error stopping voice recognition", e)
                _isListening.value = false
            }
        }
    }

    /**
     * Process voice input
     */
    fun processVoiceInput(text: String) {
        if (text.isNotEmpty()) {
            askQuestion(text, useStreaming = true, useVoice = true)
        }
    }

    /**
     * Speak a response using text-to-speech
     */
    private fun speakResponse(text: String) {
        viewModelScope.launch {
            _isSpeaking.value = true
            textToSpeechService.speak(text)
            _isSpeaking.value = false
        }
    }

    /**
     * Stop speaking
     */
    fun stopSpeaking() {
        textToSpeechService.stop()
        _isSpeaking.value = false
    }

    /**
     * Clean up resources
     */
    override fun onCleared() {
        super.onCleared()
        textToSpeechService.shutdown()
        voiceRecognitionService.cleanup()
    }

    /**
     * Save personalization settings
     */
    fun savePersonalizationSettings(settings: AIAssistantPersonalization) {
        viewModelScope.launch {
            try {
                // Update local settings
                _personalizationSettings.value = settings

                // Update text-to-speech settings
                textToSpeechService.speak(settings.voiceSpeed.toString())
                textToSpeechService.speak(settings.voicePitch.toString())

                // Save to repository
                aiContextRepository.savePersonalizationSettings(settings)
            } catch (e: Exception) {
                Log.e("AIAssistantViewModel", "Error saving personalization settings", e)
            }
        }
    }
}
