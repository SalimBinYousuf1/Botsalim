package com.example.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

sealed class StreamEvent {
    data class Token(val text: String) : StreamEvent()
    data class Completed(val fullText: String, val promptTokens: Int, val completionTokens: Int) : StreamEvent()
    data class Error(val error: String) : StreamEvent()
}

data class ChatMessagePayload(
    val role: String,
    val content: String
)

class GroqStreamingClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Streams responses token-by-token from OpenAI-compatible endpoint.
     */
    fun streamChat(
        apiKey: String,
        model: String,
        messages: List<ChatMessagePayload>,
        temperature: Float,
        topP: Float,
        maxTokens: Int
    ): Flow<StreamEvent> = flow {
        if (apiKey.isBlank()) {
            emit(
                StreamEvent.Error(
                    "No API Key configured. Please add your API key in Settings → Advanced / Developer options or configure GROQ_API_KEY in the Secrets panel."
                )
            )
            return@flow
        }

        val requestJson = JSONObject().apply {
            put("model", model)
            put("temperature", temperature.toDouble())
            put("top_p", topP.toDouble())
            put("max_tokens", maxTokens)
            put("stream", true)

            val messagesArray = JSONArray()
            for (msg in messages) {
                val m = JSONObject().apply {
                    put("role", msg.role)
                    put("content", msg.content)
                }
                messagesArray.put(m)
            }
            put("messages", messagesArray)
        }

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "text/event-stream")
            .post(requestJson.toString().toRequestBody(jsonMediaType))
            .build()

        var accumulatedText = StringBuilder()
        var promptTokens = 0
        var completionTokens = 0

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                val parsedMsg = try {
                    val obj = JSONObject(errorBody)
                    obj.optJSONObject("error")?.optString("message") ?: errorBody
                } catch (e: Exception) {
                    errorBody
                }
                emit(StreamEvent.Error("Inference error (${response.code}): $parsedMsg"))
                return@flow
            }

            val body = response.body
            if (body == null) {
                emit(StreamEvent.Error("Empty response stream from server."))
                return@flow
            }

            val reader = BufferedReader(InputStreamReader(body.byteStream()))
            var line: String? = reader.readLine()

            while (line != null && currentCoroutineContext().isActive) {
                val trimmed = line.trim()
                if (trimmed.startsWith("data:")) {
                    val dataContent = trimmed.substring(5).trim()
                    if (dataContent == "[DONE]") {
                        break
                    }
                    if (dataContent.isNotEmpty()) {
                        try {
                            val json = JSONObject(dataContent)
                            val choices = json.optJSONArray("choices")
                            if (choices != null && choices.length() > 0) {
                                val firstChoice = choices.getJSONObject(0)
                                val delta = firstChoice.optJSONObject("delta")
                                val textChunk = delta?.optString("content") ?: ""
                                if (textChunk.isNotEmpty()) {
                                    accumulatedText.append(textChunk)
                                    emit(StreamEvent.Token(textChunk))
                                }
                            }
                            val usage = json.optJSONObject("usage")
                            if (usage != null) {
                                promptTokens = usage.optInt("prompt_tokens", promptTokens)
                                completionTokens = usage.optInt("completion_tokens", completionTokens)
                            }
                        } catch (e: Exception) {
                            // Non-fatal parse issue on chunk
                        }
                    }
                }
                line = reader.readLine()
            }

            // Estimate tokens if not returned in SSE
            if (completionTokens == 0) {
                completionTokens = (accumulatedText.length / 4).coerceAtLeast(1)
            }
            if (promptTokens == 0) {
                val totalPromptLen = messages.sumOf { it.content.length }
                promptTokens = (totalPromptLen / 4).coerceAtLeast(1)
            }

            emit(
                StreamEvent.Completed(
                    fullText = accumulatedText.toString(),
                    promptTokens = promptTokens,
                    completionTokens = completionTokens
                )
            )
        } catch (e: Exception) {
            if (currentCoroutineContext().isActive) {
                emit(StreamEvent.Error("Network error: ${e.localizedMessage ?: "Failed to reach inference server"}"))
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Non-streaming call to auto-generate a short title from the first exchange.
     */
    suspend fun generateTitle(
        apiKey: String,
        userPrompt: String,
        assistantReply: String
    ): String {
        if (apiKey.isBlank()) return userPrompt.take(30)
        return try {
            val requestJson = JSONObject().apply {
                put("model", "openai/gpt-oss-20b")
                put("temperature", 0.3)
                put("max_tokens", 25)
                val arr = JSONArray()
                arr.put(JSONObject().apply {
                    put("role", "system")
                    put("content", "Generate a very brief, punchy title (3-5 words max, no quotes, no punctuation) summarizing this conversation topic.")
                })
                arr.put(JSONObject().apply {
                    put("role", "user")
                    put("content", "User: $userPrompt\nAssistant: ${assistantReply.take(150)}")
                })
                put("messages", arr)
            }

            val request = Request.Builder()
                .url("https://api.groq.com/openai/v1/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val str = response.body?.string() ?: ""
                val json = JSONObject(str)
                val choices = json.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val raw = choices.getJSONObject(0).optJSONObject("message")?.optString("content")?.trim() ?: ""
                    val cleaned = raw.replace("\"", "").replace("'", "").trim()
                    if (cleaned.isNotEmpty()) return cleaned
                }
            }
            userPrompt.take(30)
        } catch (e: Exception) {
            userPrompt.take(30)
        }
    }
}
