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
    private val saveProfile: suspend (RecommendationProfile) -> Unit,
    private val generateRecommendations: suspend (RecommendationProfile) -> Unit,
    private val completeOnboarding: suspend (String, Int, RecommendationProfile) -> Unit,
) : ContractViewModel<OnboardingUiState, OnboardingEvent>(OnboardingUiState()) {
    override fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.PurposeChanged -> mutableState.value = state.value.copy(purposeText = event.value.take(1_000))
            is OnboardingEvent.DifficultySelected -> mutableState.value = state.value.copy(difficulty = event.value)
            is OnboardingEvent.DailyNewCountSelected -> mutableState.value = state.value.copy(dailyNewCount = event.value.coerceIn(1, 100))
            OnboardingEvent.AnalyzeRequested, OnboardingEvent.RetryRequested -> requestAnalysis()
            OnboardingEvent.AnalysisAccepted -> acceptAnalysis()
            OnboardingEvent.UseStarterDeck -> {
                mutableState.value = state.value.copy(analysis = LoadState.Content(RecommendationProfile()))
                emit(UiEffect.Navigate(AppRoute.Diagnosis))
            }
            is OnboardingEvent.DiagnosisAnswered -> advanceDiagnosis()
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
            mutableState.value = current.copy(isGenerating = true)
            runCatching {
                saveProfile(profile)
                generateRecommendations(profile)
            }.onSuccess {
                mutableState.value = state.value.copy(isGenerating = false)
                emit(UiEffect.Navigate(AppRoute.Diagnosis))
            }.onFailure { error ->
                mutableState.value = state.value.copy(isGenerating = false)
                val diagnostic = error.message?.takeIf(SAFE_DIAGNOSTIC::matches)
                val suffix = diagnostic?.let { " ($it)" }.orEmpty()
                emit(UiEffect.Message("단어 생성에 실패했어요$suffix"))
            }
        }
    }

    private fun advanceDiagnosis() {
        val current = state.value
        if (current.diagnosisIndex < diagnosisWords.size) {
            val nextIndex = current.diagnosisIndex + 1
            mutableState.value = current.copy(
                diagnosisIndex = nextIndex,
                diagnosisWord = diagnosisWords[nextIndex - 1],
            )
            return
        }
        val profile = (current.analysis as? LoadState.Content)?.value ?: RecommendationProfile()
        viewModelScope.launch {
            runCatching { completeOnboarding(current.purposeText, current.dailyNewCount, profile) }
                .onSuccess { emit(UiEffect.Navigate(AppRoute.Home, clearBackStack = true)) }
                .onFailure { emit(UiEffect.Message("온보딩을 저장하지 못했어요")) }
        }
    }

    private companion object {
        val SAFE_DIAGNOSTIC = Regex("^[A-Z_]+:[A-Za-z0-9_.\\[\\]-]+$")
        val diagnosisWords = listOf(
            "apple", "travel", "choose", "maintain", "grocery",
            "feature", "deploy", "reliable", "negotiate", "efficient",
            "architecture", "consequence", "ambiguous", "sustainable", "collaborate",
            "meticulous", "ubiquitous", "pragmatic", "resilient", "counterintuitive",
        )
    }
}
