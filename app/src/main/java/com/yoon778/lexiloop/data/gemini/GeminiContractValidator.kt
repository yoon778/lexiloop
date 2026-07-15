package com.yoon778.lexiloop.data.gemini

import com.yoon778.lexiloop.domain.learning.normalizeEnglish
import java.util.UUID

object GeminiContractValidator {
    fun validate(request: PurposeAnalysisRequest) {
        schema(request.schemaVersion)
        uuid(request.requestId, "requestId")
        length(request.learningPurpose, 1, 1_000, "learningPurpose")
        if (request.dailyNewCount !in 1..100) mismatch("dailyNewCount")
        if (request.contentLocale != "ko-KR") mismatch("contentLocale")
        if (request.learningLocale != "en-US") mismatch("learningLocale")
    }

    fun validate(response: PurposeAnalysisResponse, request: PurposeAnalysisRequest) {
        schema(response.schemaVersion)
        if (response.requestId != request.requestId) {
            fail(GeminiErrorCode.REQUEST_ID_MISMATCH, "requestId")
        }
        val profile = response.profile
        if (profile.topics.size !in 1..5) mismatch("profile.topics")
        profile.topics.forEachIndexed { index, topic ->
            length(topic.name, 1, 40, "profile.topics[$index].name")
            if (topic.weightPercent !in 1..100) mismatch("profile.topics[$index].weightPercent")
        }
        if (profile.topics.sumOf(PurposeTopic::weightPercent) != 100) {
            fail(GeminiErrorCode.COUNT_MISMATCH, "profile.topics.weightPercent")
        }
        unique(profile.topics.map { it.name.lowercase() }, "profile.topics.name")
        if (profile.excludedTopics.size > 10) mismatch("profile.excludedTopics")
        profile.excludedTopics.forEachIndexed { index, topic ->
            length(topic, 1, 40, "profile.excludedTopics[$index]")
        }
        unique(profile.excludedTopics.map(String::lowercase), "profile.excludedTopics")
        val topicNames = profile.topics.map { it.name.lowercase() }.toSet()
        if (profile.excludedTopics.any { it.lowercase() in topicNames }) {
            mismatch("profile.excludedTopics")
        }
        if (profile.exampleItems.size !in 3..5) mismatch("profile.exampleItems")
        profile.exampleItems.forEachIndexed { index, item ->
            length(item.expression, 1, 80, "profile.exampleItems[$index].expression")
            length(item.targetMeaningKo, 1, 120, "profile.exampleItems[$index].targetMeaningKo")
            if (item.topicName.lowercase() !in topicNames) mismatch("profile.exampleItems[$index].topicName")
        }
    }

    fun validate(request: RecommendationRequest) {
        schema(request.schemaVersion)
        uuid(request.requestId, "requestId")
        if (request.requestedCount != 50) fail(GeminiErrorCode.COUNT_MISMATCH, "requestedCount")
        if (request.topicAllocations.size !in 1..5) mismatch("topicAllocations")
        request.topicAllocations.forEachIndexed { index, allocation ->
            uuid(allocation.topicId, "topicAllocations[$index].topicId")
            length(allocation.name, 1, 40, "topicAllocations[$index].name")
            if (allocation.count !in 1..50) mismatch("topicAllocations[$index].count")
        }
        unique(request.topicAllocations.map(RecommendationTopicAllocation::topicId), "topicAllocations.topicId")
        unique(request.topicAllocations.map { it.name.lowercase() }, "topicAllocations.name")
        if (request.topicAllocations.sumOf(RecommendationTopicAllocation::count) != 50) {
            fail(GeminiErrorCode.COUNT_MISMATCH, "topicAllocations.count")
        }
        if (request.blockedCards.size > 1_000) mismatch("blockedCards")
    }

    fun validate(response: RecommendationResponse, request: RecommendationRequest) {
        schema(response.schemaVersion)
        if (response.requestId != request.requestId) {
            fail(GeminiErrorCode.REQUEST_ID_MISMATCH, "requestId")
        }
        if (response.items.size != request.requestedCount) {
            fail(GeminiErrorCode.COUNT_MISMATCH, "items")
        }
        val allocations = request.topicAllocations.associateBy(RecommendationTopicAllocation::topicId)
        val blocked = request.blockedCards.map(::cardKey).toSet()
        val seen = mutableSetOf<String>()

        response.items.forEachIndexed { index, item ->
            val path = "items[$index]"
            length(item.expression, 1, 80, "$path.expression")
            item.baseForm?.let { length(it, 1, 80, "$path.baseForm") }
            length(item.targetMeaningKo, 1, 120, "$path.targetMeaningKo")
            if (item.auxiliaryMeaningsKo.size > 3) mismatch("$path.auxiliaryMeaningsKo")
            item.auxiliaryMeaningsKo.forEachIndexed { meaningIndex, meaning ->
                length(meaning, 1, 120, "$path.auxiliaryMeaningsKo[$meaningIndex]")
                if (meaning.trim() == item.targetMeaningKo.trim()) mismatch("$path.auxiliaryMeaningsKo[$meaningIndex]")
            }
            if (item.topicId !in allocations) {
                fail(GeminiErrorCode.TOPIC_ALLOCATION_MISMATCH, "$path.topicId")
            }
            if (item.difficulty != request.difficulty) mismatch("$path.difficulty")
            validateExample(item.example, path)

            val key = cardKey(item)
            if (!seen.add(key)) fail(GeminiErrorCode.DUPLICATE_ITEM, path)
            if (key in blocked) fail(GeminiErrorCode.BLOCKED_ITEM, path)
        }

        val actualCounts = response.items.groupingBy(RecommendationItem::topicId).eachCount()
        request.topicAllocations.forEach { allocation ->
            if (actualCounts[allocation.topicId] != allocation.count) {
                fail(GeminiErrorCode.TOPIC_ALLOCATION_MISMATCH, "items.topicId")
            }
        }
    }

    private fun validateExample(example: RecommendationExample, parentPath: String) {
        length(example.template, 1, 240, "$parentPath.example.template")
        length(example.targetForm, 1, 80, "$parentPath.example.targetForm")
        length(example.translationKo, 1, 240, "$parentPath.example.translationKo")
        if (Regex("\\{\\{target}}", RegexOption.IGNORE_CASE).findAll(example.template).count() != 1) {
            fail(GeminiErrorCode.INVALID_EXAMPLE, "$parentPath.example.template")
        }
    }

    private fun cardKey(card: BlockedCard): String =
        "${normalizeEnglish(card.expression)}|${card.partOfSpeech}|${card.targetMeaningKo.trim()}"

    private fun cardKey(card: RecommendationItem): String =
        "${normalizeEnglish(card.expression)}|${card.partOfSpeech}|${card.targetMeaningKo.trim()}"

    private fun schema(version: Int) {
        if (version != 1) mismatch("schemaVersion")
    }

    private fun uuid(value: String, path: String) {
        if (runCatching { UUID.fromString(value) }.isFailure) mismatch(path)
    }

    private fun length(value: String, min: Int, max: Int, path: String) {
        val trimmed = value.trim()
        if (trimmed.length !in min..max || forbiddenText.containsMatchIn(trimmed)) mismatch(path)
    }

    private fun unique(values: List<String>, path: String) {
        if (values.distinct().size != values.size) fail(GeminiErrorCode.DUPLICATE_ITEM, path)
    }

    private fun mismatch(path: String): Nothing = fail(GeminiErrorCode.SCHEMA_MISMATCH, path)

    private fun fail(code: GeminiErrorCode, path: String): Nothing =
        throw GeminiContractException(code, path)

    private val forbiddenText = Regex("[\\u0000-\\u001F]|```|<[^>]+>")
}
