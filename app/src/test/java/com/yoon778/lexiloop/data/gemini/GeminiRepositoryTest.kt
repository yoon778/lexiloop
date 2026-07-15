package com.yoon778.lexiloop.data.gemini

import com.yoon778.lexiloop.domain.model.Difficulty
import com.yoon778.lexiloop.domain.model.ItemType
import com.yoon778.lexiloop.domain.model.PartOfSpeech
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiRepositoryTest {
    @Test
    fun `purpose response is validated strictly`() = runTest {
        val request = purposeRequest()
        val response = validPurposeResponse(request.requestId)
        val repository = GeminiRepository(GeminiTransport { contractJson.encodeToString(response) })

        val result = repository.analyzePurpose(request) as GeminiCallResult.Success

        assertEquals(1, result.attempts)
        assertEquals(request.requestId, result.value.requestId)
    }

    @Test
    fun `invalid response is corrected only once`() = runTest {
        val request = purposeRequest()
        val responses = ArrayDeque(
            listOf(
                "{\"schemaVersion\":1}",
                contractJson.encodeToString(validPurposeResponse(request.requestId)),
            ),
        )
        val prompts = mutableListOf<String>()
        val repository = GeminiRepository(
            GeminiTransport { input ->
                prompts += input.prompt
                responses.removeFirst()
            },
        )

        val result = repository.analyzePurpose(request) as GeminiCallResult.Success

        assertEquals(2, result.attempts)
        assertTrue(prompts.last().contains("Previous output failed"))
        assertTrue(prompts.last().contains("INVALID_JSON"))
    }

    @Test
    fun `two invalid responses return failure`() = runTest {
        val repository = GeminiRepository(GeminiTransport { "not-json" })

        val result = repository.analyzePurpose(purposeRequest()) as GeminiCallResult.Failure

        assertEquals(2, result.error.attempts)
        assertEquals(GeminiErrorCode.INVALID_JSON, result.error.code)
    }

    @Test
    fun `recommendation validates exact topic allocation`() = runTest {
        val request = recommendationRequest()
        val response = RecommendationResponse(
            schemaVersion = 1,
            requestId = request.requestId,
            items = (0 until 50).map { index ->
                RecommendationItem(
                    expression = "word$index",
                    baseForm = null,
                    itemType = ItemType.WORD,
                    partOfSpeech = PartOfSpeech.NOUN,
                    targetMeaningKo = "뜻$index",
                    auxiliaryMeaningsKo = emptyList(),
                    topicId = if (index < 25) request.topicAllocations[0].topicId else request.topicAllocations[1].topicId,
                    difficulty = Difficulty.INTERMEDIATE,
                    example = RecommendationExample(
                        template = "Use {{target}} here.",
                        targetForm = "word$index",
                        translationKo = "여기에서 사용한다.",
                    ),
                )
            },
        )
        val repository = GeminiRepository(GeminiTransport { contractJson.encodeToString(response) })

        val result = repository.generateRecommendations(request) as GeminiCallResult.Success

        assertEquals(50, result.value.items.size)
    }

    @Test
    fun `unknown response field is rejected`() = runTest {
        val request = purposeRequest()
        val encoded = contractJson.encodeToString(validPurposeResponse(request.requestId))
            .dropLast(1) + ",\"unknown\":true}"
        val repository = GeminiRepository(GeminiTransport { encoded })

        val result = repository.analyzePurpose(request) as GeminiCallResult.Failure

        assertEquals(GeminiErrorCode.INVALID_JSON, result.error.code)
    }

    private fun purposeRequest() = PurposeAnalysisRequest(
        requestId = "3c56d3a0-68c1-4d52-8e2f-d7efaf4830c3",
        learningPurpose = "일상과 개발 영어",
        selfAssessedDifficulty = Difficulty.INTERMEDIATE,
        dailyNewCount = 20,
    )

    private fun validPurposeResponse(requestId: String) = PurposeAnalysisResponse(
        schemaVersion = 1,
        requestId = requestId,
        profile = PurposeProfile(
            topics = listOf(
                PurposeTopic("일상", 50),
                PurposeTopic("개발", 50),
            ),
            difficulty = Difficulty.INTERMEDIATE,
            excludedTopics = emptyList(),
            exampleItems = listOf(
                PurposeExampleItem("grocery", "식료품", "일상"),
                PurposeExampleItem("deploy", "배포하다", "개발"),
                PurposeExampleItem("maintain", "유지하다", "개발"),
            ),
        ),
    )

    private fun recommendationRequest() = RecommendationRequest(
        requestId = "1fa87335-a55c-435f-a9bc-75524099f7aa",
        difficulty = Difficulty.INTERMEDIATE,
        topicAllocations = listOf(
            RecommendationTopicAllocation("64b27fe3-4a03-48e6-aad6-f2c49490ce25", "일상", 25),
            RecommendationTopicAllocation("1af612db-10d5-4127-89e6-d189c5e74a56", "개발", 25),
        ),
        excludedTopics = emptyList(),
        blockedCards = emptyList(),
    )
}
