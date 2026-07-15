package com.yoon778.lexiloop.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class ContractViewModel<S, E>(initialState: S) : ViewModel() {
    protected val mutableState = MutableStateFlow(initialState)
    val state: StateFlow<S> = mutableState.asStateFlow()

    private val effectChannel = Channel<com.yoon778.lexiloop.presentation.contract.UiEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    abstract fun onEvent(event: E)

    protected fun emit(effect: com.yoon778.lexiloop.presentation.contract.UiEffect) {
        viewModelScope.launch { effectChannel.send(effect) }
    }
}
