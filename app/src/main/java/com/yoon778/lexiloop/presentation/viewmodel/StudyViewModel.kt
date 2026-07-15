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
    initialState: StudyUiState,
    private val reduce: suspend (StudyUiState, StudyEvent) -> StudyTransition,
) : ContractViewModel<StudyUiState, StudyEvent>(initialState) {
    override fun onEvent(event: StudyEvent) {
        if (event is StudyEvent.AnswerChanged) {
            mutableState.value = state.value.copy(answerText = event.value)
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
