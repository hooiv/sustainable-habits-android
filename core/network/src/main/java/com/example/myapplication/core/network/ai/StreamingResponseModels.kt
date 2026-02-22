package com.hooiv.habitflow.core.network.ai

import com.google.gson.annotations.SerializedName

/**
 * Models for parsing streaming responses from OpenAI API
 */

/**
 * Streaming response chunk from OpenAI API
 */
data class StreamingChatCompletionResponse(
    @SerializedName("id") val id: String,
    @SerializedName("object") val objectType: String,
    @SerializedName("created") val created: Long,
    @SerializedName("model") val model: String,
    @SerializedName("choices") val choices: List<StreamingChoice>
)

/**
 * Choice in streaming response
 */
data class StreamingChoice(
    @SerializedName("index") val index: Int,
    @SerializedName("delta") val delta: DeltaMessage,
    @SerializedName("finish_reason") val finishReason: String?
)

/**
 * Delta message in streaming response
 * Contains incremental content updates
 */
data class DeltaMessage(
    @SerializedName("role") val role: String? = null,
    @SerializedName("content") val content: String? = null
)

/**
 * Utility class for parsing SSE (Server-Sent Events) from OpenAI streaming API
 */
object SSEParser {
    /**
     * Parse a line from the SSE stream
     * @param line Line from the SSE stream
     * @return The data content if it's a data line, null otherwise
     */
    fun parseSSELine(line: String): String? {
        // Check if line is a data line
        if (line.startsWith("data: ")) {
            // Extract data content
            val data = line.substring(6).trim()
            
            // Check for stream end marker
            if (data == "[DONE]") {
                return null
            }
            
            return data
        }
        
        return null
    }
}
