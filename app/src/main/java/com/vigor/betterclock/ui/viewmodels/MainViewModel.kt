package com.vigor.betterclock.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.vigor.betterclock.ui.states.MainUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    init {
        _uiState.value = MainUiState()
    }

    var text: String?
        set(value) {
            _uiState.update { state -> state.copy(text=value) }
        }
        get() = _uiState.value.text
}