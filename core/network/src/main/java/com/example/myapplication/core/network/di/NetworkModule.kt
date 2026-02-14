package com.example.myapplication.core.network.di

import com.example.myapplication.core.network.ai.AIService
import com.example.myapplication.core.network.ai.LocalAIService
import com.example.myapplication.core.network.ai.OpenAIApiClient
import com.example.myapplication.core.network.ai.OpenAIService
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
 * Hilt module for providing network-related dependencies.
 * Lives in core:network where the API clients are defined.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val OPENAI_BASE_URL = "https://api.openai.com/"

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

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

    @Provides
    @Singleton
    fun provideOpenAIRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(OPENAI_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenAIApiClient(retrofit: Retrofit): OpenAIApiClient {
        return retrofit.create(OpenAIApiClient::class.java)
    }

    @Provides
    @Singleton
    @OpenAIImpl
    fun provideOpenAIService(openAIApiClient: OpenAIApiClient, gson: Gson): AIService {
        return OpenAIService(openAIApiClient, gson)
    }

    @Provides
    @Singleton
    @LocalImpl
    fun provideLocalAIService(): AIService {
        return LocalAIService()
    }

    @Provides
    @Singleton
    fun provideAIService(@OpenAIImpl openAIService: AIService): AIService {
        return openAIService
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
