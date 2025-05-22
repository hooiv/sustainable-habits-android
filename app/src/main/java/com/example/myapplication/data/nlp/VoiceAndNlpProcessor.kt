package com.example.myapplication.data.nlp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implements voice recognition, text-to-speech, and natural language processing
 */
@Singleton
class VoiceAndNlpProcessor @Inject constructor(
    private val context: Context,
    private val habitRepository: com.example.myapplication.data.repository.HabitRepository
) : RecognitionListener {
    companion object {
        private const val TAG = "VoiceAndNlp"

        // Intent constants
        private const val TTS_UTTERANCE_ID = "tts_utterance"

        // NLP constants
        private val HABIT_KEYWORDS = listOf(
            "habit", "routine", "daily", "regular", "practice", "activity", "task", "goal"
        )

        private val TIME_KEYWORDS = listOf(
            "morning", "afternoon", "evening", "night", "today", "tomorrow", "yesterday",
            "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday",
            "day", "week", "month", "year", "daily", "weekly", "monthly", "yearly"
        )

        private val ACTION_KEYWORDS = mapOf(
            "create" to listOf("create", "add", "new", "start", "begin"),
            "complete" to listOf("complete", "finish", "done", "mark", "check"),
            "delete" to listOf("delete", "remove", "cancel", "stop", "end"),
            "update" to listOf("update", "change", "modify", "edit", "adjust"),
            "view" to listOf("view", "show", "see", "display", "list", "find")
        )
    }

    // Speech recognizer
    private var speechRecognizer: SpeechRecognizer? = null

    // Text-to-speech
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    // Voice recognition state
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    // NLP results
    private val _nlpIntent = MutableStateFlow<NlpIntent?>(null)
    val nlpIntent: StateFlow<NlpIntent?> = _nlpIntent.asStateFlow()

    private val _confidence = MutableStateFlow(0f)
    val confidence: StateFlow<Float> = _confidence.asStateFlow()

    // Voice commands history
    private val _commandHistory = MutableStateFlow<List<VoiceCommand>>(emptyList())
    val commandHistory: StateFlow<List<VoiceCommand>> = _commandHistory.asStateFlow()

    init {
        initializeTextToSpeech()
    }

    /**
     * Initialize text-to-speech engine
     */
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            isTtsInitialized = status == TextToSpeech.SUCCESS

            if (isTtsInitialized) {
                textToSpeech?.language = Locale.US
                textToSpeech?.setSpeechRate(1.0f)
                textToSpeech?.setPitch(1.0f)

                // Set utterance progress listener
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })

                Log.d(TAG, "Text-to-speech initialized")
            } else {
                Log.e(TAG, "Failed to initialize text-to-speech")
            }
        }
    }

    /**
     * Initialize speech recognizer
     */
    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(this)
            Log.d(TAG, "Speech recognizer initialized")
        } else {
            Log.e(TAG, "Speech recognition not available")
        }
    }

    /**
     * Start listening for voice commands
     */
    fun startListening() {
        if (_isListening.value) return

        // Initialize speech recognizer if needed
        if (speechRecognizer == null) {
            initializeSpeechRecognizer()
        }

        // Create recognition intent
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        try {
            _isListening.value = true
            _recognizedText.value = ""
            speechRecognizer?.startListening(intent)
            Log.d(TAG, "Started listening for voice commands")
        } catch (e: Exception) {
            _isListening.value = false
            Log.e(TAG, "Error starting speech recognition: ${e.message}")
        }
    }

    /**
     * Stop listening for voice commands
     */
    fun stopListening() {
        if (!_isListening.value) return

        _isListening.value = false
        speechRecognizer?.stopListening()
        Log.d(TAG, "Stopped listening for voice commands")
    }

    /**
     * Speak text using text-to-speech
     */
    fun speak(text: String) {
        if (!isTtsInitialized) {
            Log.e(TAG, "Text-to-speech not initialized")
            return
        }

        // Stop any ongoing speech
        if (_isSpeaking.value) {
            textToSpeech?.stop()
        }

        // Speak the text
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, TTS_UTTERANCE_ID)
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, TTS_UTTERANCE_ID)

        Log.d(TAG, "Speaking: $text")
    }

    /**
     * Start voice recognition
     */
    fun startVoiceRecognition() {
        if (_isListening.value) return

        try {
            // Initialize speech recognizer if needed
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer?.setRecognitionListener(this)
            }

            // Create intent for speech recognition
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }

            // Start listening
            speechRecognizer?.startListening(intent)
            _isListening.value = true

            Log.d(TAG, "Voice recognition started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting voice recognition: ${e.message}")
            _isListening.value = false
        }
    }

    /**
     * Stop voice recognition
     */
    fun stopVoiceRecognition() {
        if (!_isListening.value) return

        try {
            speechRecognizer?.stopListening()
            _isListening.value = false

            Log.d(TAG, "Voice recognition stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping voice recognition: ${e.message}")
        }
    }

    /**
     * Speak text using text-to-speech
     */
    fun speakText(text: String) {
        if (_isSpeaking.value) return

        try {
            // Initialize text-to-speech if needed
            if (textToSpeech == null) {
                textToSpeech = TextToSpeech(context) { status ->
                    isTtsInitialized = status == TextToSpeech.SUCCESS

                    if (isTtsInitialized) {
                        textToSpeech?.language = Locale.getDefault()
                    } else {
                        Log.e(TAG, "Failed to initialize TTS")
                    }
                }
            }

            // Wait for TTS to initialize
            if (!isTtsInitialized) {
                Log.e(TAG, "TTS not initialized")
                return
            }

            // Set speaking state
            _isSpeaking.value = true

            // Add utterance listener
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    // Speaking started
                }

                override fun onDone(utteranceId: String?) {
                    // Speaking completed
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?) {
                    // Error occurred
                    _isSpeaking.value = false
                    Log.e(TAG, "TTS error for utterance: $utteranceId")
                }
            })

            // Speak the text
            textToSpeech?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                TTS_UTTERANCE_ID
            )

            Log.d(TAG, "Speaking text: $text")
        } catch (e: Exception) {
            _isSpeaking.value = false
            Log.e(TAG, "Error speaking text: ${e.message}")
        }
    }

    /**
     * Process text with NLP
     */
    fun processText(text: String) {
        // Reset previous results
        _nlpIntent.value = null
        _confidence.value = 0f

        // Normalize text
        val normalizedText = text.lowercase().trim()

        // Extract intent and entities
        val (intent, entities, confidence) = extractIntentAndEntities(normalizedText)

        // Create NLP intent
        val nlpIntent = NlpIntent(
            action = intent,
            entities = entities,
            originalText = text
        )

        _nlpIntent.value = nlpIntent
        _confidence.value = confidence

        // Add to command history
        val command = VoiceCommand(
            id = UUID.randomUUID().toString(),
            text = text,
            timestamp = System.currentTimeMillis(),
            intent = nlpIntent,
            confidence = confidence
        )

        _commandHistory.value = _commandHistory.value + command

        Log.d(TAG, "Processed text: $text, Intent: $intent, Confidence: $confidence")

        // Execute the intent if confidence is high enough
        if (confidence > 0.5f) {
            executeIntent(nlpIntent)
        }
    }

    /**
     * Execute an NLP intent
     */
    private fun executeIntent(intent: NlpIntent) {
        kotlinx.coroutines.MainScope().launch {
            try {
                when (intent.action) {
                    "create" -> createHabit(intent)
                    "complete" -> completeHabit(intent)
                    "delete" -> deleteHabit(intent)
                    "update" -> updateHabit(intent)
                    "view" -> viewHabit(intent)
                    else -> {
                        Log.d(TAG, "Unknown intent action: ${intent.action}")
                        speak("I'm not sure what you want to do. Could you rephrase that?")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error executing intent: ${e.message}")
            }
        }
    }

    /**
     * Extract intent and entities from text
     */
    private fun extractIntentAndEntities(text: String): Triple<String, Map<String, String>, Float> {
        // Default values
        var intent = "unknown"
        val entities = mutableMapOf<String, String>()
        var confidence = 0.1f

        // Tokenize text
        val tokens = text.split(Regex("\\s+"))

        // Extract action intent
        for ((action, keywords) in ACTION_KEYWORDS) {
            for (keyword in keywords) {
                if (tokens.contains(keyword)) {
                    intent = action
                    confidence += 0.2f
                    break
                }
            }
        }

        // Extract habit name
        val habitKeywordIndices = tokens.mapIndexedNotNull { index, token ->
            if (HABIT_KEYWORDS.contains(token)) index else null
        }

        if (habitKeywordIndices.isNotEmpty()) {
            // Look for habit name after a habit keyword
            val habitKeywordIndex = habitKeywordIndices.first()
            if (habitKeywordIndex < tokens.size - 1) {
                // Extract potential habit name (up to 5 words after the keyword)
                val endIndex = minOf(habitKeywordIndex + 6, tokens.size)
                val habitNameTokens = tokens.subList(habitKeywordIndex + 1, endIndex)

                // Remove any action keywords from the habit name
                val filteredTokens = habitNameTokens.filter { token ->
                    ACTION_KEYWORDS.values.flatten().none { it == token }
                }

                if (filteredTokens.isNotEmpty()) {
                    entities["habit_name"] = filteredTokens.joinToString(" ")
                    confidence += 0.3f
                }
            }
        }

        // Extract time information
        for (token in tokens) {
            if (TIME_KEYWORDS.contains(token)) {
                entities["time"] = token
                confidence += 0.2f
                break
            }
        }

        // Extract frequency if present
        val frequencyPattern = Regex("(daily|weekly|monthly|yearly|every day|every week|every month|every year)")
        val frequencyMatch = frequencyPattern.find(text)
        if (frequencyMatch != null) {
            entities["frequency"] = frequencyMatch.value
            confidence += 0.2f
        }

        // Cap confidence at 1.0
        confidence = minOf(confidence, 1.0f)

        return Triple(intent, entities, confidence)
    }

    /**
     * Generate response based on NLP intent
     */
    fun generateResponse(nlpIntent: NlpIntent): String {
        return when (nlpIntent.action) {
            "create" -> {
                val habitName = nlpIntent.entities["habit_name"] ?: "new habit"
                val time = nlpIntent.entities["time"]
                val frequency = nlpIntent.entities["frequency"]

                if (time != null && frequency != null) {
                    "I'll create a $habitName habit for you, scheduled for $time, $frequency."
                } else if (time != null) {
                    "I'll create a $habitName habit for you, scheduled for $time."
                } else if (frequency != null) {
                    "I'll create a $habitName habit for you, with $frequency frequency."
                } else {
                    "I'll create a $habitName habit for you."
                }
            }
            "complete" -> {
                val habitName = nlpIntent.entities["habit_name"] ?: "the habit"
                "I'll mark $habitName as completed."
            }
            "delete" -> {
                val habitName = nlpIntent.entities["habit_name"] ?: "the habit"
                "Are you sure you want to delete $habitName? Please confirm."
            }
            "update" -> {
                val habitName = nlpIntent.entities["habit_name"] ?: "the habit"
                "I'll update $habitName for you."
            }
            "view" -> {
                val habitName = nlpIntent.entities["habit_name"]
                if (habitName != null) {
                    "Here's the information for $habitName."
                } else {
                    "Here are your habits."
                }
            }
            else -> "I'm not sure what you want to do. Could you rephrase that?"
        }
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        speechRecognizer?.destroy()
        speechRecognizer = null

        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null

        Log.d(TAG, "Resources cleaned up")
    }

    // RecognitionListener implementation
    override fun onReadyForSpeech(params: Bundle?) {
        Log.d(TAG, "Ready for speech")
    }

    override fun onBeginningOfSpeech() {
        Log.d(TAG, "Beginning of speech")
    }

    override fun onRmsChanged(rmsdB: Float) {
        // Not used
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        // Not used
    }

    override fun onEndOfSpeech() {
        Log.d(TAG, "End of speech")
    }

    override fun onError(error: Int) {
        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }

        Log.e(TAG, "Error in speech recognition: $errorMessage")
        _isListening.value = false
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val text = matches[0]
            _recognizedText.value = text
            _isListening.value = false

            // Process the recognized text
            processText(text)

            Log.d(TAG, "Speech recognition result: $text")
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val text = matches[0]
            _recognizedText.value = text
            Log.d(TAG, "Partial speech recognition result: $text")
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        // Not used
    }

    /**
     * Create a habit from NLP intent
     */
    private suspend fun createHabit(intent: NlpIntent) {
        val habitName = intent.entities["habit_name"] ?: "New Habit"
        val time = intent.entities["time"]
        val frequencyStr = intent.entities["frequency"]

        // Convert string frequency to HabitFrequency enum
        val frequency = when (frequencyStr?.lowercase()) {
            "daily" -> com.example.myapplication.data.model.HabitFrequency.DAILY
            "weekly" -> com.example.myapplication.data.model.HabitFrequency.WEEKLY
            "monthly" -> com.example.myapplication.data.model.HabitFrequency.MONTHLY
            else -> com.example.myapplication.data.model.HabitFrequency.DAILY
        }

        // Create a new habit
        val habit = com.example.myapplication.data.model.Habit(
            id = UUID.randomUUID().toString(),
            name = habitName,
            description = "Created via voice command",
            category = "other",
            frequency = frequency,
            streak = 0,
            createdDate = Date(),
            reminderTime = time,
            isEnabled = true
        )

        // Save the habit
        habitRepository.insertHabit(habit)

        // Generate response
        val response = generateResponse(intent)
        speak(response)

        Log.d(TAG, "Created habit: $habitName")
    }

    /**
     * Complete a habit from NLP intent
     */
    private suspend fun completeHabit(intent: NlpIntent) {
        val habitName = intent.entities["habit_name"]

        // Find the habit by name
        val habitsFlow = habitRepository.getAllHabits()
        val habitsList = mutableListOf<com.example.myapplication.data.model.Habit>()

        // Collect the habits from the flow
        habitsFlow.collect { habits ->
            habitsList.addAll(habits)
        }

        val habitObj = if (habitName != null) {
            habitsList.find { habitItem -> habitItem.name.lowercase().contains(habitName.lowercase()) }
        } else {
            // If no habit name specified, use the most recently created habit
            habitsList.maxByOrNull { habitItem -> habitItem.createdDate.time }
        }

        if (habitObj != null) {
            // Create a completion
            val completion = com.example.myapplication.data.model.HabitCompletion(
                id = UUID.randomUUID().toString(),
                habitId = habitObj.id,
                completionDate = System.currentTimeMillis(),
                note = "Completed via voice command"
            )

            // Mark the habit as completed
            habitRepository.markHabitCompleted(habitObj.id)

            // Generate response
            val response = "I've marked ${habitObj.name} as completed. Your streak is now ${habitObj.streak + 1}."
            speak(response)

            Log.d(TAG, "Completed habit: ${habitObj.name}")
        } else {
            speak("I couldn't find that habit. Please try again with a specific habit name.")
            Log.d(TAG, "Habit not found for completion")
        }
    }

    /**
     * Delete a habit from NLP intent
     */
    private suspend fun deleteHabit(intent: NlpIntent) {
        val habitName = intent.entities["habit_name"]

        // Find the habit by name
        val habitsFlow = habitRepository.getAllHabits()
        val habitsList = mutableListOf<com.example.myapplication.data.model.Habit>()

        // Collect the habits from the flow
        habitsFlow.collect { habits ->
            habitsList.addAll(habits)
        }

        val habitObj = if (habitName != null) {
            habitsList.find { habitItem -> habitItem.name.lowercase().contains(habitName.lowercase()) }
        } else {
            null
        }

        if (habitObj != null) {
            // Use the copy method to create a new habit with isEnabled set to false
            val disabledHabit = habitObj.copy(
                isEnabled = false,
                lastUpdatedTimestamp = Date()
            )
            habitRepository.updateHabit(disabledHabit)

            // Generate response
            val response = "I've disabled the habit ${habitObj.name}."
            speak(response)

            Log.d(TAG, "Disabled habit: ${habitObj.name}")
        } else {
            speak("I couldn't find that habit. Please try again with a specific habit name.")
            Log.d(TAG, "Habit not found for deletion")
        }
    }

    /**
     * Update a habit from NLP intent
     */
    private suspend fun updateHabit(intent: NlpIntent) {
        val habitName = intent.entities["habit_name"]
        val time = intent.entities["time"]
        val frequencyStr = intent.entities["frequency"]

        // Find the habit by name
        val habitsFlow = habitRepository.getAllHabits()
        val habitsList = mutableListOf<com.example.myapplication.data.model.Habit>()

        // Collect the habits from the flow
        habitsFlow.collect { habits ->
            habitsList.addAll(habits)
        }

        val habitObj = if (habitName != null) {
            habitsList.find { habitItem -> habitItem.name.lowercase().contains(habitName.lowercase()) }
        } else {
            // If no habit name specified, use the most recently updated habit
            habitsList.maxByOrNull { habitItem -> habitItem.lastUpdatedTimestamp.time }
        }

        if (habitObj != null) {
            // Convert string frequency to HabitFrequency enum if provided
            val frequency = if (frequencyStr != null) {
                when (frequencyStr.lowercase()) {
                    "daily" -> com.example.myapplication.data.model.HabitFrequency.DAILY
                    "weekly" -> com.example.myapplication.data.model.HabitFrequency.WEEKLY
                    "monthly" -> com.example.myapplication.data.model.HabitFrequency.MONTHLY
                    else -> habitObj.frequency
                }
            } else {
                habitObj.frequency
            }

            // Use the copy method to update the habit
            val updatedHabit = habitObj.copy(
                frequency = frequency,
                reminderTime = time ?: habitObj.reminderTime,
                lastUpdatedTimestamp = Date()
            )
            habitRepository.updateHabit(updatedHabit)

            // Generate response
            val response = "I've updated the habit ${habitObj.name}."
            speak(response)

            Log.d(TAG, "Updated habit: ${habitObj.name}")
        } else {
            speak("I couldn't find that habit. Please try again with a specific habit name.")
            Log.d(TAG, "Habit not found for update")
        }
    }

    /**
     * View a habit from NLP intent
     */
    private suspend fun viewHabit(intent: NlpIntent) {
        val habitName = intent.entities["habit_name"]

        // Find the habit by name
        val habitsFlow = habitRepository.getAllHabits()
        val habitsList = mutableListOf<com.example.myapplication.data.model.Habit>()

        // Collect the habits from the flow
        habitsFlow.collect { habits ->
            habitsList.addAll(habits)
        }

        if (habitName != null) {
            val matchingHabit = habitsList.find { habitItem -> habitItem.name.lowercase().contains(habitName.lowercase()) }

            if (matchingHabit != null) {
                // Get completions for the habit
                val completionsFlow = habitRepository.getHabitCompletions(matchingHabit.id)
                val completionsList = mutableListOf<com.example.myapplication.data.model.HabitCompletion>()

                // Collect the completions from the flow
                completionsFlow.collect { completions ->
                    completionsList.addAll(completions)
                }

                // Generate response
                val response = "The habit ${matchingHabit.name} has a streak of ${matchingHabit.streak} days " +
                        "and has been completed ${completionsList.size} times in total."
                speak(response)

                Log.d(TAG, "Viewed habit: ${matchingHabit.name}")
            } else {
                speak("I couldn't find a habit with that name.")
                Log.d(TAG, "Habit not found for viewing")
            }
        } else {
            // List all habits
            val activeHabits = habitsList.filter { habitItem: com.example.myapplication.data.model.Habit -> habitItem.isEnabled }

            if (activeHabits.isNotEmpty()) {
                val habitNamesList = mutableListOf<String>()
                for (habitItem in activeHabits) {
                    habitNamesList.add(habitItem.name)
                }
                val habitNamesString = habitNamesList.joinToString(", ")
                speak("You have ${activeHabits.size} active habits: $habitNamesString")

                Log.d(TAG, "Listed all habits")
            } else {
                speak("You don't have any active habits yet. Would you like to create one?")
                Log.d(TAG, "No habits found")
            }
        }
    }
}

/**
 * NLP intent extracted from text
 */
data class NlpIntent(
    val action: String,
    val entities: Map<String, String>,
    val originalText: String
)

/**
 * Voice command with NLP processing results
 */
data class VoiceCommand(
    val id: String,
    val text: String,
    val timestamp: Long,
    val intent: NlpIntent,
    val confidence: Float
)
