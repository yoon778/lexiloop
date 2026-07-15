package com.yoon778.lexiloop.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.yoon778.lexiloop.data.settings.SettingsRepository
import com.yoon778.lexiloop.platform.notification.DailyNotificationScheduler
import com.yoon778.lexiloop.presentation.contract.AppRoute
import com.yoon778.lexiloop.presentation.contract.SettingsEvent
import com.yoon778.lexiloop.presentation.contract.SettingsUiState
import com.yoon778.lexiloop.presentation.contract.UiEffect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val scheduler: DailyNotificationScheduler,
) : ContractViewModel<SettingsUiState, SettingsEvent>(SettingsUiState()) {
    init {
        viewModelScope.launch {
            repository.settings.collectLatest { settings ->
                mutableState.value = state.value.copy(
                    dailyNewCount = settings.dailyNewCount,
                    notificationEnabled = settings.notificationEnabled,
                    notificationHour = settings.notificationHour,
                    notificationMinute = settings.notificationMinute,
                    theme = settings.theme,
                    isSaving = false,
                )
            }
        }
    }

    override fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.DailyNewCountChanged -> save { repository.setDailyNewCount(event.value) }
            is SettingsEvent.ThemeChanged -> save { repository.setTheme(event.value) }
            is SettingsEvent.NotificationChanged -> save {
                repository.setNotification(event.enabled, event.hour, event.minute)
                if (event.enabled) {
                    scheduler.schedule(event.hour, event.minute)
                    emit(UiEffect.RequestNotificationPermission)
                } else {
                    scheduler.cancel()
                }
            }
            SettingsEvent.OpenDataManagement -> emit(UiEffect.Navigate(AppRoute.DataManagement))
            SettingsEvent.OpenLicenses -> emit(UiEffect.Message("라이선스 화면 준비 중"))
            SettingsEvent.ExportData -> emit(UiEffect.LaunchJsonExport)
            SettingsEvent.ImportData -> emit(UiEffect.LaunchJsonImport)
            SettingsEvent.ResetLearning -> Unit
            SettingsEvent.DeleteAllData -> Unit
        }
    }

    private fun save(block: suspend () -> Unit) {
        viewModelScope.launch {
            mutableState.value = state.value.copy(isSaving = true)
            runCatching { block() }
                .onFailure {
                    mutableState.value = state.value.copy(isSaving = false)
                    emit(UiEffect.Message("설정을 저장하지 못했어요"))
                }
        }
    }
}
