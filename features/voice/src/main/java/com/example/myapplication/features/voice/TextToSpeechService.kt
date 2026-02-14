package com.example.myapplication.features.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for text-to-speech functionality
 */
@Singleton
class TextToSpeechService @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val pendingUtterances = mutableListOf<String>()

    /**
     * Initialize the text-to-speech engine
     */
    fun initialize() {
        if (tts == null) {
            tts = TextToSpeech(context, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TextToSpeechService", "Language not supported")
            } else {
                isInitialized = true
                // Speak any pending utterances
                for (text in pendingUtterances) {
                    speak(text)
                }
                pendingUtterances.clear()
            }
        } else {
            Log.e("TextToSpeechService", "Initialization failed")
        }
    }

    /**
     * Speak the given text
     */
    fun speak(text: String) {
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            pendingUtterances.add(text)
            if (tts == null) {
                initialize()
            }
        }
    }

    /**
     * Stop speaking
     */
    fun stop() {
        tts?.stop()
    }

    /**
     * Shut down the text-to-speech engine
     */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
