package com.example.myapplication.features.voice

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VoiceModule {

    @Provides
    @Singleton
    fun provideVoiceRecognitionService(@ApplicationContext context: Context): VoiceRecognitionService {
        return VoiceRecognitionService(context)
    }

    @Provides
    @Singleton
    fun provideTextToSpeechService(@ApplicationContext context: Context): TextToSpeechService {
        return TextToSpeechService(context)
    }
}
