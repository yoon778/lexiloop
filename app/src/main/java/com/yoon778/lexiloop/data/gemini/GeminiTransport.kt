package com.yoon778.lexiloop.data.gemini

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun interface GeminiTransport {
    suspend fun request(input: GeminiInteractionInput): String
}

data class GeminiInteractionInput(
    val prompt: String,
    val responseSchema: JsonObject,
)

class HttpGeminiTransport(
    private val apiKey: () -> String,
    private val model: String = "gemini-3.5-flash",
    private val endpoint: String = "https://generativelanguage.googleapis.com/v1/interactions",
) : GeminiTransport {
    override suspend fun request(input: GeminiInteractionInput): String = withContext(Dispatchers.IO) {
        val key = apiKey().trim()
        if (key.isEmpty()) throw GeminiTransportException(GeminiErrorCode.NETWORK_ERROR, "apiKey")

        val connection = URI(endpoint).toURL().openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 15_000
            connection.readTimeout = 60_000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("x-goog-api-key", key)
            val body = apiJson.encodeToString(
                InteractionRequest(
                    model = model,
                    input = input.prompt,
                    responseFormat = ResponseFormat(schema = input.responseSchema),
                    store = false,
                    generationConfig = GenerationConfig(),
                ),
            )
            connection.outputStream.use { it.write(body.encodeToByteArray()) }

            if (connection.responseCode !in 200..299) {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                val fieldPath = extractErrorFieldPath(errorBody)
                throw GeminiTransportException(
                    GeminiErrorCode.NETWORK_ERROR,
                    "http.${connection.responseCode}${fieldPath?.let { ".$it" }.orEmpty()}",
                )
            }
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            extractOutputText(response)
        } catch (error: GeminiTransportException) {
            throw error
        } catch (error: IOException) {
            throw GeminiTransportException(GeminiErrorCode.NETWORK_ERROR, "transport", error)
        } finally {
            connection.disconnect()
        }
    }
}

class GeminiTransportException(
    val code: GeminiErrorCode,
    val fieldPath: String = "transport",
    cause: Throwable? = null,
) : IOException(code.name, cause)

@Serializable
private data class InteractionRequest(
    val model: String,
    val input: String,
    @kotlinx.serialization.SerialName("response_format")
    val responseFormat: ResponseFormat,
    val store: Boolean,
    @kotlinx.serialization.SerialName("generation_config")
    val generationConfig: GenerationConfig,
)

@Serializable
private data class ResponseFormat(
    val type: String = "text",
    @kotlinx.serialization.SerialName("mime_type")
    val mimeType: String = "application/json",
    val schema: JsonObject,
)

@Serializable
private data class GenerationConfig(
    @kotlinx.serialization.SerialName("max_output_tokens")
    val maxOutputTokens: Int = 32_768,
    val temperature: Double = 0.3,
    @kotlinx.serialization.SerialName("thinking_level")
    val thinkingLevel: String = "low",
)

private fun extractOutputText(rawResponse: String): String {
    val root = runCatching { apiJson.parseToJsonElement(rawResponse).jsonObject }
        .getOrElse { throw GeminiTransportException(GeminiErrorCode.INVALID_JSON, "response", it) }
    if (root["status"]?.jsonPrimitive?.contentOrNull != "completed") {
        throw GeminiTransportException(GeminiErrorCode.NETWORK_ERROR, "status")
    }
    val texts = root["steps"]?.jsonArray.orEmpty()
        .filter { it.jsonObject["type"]?.jsonPrimitive?.contentOrNull == "model_output" }
        .flatMap { step -> step.jsonObject["content"]?.jsonArray.orEmpty() }
        .filter { content -> content.jsonObject["type"]?.jsonPrimitive?.contentOrNull == "text" }
        .mapNotNull { content -> content.jsonObject["text"]?.jsonPrimitive?.contentOrNull }
    return texts.joinToString(separator = "").ifBlank {
        throw GeminiTransportException(GeminiErrorCode.INVALID_JSON, "response")
    }
}

private fun extractErrorFieldPath(rawResponse: String): String? = runCatching {
    val error = apiJson.parseToJsonElement(rawResponse).jsonObject["error"]?.jsonObject ?: return@runCatching null
    error["details"]?.jsonArray.orEmpty()
        .flatMap { it.jsonObject["fieldViolations"]?.jsonArray.orEmpty() }
        .firstNotNullOfOrNull { violation ->
            violation.jsonObject["field"]?.jsonPrimitive?.contentOrNull
        }
        ?.takeIf { SAFE_FIELD_PATH.matches(it) }
}.getOrNull()

private val apiJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
}

private val SAFE_FIELD_PATH = Regex("^[A-Za-z0-9_.\\[\\]-]{1,160}$")
