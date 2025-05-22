package com.example.myapplication.data.voice

import android.content.Context
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
            
            // Speak text
            val utteranceId = UUID.randomUUID().toString()
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            Log.d(TAG, "Speaking: $text")
        } catch (e: Exception) {
            _error.value = "Error speaking text: ${e.message}"
            Log.e(TAG, "Error speaking text", e)
        }
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
}
