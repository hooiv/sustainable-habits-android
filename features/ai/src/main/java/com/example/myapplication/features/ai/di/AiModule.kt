package com.example.myapplication.features.ai.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideAiSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("ai_assistant_prefs", Context.MODE_PRIVATE)
    }
}
