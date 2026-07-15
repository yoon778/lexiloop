package com.yoon778.lexiloop

import android.app.Application
import com.yoon778.lexiloop.data.settings.SettingsRepository
import com.yoon778.lexiloop.platform.notification.DailyNotificationScheduler

class LexiLoopApplication : Application() {
    val settingsRepository: SettingsRepository by lazy { SettingsRepository.create(this) }
    val notificationScheduler: DailyNotificationScheduler by lazy {
        DailyNotificationScheduler(this)
    }
}
