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
    private var isSimulated = true // Set to true to use simulated results

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
        onAmplitudeChanged: (Float) -> Unit,
        onPartialResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (isSimulated) {
            // Simulate voice recognition
            simulateVoiceRecognition(onAmplitudeChanged, onPartialResult)
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
                    onAmplitudeChanged(amplitude.coerceIn(0f, 1f))
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
                        SpeechRecognizer.ERROR_NO_MATCH -> "No recognition match"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                        else -> "Unknown error"
                    }
                    Log.e(TAG, "Error: $errorMessage")
                    onError(errorMessage)
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0]
                        lastPartialResult = text
                        onPartialResult(text)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0]
                        lastPartialResult = text
                        onPartialResult(text)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {
                    // Not used
                }
            }

            // Set recognition listener
            speechRecognizer?.setRecognitionListener(recognitionListener)

            // Start listening
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting voice recognition: ${e.message}")
            onError("Failed to start voice recognition: ${e.message}")
        }
    }

    /**
     * Stop listening for voice commands
     */
    suspend fun stopListening(): String {
        if (isSimulated) {
            // Return the last simulated result
            return lastPartialResult
        }

        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
            recognitionListener = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping voice recognition: ${e.message}")
        }

        return lastPartialResult
    }

    /**
     * Simulate voice recognition
     */
    private suspend fun simulateVoiceRecognition(
        onAmplitudeChanged: (Float) -> Unit,
        onPartialResult: (String) -> Unit
    ) {
        // Select a random command
        val selectedCommand = sampleCommands.random()
        lastPartialResult = ""

        // Simulate typing
        for (i in selectedCommand.indices) {
            // Simulate amplitude changes
            val amplitude = Random.nextFloat() * 0.8f + 0.2f
            onAmplitudeChanged(amplitude)

            // Update partial result
            lastPartialResult = selectedCommand.substring(0, i + 1)
            onPartialResult(lastPartialResult)

            // Delay for typing simulation
            delay(50)
        }
    }
}
