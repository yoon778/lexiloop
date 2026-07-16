package com.yoon778.lexiloop.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.yoon778.lexiloop.presentation.contract.StudyEvent
import com.yoon778.lexiloop.presentation.contract.StudyUiState
import com.yoon778.lexiloop.presentation.contract.UiEffect
import kotlinx.coroutines.launch

data class StudyTransition(
    val state: StudyUiState,
    val effects: List<UiEffect> = emptyList(),
)

class StudyViewModel(
    sessionId: String,
    private val loadInitial: suspend () -> StudyUiState,
    private val reduce: suspend (StudyUiState, StudyEvent) -> StudyTransition,
) : ContractViewModel<StudyUiState, StudyEvent>(loadingState(sessionId)) {
    init {
        viewModelScope.launch {
            runCatching { loadInitial() }
                .onSuccess { mutableState.value = it }
                .onFailure { emit(UiEffect.Message("학습 세션을 불러오지 못했어요")) }
        }
    }

    override fun onEvent(event: StudyEvent) {
        if (event is StudyEvent.AnswerChanged) {
            mutableState.value = state.value.copy(
                answerText = event.value,
                canSubmit = event.value.isNotBlank(),
            )
            return
        }
        if (event == StudyEvent.RepeatPronunciation) {
            emit(UiEffect.Speak(state.value.expression))
            return
        }
        viewModelScope.launch {
            runCatching { reduce(state.value, event) }
                .onSuccess { transition ->
                    mutableState.value = transition.state
                    transition.effects.forEach(::emit)
                }
                .onFailure { emit(UiEffect.Message("학습 상태를 저장하지 못했어요")) }
        }
    }
}

private fun loadingState(sessionId: String) = StudyUiState(
    sessionId = sessionId,
    sessionType = com.yoon778.lexiloop.domain.model.SessionType.NEW,
    completedCount = 0,
    totalCount = 0,
    itemId = "",
    expression = "",
    targetMeaningKo = "",
    exampleSentence = "",
    exampleTranslationKo = "",
    phase = com.yoon778.lexiloop.domain.model.LearningPhase.CARD,
    canGoBack = false,
    isLoading = true,
)
