package com.yoon778.lexiloop

import android.app.Application
import com.yoon778.lexiloop.data.gemini.GeminiRepository
import com.yoon778.lexiloop.data.gemini.HttpGeminiTransport
import com.yoon778.lexiloop.data.local.LexiLoopDatabase
import com.yoon778.lexiloop.data.repository.RoomLearningRepository
import com.yoon778.lexiloop.data.settings.SettingsRepository
import com.yoon778.lexiloop.platform.notification.DailyNotificationScheduler
import com.yoon778.lexiloop.presentation.viewmodel.LexiLoopViewModelProvider

class LexiLoopApplication : Application() {
    val database: LexiLoopDatabase by lazy { LexiLoopDatabase.create(this) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository.create(this) }
    val learningRepository: RoomLearningRepository by lazy { RoomLearningRepository(database) }
    val geminiRepository: GeminiRepository by lazy {
        GeminiRepository(HttpGeminiTransport(apiKey = { BuildConfig.GEMINI_API_KEY }))
    }
    val notificationScheduler: DailyNotificationScheduler by lazy {
        DailyNotificationScheduler(this)
    }
    val viewModels: LexiLoopViewModelProvider by lazy {
        LexiLoopViewModelProvider(
            database = database,
            learningRepository = learningRepository,
            settingsRepository = settingsRepository,
            geminiRepository = geminiRepository,
        )
    }
}
