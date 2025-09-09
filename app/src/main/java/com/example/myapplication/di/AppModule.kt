package com.example.myapplication.di

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.data.voice.TextToSpeechService
import com.example.myapplication.data.voice.VoiceRecognitionService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext appContext: Context): Context {
        return appContext
    }

    @Provides
    @Singleton
    fun provideTextToSpeechService(@ApplicationContext context: Context): TextToSpeechService {
        return TextToSpeechService(context)
    }

    @Provides
    @Singleton
    fun provideVoiceRecognitionService(@ApplicationContext context: Context): VoiceRecognitionService {
        return VoiceRecognitionService(context)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("ai_assistant_prefs", Context.MODE_PRIVATE)
    }
}
