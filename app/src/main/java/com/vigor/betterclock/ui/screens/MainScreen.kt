package com.vigor.betterclock.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vigor.betterclock.ui.theme.BetterClockTheme
import com.vigor.betterclock.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewmodel: MainViewModel = viewModel()) {
    val state by viewmodel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            SettingRow(
                label = "24 Hour Mode",
                checked = state.settings_clock_hour_24,
                onCheckedChange = { viewmodel._push_setting("clock_hour_24")}
            )
            HorizontalDivider()
            SettingRow(
                label = "Do Not Disturb function",
                checked = state.settings_dnd,
                onCheckedChange = { viewmodel._push_setting("dnd") }
            )
            HorizontalDivider()
            SettingRow(
                label = "Charge Animation",
                checked = state.settings_charge_animation,
                onCheckedChange = { viewmodel._push_setting("charge_animation") }
            )
            HorizontalDivider()
            SettingRow(
                label = "Charge Icon",
                checked = state.settings_charge_icon,
                onCheckedChange = { viewmodel._push_setting("charge_icon") }
            )
            HorizontalDivider()
            SettingRowInt(
                label = "Animation interval (s)",
                value = state.settings_interval_s,
                onValueChange = { viewmodel._push_setting("interval_s", it) }
            )
        }
    }
}

@Composable
fun SettingRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingRowInt(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = value,
            onValueChange = { text_in: String ->
                var new_text = text_in.filter { it.isDigit() }
                if (new_text.isEmpty() || (new_text.toIntOrNull() != null && new_text.toInt() in 1..20) )
                    onValueChange(new_text)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            label = { Text(label) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BetterClockTheme {
        val viewmodel: MainViewModel = viewModel()
        MainScreen(viewmodel)
    }
}