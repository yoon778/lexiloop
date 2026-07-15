package com.yoon778.lexiloop.data.gemini

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class GeminiRepository(
    private val transport: GeminiTransport,
) {
    suspend fun analyzePurpose(request: PurposeAnalysisRequest): GeminiCallResult<PurposeAnalysisResponse> {
        GeminiContractValidator.validate(request)
        return callWithCorrection(
            requestJson = contractJson.encodeToString(request),
            schema = purposeResponseSchema,
            instruction = purposeInstruction,
        ) { raw ->
            val response = decode<PurposeAnalysisResponse>(raw)
            GeminiContractValidator.validate(response, request)
            response
        }
    }

    suspend fun generateRecommendations(
        request: RecommendationRequest,
    ): GeminiCallResult<RecommendationResponse> {
        GeminiContractValidator.validate(request)
        return callWithCorrection(
            requestJson = contractJson.encodeToString(request),
            schema = recommendationResponseSchema,
            instruction = recommendationInstruction,
        ) { raw ->
            val response = decode<RecommendationResponse>(raw)
            GeminiContractValidator.validate(response, request)
            response
        }
    }

    private suspend fun <T> callWithCorrection(
        requestJson: String,
        schema: JsonObject,
        instruction: String,
        parseAndValidate: (String) -> T,
    ): GeminiCallResult<T> {
        var lastFailure = GeminiFailure(GeminiErrorCode.NETWORK_ERROR, "transport", 0)
        repeat(2) { attemptIndex ->
            val attempt = attemptIndex + 1
            val correction = if (attempt == 1) {
                ""
            } else {
                "\nPrevious output failed: ${lastFailure.code.name} at ${lastFailure.fieldPath}. Return a fully corrected object."
            }
            val raw = runCatching {
                transport.request(
                    GeminiInteractionInput(
                        prompt = "$instruction\nRequest JSON:\n$requestJson$correction",
                        responseSchema = schema,
                    ),
                )
            }.getOrElse { error ->
                lastFailure = GeminiFailure(
                    code = (error as? GeminiTransportException)?.code ?: GeminiErrorCode.NETWORK_ERROR,
                    fieldPath = "transport",
                    attempts = attempt,
                )
                return@repeat
            }

            try {
                return GeminiCallResult.Success(parseAndValidate(raw), attempt)
            } catch (error: GeminiContractException) {
                lastFailure = GeminiFailure(error.code, error.fieldPath, attempt)
            } catch (_: SerializationException) {
                lastFailure = GeminiFailure(GeminiErrorCode.INVALID_JSON, "response", attempt)
            } catch (_: IllegalArgumentException) {
                lastFailure = GeminiFailure(GeminiErrorCode.SCHEMA_MISMATCH, "response", attempt)
            }
        }
        return GeminiCallResult.Failure(lastFailure)
    }
}

sealed interface GeminiCallResult<out T> {
    data class Success<T>(val value: T, val attempts: Int) : GeminiCallResult<T>
    data class Failure(val error: GeminiFailure) : GeminiCallResult<Nothing>
}

data class GeminiFailure(
    val code: GeminiErrorCode,
    val fieldPath: String,
    val attempts: Int,
)

private inline fun <reified T> decode(raw: String): T = try {
    contractJson.decodeFromString(raw)
} catch (error: SerializationException) {
    throw GeminiContractException(GeminiErrorCode.INVALID_JSON, "response")
}

val contractJson = Json {
    ignoreUnknownKeys = false
    explicitNulls = true
    encodeDefaults = true
    isLenient = false
}

private const val purposeInstruction =
    "Analyze the Korean learning purpose. Return only the JSON object matching the schema. Preserve requestId exactly."

private const val recommendationInstruction =
    "Generate practical English learning cards for a Korean learner. Return only the JSON object matching the schema. Preserve requestId, difficulty, topic IDs, exact topic counts, and {{target}} exactly once per example template."
