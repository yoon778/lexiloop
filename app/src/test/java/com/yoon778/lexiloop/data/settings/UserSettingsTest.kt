package com.yoon778.lexiloop.data.settings

import com.yoon778.lexiloop.domain.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserSettingsTest {
    @Test
    fun `settings round trip preserves versioned profile`() {
        val settings = UserSettings(
            dailyNewCount = 20,
            learningPurpose = "일상과 개발 영어",
            recommendationProfile = RecommendationProfile(
                topics = listOf(
                    RecommendationTopic("daily", "일상", 50),
                    RecommendationTopic("dev", "개발", 50),
                ),
                difficulty = Difficulty.INTERMEDIATE,
            ),
            onboardingCompleted = true,
        ).validated()

        val encoded = strictSettingsJson.encodeToString(settings)
        val decoded = strictSettingsJson.decodeFromString<UserSettings>(encoded).validated()

        assertEquals(settings, decoded)
    }

    @Test
    fun `unknown fields are rejected`() {
        val encoded = strictSettingsJson.encodeToString(UserSettings())
            .dropLast(1) + ",\"unknown\":true}"

        assertTrue(runCatching { strictSettingsJson.decodeFromString<UserSettings>(encoded) }.isFailure)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `topic weights must sum to one hundred`() {
        UserSettings(
            recommendationProfile = RecommendationProfile(
                topics = listOf(RecommendationTopic("daily", "일상", 90)),
            ),
        ).validated()
    }
}
