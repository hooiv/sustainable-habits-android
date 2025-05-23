package com.example.myapplication.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Service for voice recognition
 */
@Singleton
class VoiceRecognitionService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "VoiceRecognitionService"
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var recognitionListener: RecognitionListener? = null
    private var lastPartialResult = ""
    private var isSimulated = false // Set to false to use real voice recognition
    private var isContinuousListening = false

    // State flows for reactive UI updates
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _voiceInputText = MutableStateFlow("")
    val voiceInputText: StateFlow<String> = _voiceInputText.asStateFlow()

    private val _voiceAmplitude = MutableStateFlow(0f)
    val voiceAmplitude: StateFlow<Float> = _voiceAmplitude.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Wake words/trigger phrases
    private val wakeWords = listOf(
        "hey assistant", "ok assistant", "hello assistant",
        "hey habit", "ok habit", "hello habit"
    )

    // Sample commands for simulation
    private val sampleCommands = listOf(
        "Create a new habit to drink water every day",
        "Complete my meditation habit for today",
        "Show me my exercise statistics",
        "Set a reminder for my reading habit at 9 PM",
        "What's my progress on yoga?",
        "How many days streak do I have for running?",
        "When is my next journaling scheduled?",
        "Skip my guitar practice habit for today"
    )

    /**
     * Start listening for voice commands
     * @param continuous Whether to continuously listen for commands
     * @param useWakeWord Whether to use wake word detection
     * @param onAmplitudeChanged Callback for amplitude changes (optional)
     * @param onPartialResult Callback for partial recognition results (optional)
     * @param onFinalResult Callback for final recognition results (optional)
     * @param onError Callback for errors (optional)
     */
    suspend fun startListening(
        continuous: Boolean = false,
        useWakeWord: Boolean = false,
        onAmplitudeChanged: ((Float) -> Unit)? = null,
        onPartialResult: ((String) -> Unit)? = null,
        onFinalResult: ((String) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        if (_isListening.value) {
            Log.d(TAG, "Already listening, ignoring request")
            return
        }

        _isListening.value = true
        _voiceInputText.value = ""
        _errorMessage.value = null
        isContinuousListening = continuous

        if (isSimulated) {
            // Simulate voice recognition
            simulateVoiceRecognition(
                onAmplitudeChanged = { amplitude ->
                    _voiceAmplitude.value = amplitude
                    onAmplitudeChanged?.invoke(amplitude)
                },
                onPartialResult = { text ->
                    _voiceInputText.value = text
                    onPartialResult?.invoke(text)
                }
            )
            onFinalResult?.invoke(lastPartialResult)
            _isListening.value = false
            return
        }

        try {
            // Initialize speech recognizer if needed
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            }

            // Create recognition listener
            recognitionListener = object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d(TAG, "Ready for speech")
                }

                override fun onBeginningOfSpeech() {
                    Log.d(TAG, "Beginning of speech")
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Convert RMS to amplitude (0-1 range)
                    val amplitude = (rmsdB + 10) / 30f
                    val normalizedAmplitude = amplitude.coerceIn(0f, 1f)
                    _voiceAmplitude.value = normalizedAmplitude
                    onAmplitudeChanged?.invoke(normalizedAmplitude)
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                    // Not used
                }

                override fun onEndOfSpeech() {
                    Log.d(TAG, "End of speech")

                    // If continuous listening is enabled, restart listening
                    if (isContinuousListening && _isListening.value) {
                        startListeningInternal()
                    } else {
                        _isListening.value = false
                    }
                }

                override fun onError(error: Int) {
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No recognition match"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                        else -> "Unknown error"
                    }
                    Log.e(TAG, "Error: $errorMessage")

                    // For some errors, we want to restart listening in continuous mode
                    if (isContinuousListening && _isListening.value &&
                        (error == SpeechRecognizer.ERROR_NO_MATCH ||
                         error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                        startListeningInternal()
                    } else {
                        _errorMessage.value = errorMessage
                        onError?.invoke(errorMessage)
                        _isListening.value = false
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0]
                        lastPartialResult = text
                        _voiceInputText.value = text
                        onFinalResult?.invoke(text)

                        // If continuous listening is enabled, restart listening
                        if (isContinuousListening && _isListening.value) {
                            startListeningInternal()
                        }
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0]
                        lastPartialResult = text
                        _voiceInputText.value = text
                        onPartialResult?.invoke(text)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {
                    // Not used
                }
            }

            // Set recognition listener
            speechRecognizer?.setRecognitionListener(recognitionListener)

            // Start listening
            startListeningInternal()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting voice recognition: ${e.message}")
            val errorMsg = "Failed to start voice recognition: ${e.message}"
            _errorMessage.value = errorMsg
            onError?.invoke(errorMsg)
            _isListening.value = false
        }
    }

    /**
     * Internal method to start the speech recognizer
     */
    private fun startListeningInternal() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error in startListeningInternal: ${e.message}")
            _errorMessage.value = "Error starting speech recognition: ${e.message}"
            _isListening.value = false
        }
    }

    /**
     * Stop listening for voice commands
     * @return The last recognized text
     */
    suspend fun stopListening(): String {
        if (!_isListening.value) {
            return lastPartialResult
        }

        // Set flags to stop continuous listening
        isContinuousListening = false
        _isListening.value = false
        _voiceAmplitude.value = 0f

        if (isSimulated) {
            // Return the last simulated result
            return lastPartialResult
        }

        try {
            speechRecognizer?.stopListening()

            // Don't destroy the recognizer, just stop it
            // This allows for faster restart if needed
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping voice recognition: ${e.message}")
            _errorMessage.value = "Error stopping speech recognition: ${e.message}"
        }

        return lastPartialResult
    }

    /**
     * Clean up resources
     * Call this when the service is no longer needed
     */
    fun cleanup() {
        try {
            _isListening.value = false
            isContinuousListening = false
            _voiceAmplitude.value = 0f

            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
            recognitionListener = null

            Log.d(TAG, "Voice recognition resources cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up voice recognition: ${e.message}")
            _errorMessage.value = "Error cleaning up speech recognition: ${e.message}"
        }
    }

    /**
     * Check if the text contains a wake word
     * @param text The text to check
     * @return True if a wake word is found, false otherwise
     */
    fun containsWakeWord(text: String): Boolean {
        val lowerText = text.toLowerCase(Locale.ROOT)
        return wakeWords.any { lowerText.contains(it) }
    }

    /**
     * Extract command from text containing a wake word
     * @param text The text containing a wake word
     * @return The command part of the text (without the wake word)
     */
    fun extractCommand(text: String): String {
        val lowerText = text.toLowerCase(Locale.ROOT)

        // Find the wake word that was used
        val usedWakeWord = wakeWords.firstOrNull { lowerText.contains(it) } ?: return text

        // Find where the wake word ends
        val wakeWordIndex = lowerText.indexOf(usedWakeWord)
        val commandStartIndex = wakeWordIndex + usedWakeWord.length

        // Extract the command part (if any)
        return if (commandStartIndex < text.length) {
            text.substring(commandStartIndex).trim()
        } else {
            "" // No command, just the wake word
        }
    }

    /**
     * Simulate voice recognition
     */
    private suspend fun simulateVoiceRecognition(
        onAmplitudeChanged: ((Float) -> Unit)? = null,
        onPartialResult: ((String) -> Unit)? = null
    ) {
        // Select a random command
        val selectedCommand = sampleCommands.random()
        lastPartialResult = ""

        // Simulate "thinking" before starting to recognize
        delay(300)

        // Simulate amplitude changes during "listening"
        repeat(5) {
            val amplitude = Random.nextFloat() * 0.5f + 0.2f
            _voiceAmplitude.value = amplitude
            onAmplitudeChanged?.invoke(amplitude)
            delay(100)
        }

        // Simulate beginning of speech
        _voiceAmplitude.value = 0.7f
        onAmplitudeChanged?.invoke(0.7f)
        delay(200)

        // Simulate typing with word-by-word recognition
        val words = selectedCommand.split(" ")
        var currentText = ""

        for (word in words) {
            // Simulate amplitude changes during word
            repeat(3) {
                val amplitude = Random.nextFloat() * 0.8f + 0.2f
                _voiceAmplitude.value = amplitude
                onAmplitudeChanged?.invoke(amplitude)
                delay(50)
            }

            // Update partial result with new word
            currentText = if (currentText.isEmpty()) word else "$currentText $word"
            lastPartialResult = currentText
            _voiceInputText.value = lastPartialResult
            onPartialResult?.invoke(lastPartialResult)

            // Delay between words
            delay(150)
        }

        // Simulate end of speech
        _voiceAmplitude.value = 0.1f
        onAmplitudeChanged?.invoke(0.1f)
        delay(200)
    }
}
