package com.example.myapplication.features.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.model.EntityType
import com.example.myapplication.core.data.model.VoiceCommand
import com.example.myapplication.core.data.model.VoiceEntity
import com.example.myapplication.core.data.model.VoiceIntent
import com.example.myapplication.core.data.repository.HabitRepository
import com.example.myapplication.features.voice.VoiceRecognitionService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

/**
 * ViewModel for the Voice Integration screen
 */
@HiltViewModel
class VoiceIntegrationViewModel @Inject constructor(
    private val voiceRecognitionService: VoiceRecognitionService,
    private val habitRepository: HabitRepository
) : ViewModel() {

    // Voice recognition state
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _voiceAmplitude = MutableStateFlow(0f)
    val voiceAmplitude: StateFlow<Float> = _voiceAmplitude.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _lastCommand = MutableStateFlow<VoiceCommand?>(null)
    val lastCommand: StateFlow<VoiceCommand?> = _lastCommand.asStateFlow()

    private val _availableCommands = MutableStateFlow<List<String>>(emptyList())
    val availableCommands: StateFlow<List<String>> = _availableCommands.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadAvailableCommands()
    }

    /**
     * Load available voice commands
     */
    private fun loadAvailableCommands() {
        _availableCommands.value = listOf(
            "Create a new habit to [activity] every day",
            "Complete my [habit name] habit for today",
            "Show me my [habit name] statistics",
            "Set a reminder for my [habit name] habit at [time]",
            "What's my progress on [habit name]?",
            "How many days streak do I have for [habit name]?",
            "When is my next [habit name] scheduled?",
            "Skip my [habit name] habit for today"
        )
    }

    /**
     * Start listening for voice commands
     */
    suspend fun startListening() {
        try {
            _isListening.value = true
            _recognizedText.value = ""
            _lastCommand.value = null

            // Start voice recognition service
            voiceRecognitionService.startListening(
                onAmplitudeChanged = { amplitude ->
                    _voiceAmplitude.value = amplitude
                },
                onPartialResult = { text ->
                    _recognizedText.value = text
                },
                onError = { error ->
                    _errorMessage.value = "Voice recognition error: $error"
                    _isListening.value = false
                }
            )

            // Simulate amplitude changes
            viewModelScope.launch {
                while (_isListening.value) {
                    _voiceAmplitude.value = Random.nextFloat() * 0.8f + 0.2f
                    delay(100)
                }
            }
        } catch (e: Exception) {
            _errorMessage.value = "Failed to start voice recognition: ${e.message}"
            _isListening.value = false
        }
    }

    /**
     * Stop listening for voice commands
     */
    suspend fun stopListening() {
        try {
            _isListening.value = false
            _isProcessing.value = true

            // Stop voice recognition service
            val finalText = voiceRecognitionService.stopListening()
            _recognizedText.value = finalText

            // Process the command
            delay(1000) // Simulate processing time

            // Generate command
            val command = generateVoiceCommand(finalText)
            _lastCommand.value = command

            // Execute the command
            executeVoiceCommand(command)

            _isProcessing.value = false
        } catch (e: Exception) {
            _errorMessage.value = "Failed to process voice command: ${e.message}"
            _isProcessing.value = false
        }
    }

    /**
     * Generate a voice command from text
     */
    private fun generateVoiceCommand(text: String): VoiceCommand {
        // Determine intent
        val intent = when {
            text.contains("create", ignoreCase = true) -> VoiceIntent.CREATE_HABIT
            text.contains("complete", ignoreCase = true) -> VoiceIntent.COMPLETE_HABIT
            text.contains("show", ignoreCase = true) || text.contains("view", ignoreCase = true) -> {
                if (text.contains("statistics", ignoreCase = true) || text.contains("stats", ignoreCase = true)) {
                    VoiceIntent.VIEW_STATS
                } else {
                    VoiceIntent.VIEW_HABIT
                }
            }
            text.contains("reminder", ignoreCase = true) || text.contains("set", ignoreCase = true) -> VoiceIntent.SET_REMINDER
            text.contains("progress", ignoreCase = true) -> VoiceIntent.CHECK_PROGRESS
            text.contains("streak", ignoreCase = true) -> VoiceIntent.VIEW_STATS
            text.contains("next", ignoreCase = true) || text.contains("scheduled", ignoreCase = true) -> VoiceIntent.VIEW_HABIT
            text.contains("skip", ignoreCase = true) -> VoiceIntent.COMPLETE_HABIT
            else -> VoiceIntent.UNKNOWN
        }

        // Extract entities
        val entities = mutableListOf<VoiceEntity>()

        // Extract habit name
        val habitNameRegex = when (intent) {
            VoiceIntent.CREATE_HABIT -> "create a new habit to (.*?)(every|daily|weekly|monthly|at|$)".toRegex(RegexOption.IGNORE_CASE)
            VoiceIntent.COMPLETE_HABIT -> "complete my (.*?) (habit|for|$)".toRegex(RegexOption.IGNORE_CASE)
            VoiceIntent.VIEW_HABIT -> "show me my (.*?) (habit|$)".toRegex(RegexOption.IGNORE_CASE)
            VoiceIntent.VIEW_STATS -> "show me my (.*?) (stats|statistics|$)".toRegex(RegexOption.IGNORE_CASE)
            VoiceIntent.SET_REMINDER -> "set a reminder for my (.*?) (habit|at|$)".toRegex(RegexOption.IGNORE_CASE)
            VoiceIntent.CHECK_PROGRESS -> "progress on (.*?)($|\\?)".toRegex(RegexOption.IGNORE_CASE)
            else -> "(.*?)".toRegex()
        }

        habitNameRegex.find(text)?.let { matchResult ->
            val habitName = matchResult.groupValues[1].trim()
            if (habitName.isNotEmpty()) {
                entities.add(
                    VoiceEntity(
                        type = EntityType.HABIT_NAME,
                        value = habitName,
                        confidence = 0.8f + Random.nextFloat() * 0.2f
                    )
                )
            }
        }

        // Extract frequency for CREATE_HABIT
        if (intent == VoiceIntent.CREATE_HABIT) {
            val frequencyRegex = "every (day|week|month|morning|evening|night|daily|weekly|monthly)".toRegex(RegexOption.IGNORE_CASE)
            frequencyRegex.find(text)?.let { matchResult ->
                val frequency = matchResult.groupValues[1].trim()
                entities.add(
                    VoiceEntity(
                        type = EntityType.FREQUENCY,
                        value = frequency,
                        confidence = 0.85f + Random.nextFloat() * 0.15f
                    )
                )
            }
        }

        // Extract time for SET_REMINDER
        if (intent == VoiceIntent.SET_REMINDER) {
            val timeRegex = "at (\\d{1,2}(:\\d{2})? ?[ap]m|\\d{1,2}(:\\d{2})?)".toRegex(RegexOption.IGNORE_CASE)
            timeRegex.find(text)?.let { matchResult ->
                val time = matchResult.groupValues[1].trim()
                entities.add(
                    VoiceEntity(
                        type = EntityType.TIME,
                        value = time,
                        confidence = 0.9f + Random.nextFloat() * 0.1f
                    )
                )
            }
        }

        return VoiceCommand(
            text = text,
            intent = intent,
            entities = entities,
            confidence = 0.7f + Random.nextFloat() * 0.3f,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * Execute a voice command
     */
    private suspend fun executeVoiceCommand(command: VoiceCommand) {
        try {
            // Simulate command execution
            delay(500)

            // In a real implementation, this would interact with the repository
            // to perform the requested action
            when (command.intent) {
                VoiceIntent.CREATE_HABIT -> {
                    // Create a new habit
                }
                VoiceIntent.COMPLETE_HABIT -> {
                    // Complete a habit
                }
                VoiceIntent.VIEW_HABIT -> {
                    // View habit
                }
                VoiceIntent.VIEW_STATS -> {
                    // View stats
                }
                VoiceIntent.SET_REMINDER -> {
                    // Set a reminder
                }
                VoiceIntent.CHECK_PROGRESS -> {
                    // Check progress
                }
                else -> {
                    _errorMessage.value = "Sorry, I didn't understand that command."
                }
            }
        } catch (e: Exception) {
            _errorMessage.value = "Failed to execute command: ${e.message}"
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
