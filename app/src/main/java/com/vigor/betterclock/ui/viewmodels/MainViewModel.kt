package com.vigor.betterclock.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.vigor.betterclock.ui.states.MainUiState
import com.vigor.betterclock.utils.PrefUtils
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

    fun update_settings_data(context: Context) {
        val prefUtils = PrefUtils(context)
        _uiState.update { state ->
            state.copy(settings_dnd=prefUtils.dnd, settings_charge_animation=prefUtils.charge_animation, settings_charge_icon=prefUtils.charge_icon, settings_interval_s=prefUtils.interval_s.toString())
        }
    }

    fun _push_setting(k: String) {
        var nv: Boolean? = null
        when (k) {
            "clock_hour_24" -> {
                _uiState.update { state ->
                    nv = !state.settings_clock_hour_24
                    state.copy(settings_clock_hour_24=nv)
                }
            }
            "dnd" -> {
                if (!check_permissions())
                    return
                _uiState.update { state ->
                    nv = !state.settings_dnd
                    state.copy(settings_dnd=nv)
                }
            }
            "charge_animation" -> {
                _uiState.update { state ->
                    nv = !state.settings_charge_animation
                    state.copy(settings_charge_animation=nv)
                }
            }
            "charge_icon" -> {
                _uiState.update { state ->
                    nv = !state.settings_charge_icon
                    state.copy(settings_charge_icon=nv)
                }
            }
            else -> return
        }
        nv?.let { push_setting(k, it) }
    }

    fun _push_setting(k: String, v: String) {
        when (k) {
            "interval_s" -> {
                _uiState.update { state ->
                    state.copy(settings_interval_s=v)
                }
            }
            else -> return
        }
        v.toIntOrNull()?.let { push_int_setting(k, it) }
    }

    var check_permissions: () -> Boolean = { false }
    var push_setting: (String, Boolean) -> Unit = {k,v -> }
    var push_int_setting: (String, Int) -> Unit = {k,v -> }
}