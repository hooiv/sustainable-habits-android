package com.example.myapplication.features.voice

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

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    Log.d(TAG, "End of speech")
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

                override fun onEvent(eventType: Int, params: Bundle?) {}
            }

            speechRecognizer?.setRecognitionListener(recognitionListener)
            startListeningInternal()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting voice recognition: ${e.message}")
            val errorMsg = "Failed to start voice recognition: ${e.message}"
            _errorMessage.value = errorMsg
            onError?.invoke(errorMsg)
            _isListening.value = false
        }
    }

    private fun startListeningInternal() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error in startListeningInternal: ${e.message}")
            _errorMessage.value = "Error starting speech recognition: ${e.message}"
            _isListening.value = false
        }
    }

    suspend fun stopListening(): String {
        if (!_isListening.value) return lastPartialResult
        isContinuousListening = false
        _isListening.value = false
        _voiceAmplitude.value = 0f
        if (!isSimulated) {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping voice recognition: ${e.message}")
            }
        }
        return lastPartialResult
    }

    fun cleanup() {
        try {
            _isListening.value = false
            isContinuousListening = false
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
            recognitionListener = null
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up voice recognition: ${e.message}")
        }
    }

    fun containsWakeWord(text: String): Boolean {
        val lowerText = text.lowercase(Locale.ROOT)
        return wakeWords.any { lowerText.contains(it) }
    }

    fun extractCommand(text: String): String {
        val lowerText = text.lowercase(Locale.ROOT)
        val usedWakeWord = wakeWords.firstOrNull { lowerText.contains(it) } ?: return text
        val wakeWordIndex = lowerText.indexOf(usedWakeWord)
        val commandStartIndex = wakeWordIndex + usedWakeWord.length
        return if (commandStartIndex < text.length) {
            text.substring(commandStartIndex).trim()
        } else {
            ""
        }
    }

    private suspend fun simulateVoiceRecognition(
        onAmplitudeChanged: ((Float) -> Unit)? = null,
        onPartialResult: ((String) -> Unit)? = null
    ) {
        val selectedCommand = sampleCommands.random()
        lastPartialResult = ""
        delay(300)
        repeat(5) {
            val amplitude = Random.nextFloat() * 0.5f + 0.2f
            _voiceAmplitude.value = amplitude
            onAmplitudeChanged?.invoke(amplitude)
            delay(100)
        }
        val words = selectedCommand.split(" ")
        var currentText = ""
        for (word in words) {
            currentText = if (currentText.isEmpty()) word else "$currentText $word"
            lastPartialResult = currentText
            _voiceInputText.value = lastPartialResult
            onPartialResult?.invoke(lastPartialResult)
            delay(150)
        }
    }
}
