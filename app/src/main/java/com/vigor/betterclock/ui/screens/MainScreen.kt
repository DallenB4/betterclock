package com.vigor.betterclock.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vigor.betterclock.ui.theme.BetterClockTheme
import com.vigor.betterclock.ui.viewmodels.MainViewModel

@Composable
fun MainScreen(viewmodel: MainViewModel = viewModel()) {
    val states by viewmodel.uiState.collectAsState()
    Scaffold (
        floatingActionButton = {

        }
    ){ innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            states.text?.let { Text(text=it) }
            LazyColumn(
                modifier = Modifier
                    .padding(6.dp, 3.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = 72.dp)
            ) {
                itemsIndexed(
                    states.arr,
                    key = { _, el -> el.hashCode() }) { index, pair ->
                    val id = pair.first
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                println("Clicked on $id at index $index")
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$id",
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp, top = 16.dp, bottom = 16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BetterClockTheme {
        val viewmodel: MainViewModel = viewModel()
        viewmodel.text = "Test"
        MainScreen(viewmodel)
    }
}