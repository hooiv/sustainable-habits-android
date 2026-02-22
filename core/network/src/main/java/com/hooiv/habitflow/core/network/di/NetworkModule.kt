package com.hooiv.habitflow.core.network.di

import com.hooiv.habitflow.core.network.ai.AIService
import com.hooiv.habitflow.core.network.ai.LocalAIService
import com.hooiv.habitflow.core.network.ai.OpenAIApiClient
import com.hooiv.habitflow.core.network.ai.OpenAIService
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
import android.content.Context
import com.hooiv.habitflow.core.network.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Cache
import okhttp3.Interceptor
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
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }
        
        val cacheSize = (10 * 1024 * 1024).toLong() // 10 MB Cache
        val cache = Cache(context.cacheDir, cacheSize)

        // Offline cache interceptor
        val offlineInterceptor = Interceptor { chain ->
            var request = chain.request()
            request = request.newBuilder()
                .header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7) // 7 days stale allowed offline
                .build()
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(offlineInterceptor)
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
