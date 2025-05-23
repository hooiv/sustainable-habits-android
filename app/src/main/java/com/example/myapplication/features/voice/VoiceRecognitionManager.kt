package com.example.myapplication.features.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for voice recognition functionality
 */
@Singleton
class VoiceRecognitionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var recognitionIntent: Intent? = null
    
    // State flows
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()
    
    private val _voiceInputText = MutableStateFlow("")
    val voiceInputText: StateFlow<String> = _voiceInputText.asStateFlow()
    
    private val _voiceAmplitude = MutableStateFlow(0f)
    val voiceAmplitude: StateFlow<Float> = _voiceAmplitude.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Callback for voice recognition results
    private var onResultCallback: ((String) -> Unit)? = null
    
    // Wake word detection
    private val wakeWords = mutableListOf("hey assistant", "ok assistant", "hello assistant")
    private var useWakeWord = false
    private var continuous = false
    
    /**
     * Initialize speech recognizer
     */
    fun initialize() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _errorMessage.value = "Speech recognition is not available on this device"
            return
        }
        
        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            setupRecognitionIntent()
            setupRecognitionListener()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing speech recognizer", e)
            _errorMessage.value = "Failed to initialize speech recognition: ${e.message}"
        }
    }
    
    /**
     * Set up recognition intent
     */
    private fun setupRecognitionIntent() {
        recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000)
        }
    }
    
    /**
     * Set up recognition listener
     */
    private fun setupRecognitionListener() {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
                _errorMessage.value = null
                _voiceInputText.value = ""
            }

            override fun onBeginningOfSpeech() {
                // Speech started
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Update amplitude (normalize to 0-1 range)
                val normalizedRms = (rmsdB + 10) / 30f
                _voiceAmplitude.value = normalizedRms.coerceIn(0f, 1f)
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Buffer received
            }

            override fun onEndOfSpeech() {
                if (!continuous) {
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
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Unknown error"
                }
                
                Log.e(TAG, "Speech recognition error: $errorMessage ($error)")
                
                // Only set error if it's not a timeout in continuous mode
                if (!(continuous && error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                    _errorMessage.value = errorMessage
                }
                
                _isListening.value = false
                
                // Restart listening if in continuous mode
                if (continuous && error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    startListening(continuous, useWakeWord)
                }
            }

            override fun onResults(results: Bundle?) {
                _isListening.value = false
                
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val recognizedText = matches[0]
                    
                    if (useWakeWord) {
                        // Check if the text contains a wake word
                        val containsWakeWord = wakeWords.any { 
                            recognizedText.lowercase().contains(it) 
                        }
                        
                        if (containsWakeWord) {
                            // Remove wake word from text
                            var processedText = recognizedText
                            wakeWords.forEach { word ->
                                processedText = processedText.replace(word, "", ignoreCase = true)
                            }
                            processedText = processedText.trim()
                            
                            _voiceInputText.value = processedText
                            onResultCallback?.invoke(processedText)
                        }
                    } else {
                        _voiceInputText.value = recognizedText
                        onResultCallback?.invoke(recognizedText)
                    }
                }
                
                // Restart listening if in continuous mode
                if (continuous) {
                    startListening(continuous, useWakeWord)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val recognizedText = matches[0]
                    _voiceInputText.value = recognizedText
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Event occurred
            }
        })
    }
    
    /**
     * Start listening for voice input
     */
    fun startListening(
        continuous: Boolean = false,
        useWakeWord: Boolean = false,
        onResult: ((String) -> Unit)? = null
    ) {
        if (speechRecognizer == null) {
            initialize()
        }
        
        this.continuous = continuous
        this.useWakeWord = useWakeWord
        this.onResultCallback = onResult
        
        try {
            recognitionIntent?.let { intent ->
                speechRecognizer?.startListening(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition", e)
            _errorMessage.value = "Failed to start speech recognition: ${e.message}"
            _isListening.value = false
        }
    }
    
    /**
     * Stop listening for voice input
     */
    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            _isListening.value = false
            _voiceAmplitude.value = 0f
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping speech recognition", e)
        }
    }
    
    /**
     * Set custom wake words
     */
    fun setWakeWords(words: List<String>) {
        wakeWords.clear()
        wakeWords.addAll(words.map { it.lowercase() })
        
        // Always include default wake words
        if (!wakeWords.contains("hey assistant")) wakeWords.add("hey assistant")
        if (!wakeWords.contains("ok assistant")) wakeWords.add("ok assistant")
        if (!wakeWords.contains("hello assistant")) wakeWords.add("hello assistant")
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            _isListening.value = false
            _voiceAmplitude.value = 0f
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up speech recognizer", e)
        }
    }
    
    companion object {
        private const val TAG = "VoiceRecognitionManager"
    }
}
