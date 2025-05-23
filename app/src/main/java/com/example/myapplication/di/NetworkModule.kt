package com.example.myapplication.di

import com.example.myapplication.data.ai.AIService
import com.example.myapplication.data.ai.LocalAIService
import com.example.myapplication.data.ai.OpenAIApiClient
import com.example.myapplication.data.ai.OpenAIService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Network module for dependency injection
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val OPENAI_BASE_URL = "https://api.openai.com/"

    /**
     * Provides Gson instance
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    /**
     * Provides OkHttpClient instance
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provides Retrofit instance for OpenAI
     */
    @Provides
    @Singleton
    fun provideOpenAIRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(OPENAI_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Provides OpenAI API client
     */
    @Provides
    @Singleton
    fun provideOpenAIApiClient(retrofit: Retrofit): OpenAIApiClient {
        return retrofit.create(OpenAIApiClient::class.java)
    }

    /**
     * Provides OpenAI service implementation
     */
    @Provides
    @Singleton
    @OpenAIImpl
    fun provideOpenAIService(openAIApiClient: OpenAIApiClient, gson: Gson): AIService {
        return OpenAIService(openAIApiClient, gson)
    }

    /**
     * Provides Local AI service implementation
     */
    @Provides
    @Singleton
    @LocalImpl
    fun provideLocalAIService(): AIService {
        return LocalAIService()
    }

    /**
     * Provides default AI service implementation
     * Currently using LocalAIService as default to avoid API costs
     * Change to @OpenAIImpl to use OpenAI API
     */
    @Provides
    @Singleton
    fun provideDefaultAIService(@LocalImpl localAIService: AIService): AIService {
        return localAIService
    }
}

/**
 * Qualifier for OpenAI implementation
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenAIImpl

/**
 * Qualifier for Local implementation
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LocalImpl
