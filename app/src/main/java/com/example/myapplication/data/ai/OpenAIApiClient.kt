package com.example.myapplication.data.ai

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming

/**
 * Retrofit API client for OpenAI
 */
interface OpenAIApiClient {
    /**
     * Create a chat completion (non-streaming)
     */
    @POST("v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequest
    ): Response<ChatCompletionResponse>

    /**
     * Create a streaming chat completion
     * Returns a raw response body that can be processed as an event stream
     */
    @Streaming
    @POST("v1/chat/completions")
    suspend fun createStreamingChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequest
    ): Response<ResponseBody>
}

/**
 * Chat completion request
 */
data class ChatCompletionRequest(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<Message>,
    @SerializedName("temperature") val temperature: Float = 0.7f,
    @SerializedName("max_tokens") val maxTokens: Int = 500,
    @SerializedName("stream") val stream: Boolean = false
)

/**
 * Message for chat completion
 */
data class Message(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

/**
 * Chat completion response
 */
data class ChatCompletionResponse(
    @SerializedName("id") val id: String,
    @SerializedName("object") val objectType: String,
    @SerializedName("created") val created: Long,
    @SerializedName("model") val model: String,
    @SerializedName("choices") val choices: List<Choice>,
    @SerializedName("usage") val usage: Usage
)

/**
 * Choice in chat completion response
 */
data class Choice(
    @SerializedName("index") val index: Int,
    @SerializedName("message") val message: Message,
    @SerializedName("finish_reason") val finishReason: String
)

/**
 * Usage statistics in chat completion response
 */
data class Usage(
    @SerializedName("prompt_tokens") val promptTokens: Int,
    @SerializedName("completion_tokens") val completionTokens: Int,
    @SerializedName("total_tokens") val totalTokens: Int
)
