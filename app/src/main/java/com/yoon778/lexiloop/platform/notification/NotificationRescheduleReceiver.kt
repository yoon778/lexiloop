package com.yoon778.lexiloop.platform.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yoon778.lexiloop.LexiLoopApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in SUPPORTED_ACTIONS) return
        val pendingResult = goAsync()
        val application = context.applicationContext as LexiLoopApplication
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = application.settingsRepository.settings.first()
                if (settings.notificationEnabled) {
                    application.notificationScheduler.schedule(
                        settings.notificationHour,
                        settings.notificationMinute,
                    )
                } else {
                    application.notificationScheduler.cancel()
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        val SUPPORTED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
        )
    }
}
