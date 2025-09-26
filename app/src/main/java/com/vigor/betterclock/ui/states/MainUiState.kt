package com.vigor.betterclock.ui.states

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class MainUiState(
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val arr: SnapshotStateList<Pair<Long, String>> = mutableStateListOf(),
    val text: String? = null,
    var settings_clock_hour_24: Boolean = true,
    val settings_dnd: Boolean = false,
    val settings_charge_animation: Boolean = false,
    val settings_charge_icon: Boolean = false,
    val settings_interval_s: String = "1"
)