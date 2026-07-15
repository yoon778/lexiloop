package com.yoon778.lexiloop.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.yoon778.lexiloop.presentation.contract.LoadState
import com.yoon778.lexiloop.presentation.contract.UiEffect
import com.yoon778.lexiloop.presentation.contract.WordManagementEvent
import com.yoon778.lexiloop.presentation.contract.WordManagementUiState
import kotlinx.coroutines.launch

class WordManagementViewModel(
    private val load: suspend (String, com.yoon778.lexiloop.domain.model.LearningStatus?) -> LoadState.Content<List<com.yoon778.lexiloop.presentation.contract.WordListItemUiState>>,
    private val mutate: suspend (WordManagementEvent) -> Unit,
) : ContractViewModel<WordManagementUiState, WordManagementEvent>(WordManagementUiState()) {
    init { refresh() }

    override fun onEvent(event: WordManagementEvent) {
        when (event) {
            is WordManagementEvent.QueryChanged -> {
                mutableState.value = state.value.copy(query = event.value)
                refresh()
            }
            is WordManagementEvent.FilterSelected -> {
                mutableState.value = state.value.copy(statusFilter = event.value)
                refresh()
            }
            WordManagementEvent.Retry -> refresh()
            else -> viewModelScope.launch {
                runCatching { mutate(event) }
                    .onSuccess { refresh() }
                    .onFailure { emit(UiEffect.Message("변경하지 못했어요")) }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            mutableState.value = state.value.copy(words = LoadState.Loading)
            mutableState.value = runCatching { load(state.value.query, state.value.statusFilter) }
                .fold(
                    onSuccess = { state.value.copy(words = it) },
                    onFailure = { state.value.copy(words = LoadState.Error("단어를 불러오지 못했어요", true)) },
                )
        }
    }
}
