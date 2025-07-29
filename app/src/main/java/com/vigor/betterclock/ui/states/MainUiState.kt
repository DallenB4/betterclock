package com.vigor.betterclock.ui.states

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class MainUiState(
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val arr: SnapshotStateList<Pair<Long, String>> = mutableStateListOf(),
    val text: String? = null
)