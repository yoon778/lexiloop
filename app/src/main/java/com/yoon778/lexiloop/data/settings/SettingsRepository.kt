package com.yoon778.lexiloop.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import kotlinx.coroutines.flow.Flow

class SettingsRepository(
    private val dataStore: DataStore<UserSettings>,
) {
    val settings: Flow<UserSettings> = dataStore.data

    suspend fun update(transform: (UserSettings) -> UserSettings): UserSettings =
        dataStore.updateData { current -> transform(current).validated() }

    suspend fun setDailyNewCount(count: Int) = update { it.copy(dailyNewCount = count) }

    suspend fun setNotification(enabled: Boolean, hour: Int, minute: Int) = update {
        it.copy(
            notificationEnabled = enabled,
            notificationHour = hour,
            notificationMinute = minute,
        )
    }

    suspend fun setOnboarding(
        learningPurpose: String,
        dailyNewCount: Int,
        profile: RecommendationProfile,
    ) = update {
        it.copy(
            learningPurpose = learningPurpose.trim(),
            dailyNewCount = dailyNewCount,
            recommendationProfile = profile,
            onboardingCompleted = true,
        )
    }

    suspend fun setRecommendationProfile(profile: RecommendationProfile) =
        update { it.copy(recommendationProfile = profile) }

    suspend fun setTheme(theme: ThemePreference) = update { it.copy(theme = theme) }

    suspend fun reset() = dataStore.updateData { UserSettings() }

    companion object {
        fun create(context: Context): SettingsRepository = SettingsRepository(
            DataStoreFactory.create(
                serializer = UserSettingsSerializer(),
                produceFile = { context.applicationContext.dataStoreFile("user_settings.json") },
            ),
        )
    }
}
