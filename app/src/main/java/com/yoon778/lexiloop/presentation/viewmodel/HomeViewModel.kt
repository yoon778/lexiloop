package com.yoon778.lexiloop.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.yoon778.lexiloop.presentation.contract.AppRoute
import com.yoon778.lexiloop.presentation.contract.HomeEvent
import com.yoon778.lexiloop.presentation.contract.HomeUiState
import com.yoon778.lexiloop.presentation.contract.UiEffect
import kotlinx.coroutines.launch

class HomeViewModel(
    private val load: suspend () -> HomeUiState,
    private val startReview: suspend () -> String?,
    private val startNew: suspend () -> String?,
) : ContractViewModel<HomeUiState, HomeEvent>(HomeUiState()) {
    init { refresh() }

    override fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.Refresh -> refresh()
            HomeEvent.StartReview -> viewModelScope.launch {
                (state.value.activeSessionId ?: startReview())?.let { emit(UiEffect.Navigate(AppRoute.Study(it))) }
                    ?: emit(UiEffect.Message("복습할 항목이 없어요"))
            }
            HomeEvent.OpenNewOverview -> emit(UiEffect.Navigate(AppRoute.NewOverview))
            HomeEvent.StartNew -> viewModelScope.launch {
                startNew()?.let { emit(UiEffect.Navigate(AppRoute.Study(it))) }
                    ?: emit(UiEffect.Message("학습할 신규 단어가 없어요"))
            }
            HomeEvent.ContinueSession -> state.value.activeSessionId?.let {
                emit(UiEffect.Navigate(AppRoute.Study(it)))
            }
            HomeEvent.OpenWordManagement -> emit(UiEffect.Navigate(AppRoute.WordManagement))
            HomeEvent.OpenSettings -> emit(UiEffect.Navigate(AppRoute.Settings))
            HomeEvent.GenerateMoreRequested -> emit(UiEffect.Message("추천 생성 확인이 필요해요"))
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            mutableState.value = state.value.copy(isLoading = true)
            runCatching { load() }
                .onSuccess { mutableState.value = it.copy(isLoading = false) }
                .onFailure { mutableState.value = state.value.copy(isLoading = false, isOffline = true) }
        }
    }
}
