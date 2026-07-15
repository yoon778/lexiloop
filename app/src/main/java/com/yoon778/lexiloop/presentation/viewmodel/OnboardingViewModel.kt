package com.yoon778.lexiloop.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.yoon778.lexiloop.data.gemini.PurposeAnalysisRequest
import com.yoon778.lexiloop.data.gemini.PurposeAnalysisResponse
import com.yoon778.lexiloop.data.settings.RecommendationProfile
import com.yoon778.lexiloop.data.settings.RecommendationTopic
import com.yoon778.lexiloop.presentation.contract.AppRoute
import com.yoon778.lexiloop.presentation.contract.LoadState
import com.yoon778.lexiloop.presentation.contract.OnboardingEvent
import com.yoon778.lexiloop.presentation.contract.OnboardingUiState
import com.yoon778.lexiloop.presentation.contract.UiEffect
import kotlinx.coroutines.launch
import java.util.UUID

class OnboardingViewModel(
    private val analyze: suspend (PurposeAnalysisRequest) -> PurposeAnalysisResponse,
    private val saveProfile: suspend (String, Int, RecommendationProfile) -> Unit,
) : ContractViewModel<OnboardingUiState, OnboardingEvent>(OnboardingUiState()) {
    override fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.PurposeChanged -> mutableState.value = state.value.copy(purposeText = event.value.take(1_000))
            is OnboardingEvent.DifficultySelected -> mutableState.value = state.value.copy(difficulty = event.value)
            is OnboardingEvent.DailyNewCountSelected -> mutableState.value = state.value.copy(dailyNewCount = event.value.coerceIn(1, 100))
            OnboardingEvent.AnalyzeRequested, OnboardingEvent.RetryRequested -> requestAnalysis()
            OnboardingEvent.AnalysisAccepted -> acceptAnalysis()
            OnboardingEvent.UseStarterDeck -> emit(UiEffect.Navigate(AppRoute.Diagnosis))
            is OnboardingEvent.DiagnosisAnswered -> Unit
        }
    }

    private fun requestAnalysis() {
        val current = state.value
        if (!current.isSubmitEnabled) return
        viewModelScope.launch {
            mutableState.value = current.copy(isAnalyzing = true, analysis = LoadState.Loading)
            runCatching {
                analyze(
                    PurposeAnalysisRequest(
                        requestId = UUID.randomUUID().toString(),
                        learningPurpose = current.purposeText.trim(),
                        selfAssessedDifficulty = current.difficulty,
                        dailyNewCount = current.dailyNewCount,
                    ),
                )
            }.onSuccess { response ->
                val profile = RecommendationProfile(
                    topics = response.profile.topics.map {
                        RecommendationTopic(UUID.randomUUID().toString(), it.name, it.weightPercent)
                    },
                    difficulty = response.profile.difficulty,
                    excludedTopics = response.profile.excludedTopics,
                )
                mutableState.value = state.value.copy(isAnalyzing = false, analysis = LoadState.Content(profile))
                emit(UiEffect.Navigate(AppRoute.OnboardingAnalysis))
            }.onFailure {
                mutableState.value = state.value.copy(
                    isAnalyzing = false,
                    analysis = LoadState.Error("분석에 실패했어요", canRetry = true),
                )
            }
        }
    }

    private fun acceptAnalysis() {
        val current = state.value
        val profile = (current.analysis as? LoadState.Content)?.value ?: return
        viewModelScope.launch {
            runCatching { saveProfile(current.purposeText, current.dailyNewCount, profile) }
                .onSuccess { emit(UiEffect.Navigate(AppRoute.Diagnosis)) }
                .onFailure { emit(UiEffect.Message("설정을 저장하지 못했어요")) }
        }
    }
}
