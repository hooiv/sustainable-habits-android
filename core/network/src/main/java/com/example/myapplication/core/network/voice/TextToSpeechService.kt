package com.example.myapplication.core.network.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for text-to-speech
 */
@Singleton
class TextToSpeechService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "TextToSpeechService"
    }

    // Text-to-speech engine
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    // State
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Initialize text-to-speech engine
     */
    fun initialize() {
        if (textToSpeech == null) {
            textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = textToSpeech?.setLanguage(Locale.US)

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        _error.value = "Language not supported"
                        Log.e(TAG, "Language not supported")
                    } else {
                        isInitialized = true
                        Log.d(TAG, "Text-to-speech initialized")

                        // Set utterance progress listener
                        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                _isSpeaking.value = true
                                Log.d(TAG, "Started speaking: $utteranceId")
                            }

                            override fun onDone(utteranceId: String?) {
                                _isSpeaking.value = false
                                Log.d(TAG, "Finished speaking: $utteranceId")
                            }

                            override fun onError(utteranceId: String?) {
                                _isSpeaking.value = false
                                _error.value = "Error speaking text"
                                Log.e(TAG, "Error speaking: $utteranceId")
                            }
                        })
                    }
                } else {
                    _error.value = "Failed to initialize text-to-speech"
                    Log.e(TAG, "Failed to initialize text-to-speech: $status")
                }
            }
        }
    }

    /**
     * Speak text
     */
    fun speak(text: String) {
        if (!isInitialized) {
            initialize()
            // Wait for initialization
            return
        }

        try {
            // Stop any current speech
            stop()

            // Split text into smaller chunks if it's too long
            val chunks = splitTextIntoChunks(text)
            val baseUtteranceId = UUID.randomUUID().toString()

            // Speak the first chunk
            if (chunks.isNotEmpty()) {
                // Create a bundle for params
                val params = Bundle()
                params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, baseUtteranceId)

                textToSpeech?.speak(chunks[0], TextToSpeech.QUEUE_FLUSH, params, baseUtteranceId)

                // Queue the rest of the chunks
                for (i in 1 until chunks.size) {
                    val chunkId = "$baseUtteranceId-$i"
                    textToSpeech?.speak(chunks[i], TextToSpeech.QUEUE_ADD, params, chunkId)
                }

                Log.d(TAG, "Speaking text in ${chunks.size} chunks")
            }
        } catch (e: Exception) {
            _error.value = "Error speaking text: ${e.message}"
            Log.e(TAG, "Error speaking text", e)
        }
    }

    /**
     * Split text into smaller chunks for better TTS performance
     */
    private fun splitTextIntoChunks(text: String, maxChunkSize: Int = 250): List<String> {
        if (text.length <= maxChunkSize) {
            return listOf(text)
        }

        val chunks = mutableListOf<String>()
        var start = 0

        while (start < text.length) {
            var end = minOf(start + maxChunkSize, text.length)

            // Try to find a sentence end or punctuation
            if (end < text.length) {
                val possibleBreaks = listOf(". ", "! ", "? ", ".\n", "!\n", "?\n", "\n\n")

                // Look for a good breaking point
                var breakPoint = -1
                for (breakChar in possibleBreaks) {
                    val lastIndex = text.lastIndexOf(breakChar, end)
                    if (lastIndex > start && (breakPoint == -1 || lastIndex > breakPoint)) {
                        breakPoint = lastIndex + breakChar.length - 1
                    }
                }

                // If we found a good breaking point, use it
                if (breakPoint > start) {
                    end = breakPoint + 1
                } else {
                    // Otherwise, try to break at a space
                    val lastSpace = text.lastIndexOf(' ', end)
                    if (lastSpace > start) {
                        end = lastSpace + 1
                    }
                }
            }

            chunks.add(text.substring(start, end))
            start = end
        }

        return chunks
    }

    /**
     * Stop speaking
     */
    fun stop() {
        try {
            textToSpeech?.stop()
            _isSpeaking.value = false
            Log.d(TAG, "Stopped speaking")
        } catch (e: Exception) {
            _error.value = "Error stopping speech: ${e.message}"
            Log.e(TAG, "Error stopping speech", e)
        }
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
            isInitialized = false
            Log.d(TAG, "Text-to-speech shutdown")
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down text-to-speech", e)
        }
    }

    /**
     * Alias for cleanup to match the interface used in AIAssistantViewModel
     */
    fun shutdown() {
        cleanup()
    }
}
