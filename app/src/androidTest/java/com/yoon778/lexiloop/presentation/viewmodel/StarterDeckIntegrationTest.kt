package com.yoon778.lexiloop.presentation.viewmodel

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yoon778.lexiloop.data.gemini.GeminiRepository
import com.yoon778.lexiloop.data.gemini.GeminiTransport
import com.yoon778.lexiloop.data.gemini.GeminiContractValidator
import com.yoon778.lexiloop.data.gemini.RecommendationExample
import com.yoon778.lexiloop.data.gemini.RecommendationItem
import com.yoon778.lexiloop.data.gemini.RecommendationRequest
import com.yoon778.lexiloop.data.gemini.RecommendationResponse
import com.yoon778.lexiloop.data.gemini.contractJson
import com.yoon778.lexiloop.data.local.LexiLoopDatabase
import com.yoon778.lexiloop.data.repository.RoomLearningRepository
import com.yoon778.lexiloop.data.settings.RecommendationProfile
import com.yoon778.lexiloop.data.settings.RecommendationTopic
import com.yoon778.lexiloop.data.settings.SettingsRepository
import com.yoon778.lexiloop.domain.model.Difficulty
import com.yoon778.lexiloop.domain.model.ItemType
import com.yoon778.lexiloop.domain.model.PartOfSpeech
import java.util.UUID
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StarterDeckIntegrationTest {
    private lateinit var database: LexiLoopDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, LexiLoopDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun sixValidatedBatchesStoreThreeHundredCards() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        var batch = 0
        val gemini = GeminiRepository(
            GeminiTransport { input ->
                val request = contractJson.decodeFromString<RecommendationRequest>(
                    input.prompt.substringAfter("Request JSON:\n").substringBefore("\nPrevious output"),
                )
                val offset = batch++ * 50
                val response = RecommendationResponse(
                    schemaVersion = 1,
                    requestId = request.requestId,
                    items = request.topicAllocations.flatMap { allocation ->
                        (0 until allocation.count).map { allocationIndex -> allocation.topicId to allocationIndex }
                    }.mapIndexed { index, (topicId, _) ->
                        val number = offset + index
                        RecommendationItem(
                            expression = "word$number",
                            baseForm = null,
                            itemType = ItemType.WORD,
                            partOfSpeech = PartOfSpeech.NOUN,
                            targetMeaningKo = "뜻$number",
                            auxiliaryMeaningsKo = emptyList(),
                            topicId = topicId,
                            difficulty = Difficulty.INTERMEDIATE,
                            example = RecommendationExample(
                                template = "Use {{target}} here.",
                                targetForm = "word$number",
                                translationKo = "여기에서 사용한다.",
                            ),
                        )
                    },
                )
                GeminiContractValidator.validate(response, request)
                contractJson.encodeToString(response)
            },
        )
        val provider = LexiLoopViewModelProvider(
            database = database,
            learningRepository = RoomLearningRepository(database),
            settingsRepository = SettingsRepository.create(context),
            geminiRepository = gemini,
        )

        provider.generateStarterDeck(
            RecommendationProfile(
                topics = listOf(
                    RecommendationTopic(TOPIC_A, "일상", 50),
                    RecommendationTopic(TOPIC_B, "개발", 50),
                ),
                difficulty = Difficulty.INTERMEDIATE,
            ),
        )
        provider.generateStarterDeck(
            RecommendationProfile(
                topics = listOf(
                    RecommendationTopic(TOPIC_A, "일상", 50),
                    RecommendationTopic(TOPIC_B, "개발", 50),
                ),
                difficulty = Difficulty.INTERMEDIATE,
            ),
        )

        assertEquals(6, batch)
        assertEquals(300, database.dao().queuedItems(400).size)
    }

    private companion object {
        val TOPIC_A: String = UUID.fromString("64b27fe3-4a03-48e6-aad6-f2c49490ce25").toString()
        val TOPIC_B: String = UUID.fromString("1af612db-10d5-4127-89e6-d189c5e74a56").toString()
    }
}
