package com.yoon778.lexiloop.data.settings

import com.yoon778.lexiloop.domain.model.Difficulty
import kotlinx.serialization.Serializable

@Serializable
data class UserSettings(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val dailyNewCount: Int = 20,
    val notificationEnabled: Boolean = false,
    val notificationHour: Int = 20,
    val notificationMinute: Int = 0,
    val learningPurpose: String = "",
    val recommendationProfile: RecommendationProfile = RecommendationProfile(),
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val onboardingCompleted: Boolean = false,
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }
}

@Serializable
data class RecommendationProfile(
    val topics: List<RecommendationTopic> = emptyList(),
    val difficulty: Difficulty = Difficulty.INTERMEDIATE,
    val excludedTopics: List<String> = emptyList(),
)

@Serializable
data class RecommendationTopic(
    val id: String,
    val name: String,
    val weightPercent: Int,
)

@Serializable
enum class ThemePreference {
    SYSTEM,
    LIGHT,
    DARK,
}

fun UserSettings.validated(): UserSettings {
    require(schemaVersion == UserSettings.CURRENT_SCHEMA_VERSION)
    require(dailyNewCount in 1..100)
    require(notificationHour in 0..23)
    require(notificationMinute in 0..59)
    require(learningPurpose.length <= 1_000)
    require(recommendationProfile.topics.size <= 5)
    if (recommendationProfile.topics.isNotEmpty()) {
        require(recommendationProfile.topics.sumOf(RecommendationTopic::weightPercent) == 100)
    }
    require(recommendationProfile.topics.all { it.name.isNotBlank() && it.weightPercent in 1..100 })
    require(recommendationProfile.topics.map { it.id }.distinct().size == recommendationProfile.topics.size)
    require(recommendationProfile.topics.map { it.name.lowercase() }.distinct().size == recommendationProfile.topics.size)
    require(recommendationProfile.excludedTopics.size <= 10)
    require(recommendationProfile.excludedTopics.all(String::isNotBlank))
    val topicNames = recommendationProfile.topics.map { it.name.lowercase() }.toSet()
    require(recommendationProfile.excludedTopics.none { it.lowercase() in topicNames })
    return this
}
