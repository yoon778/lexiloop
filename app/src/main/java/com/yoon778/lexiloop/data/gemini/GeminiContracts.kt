package com.yoon778.lexiloop.data.gemini

import com.yoon778.lexiloop.domain.model.Difficulty
import com.yoon778.lexiloop.domain.model.ItemType
import com.yoon778.lexiloop.domain.model.PartOfSpeech
import kotlinx.serialization.Serializable

@Serializable
data class PurposeAnalysisRequest(
    val schemaVersion: Int = 1,
    val requestId: String,
    val learningPurpose: String,
    val selfAssessedDifficulty: Difficulty,
    val dailyNewCount: Int,
    val contentLocale: String = "ko-KR",
    val learningLocale: String = "en-US",
)

@Serializable
data class PurposeAnalysisResponse(
    val schemaVersion: Int,
    val requestId: String,
    val profile: PurposeProfile,
)

@Serializable
data class PurposeProfile(
    val topics: List<PurposeTopic>,
    val difficulty: Difficulty,
    val excludedTopics: List<String>,
    val exampleItems: List<PurposeExampleItem>,
)

@Serializable
data class PurposeTopic(val name: String, val weightPercent: Int)

@Serializable
data class PurposeExampleItem(
    val expression: String,
    val targetMeaningKo: String,
    val topicName: String,
)

@Serializable
data class RecommendationRequest(
    val schemaVersion: Int = 1,
    val requestId: String,
    val requestedCount: Int = 50,
    val coreWordCount: Int = 40,
    val supplementaryExpressionCount: Int = 10,
    val difficulty: Difficulty,
    val topicAllocations: List<RecommendationTopicAllocation>,
    val excludedTopics: List<String>,
    val blockedCards: List<BlockedCard>,
)

@Serializable
data class RecommendationTopicAllocation(
    val topicId: String,
    val name: String,
    val count: Int,
)

@Serializable
data class BlockedCard(
    val expression: String,
    val partOfSpeech: PartOfSpeech,
    val targetMeaningKo: String,
)

@Serializable
data class RecommendationResponse(
    val schemaVersion: Int,
    val requestId: String,
    val items: List<RecommendationItem>,
)

@Serializable
data class RecommendationItem(
    val expression: String,
    val baseForm: String?,
    val itemType: ItemType,
    val partOfSpeech: PartOfSpeech,
    val targetMeaningKo: String,
    val auxiliaryMeaningsKo: List<String>,
    val topicId: String,
    val difficulty: Difficulty,
    val example: RecommendationExample,
)

@Serializable
data class RecommendationExample(
    val template: String,
    val targetForm: String,
    val translationKo: String,
)

enum class GeminiErrorCode {
    INVALID_JSON,
    SCHEMA_MISMATCH,
    REQUEST_ID_MISMATCH,
    COUNT_MISMATCH,
    TOPIC_ALLOCATION_MISMATCH,
    DUPLICATE_ITEM,
    BLOCKED_ITEM,
    INVALID_EXAMPLE,
    DICTIONARY_MISMATCH,
    NETWORK_ERROR,
}

class GeminiContractException(
    val code: GeminiErrorCode,
    val fieldPath: String,
) : IllegalArgumentException("${code.name}: $fieldPath")
