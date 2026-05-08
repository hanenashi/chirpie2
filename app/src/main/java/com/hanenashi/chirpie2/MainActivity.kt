package com.hanenashi.chirpie2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.hanenashi.chirpie2.data.local.ChirpieDatabase
import com.hanenashi.chirpie2.data.repository.BirdRepository
import com.hanenashi.chirpie2.ui.screens.BirdListScreen
import com.hanenashi.chirpie2.ui.theme.ChirpieTheme
import com.hanenashi.chirpie2.viewmodel.BirdListViewModel
import com.hanenashi.chirpie2.viewmodel.BirdListViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: BirdListViewModel by viewModels {
        BirdListViewModelFactory(
            BirdRepository(
                ChirpieDatabase.getInstance(this).birdDao()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChirpieTheme {
                val uiState by viewModel.uiState.collectAsState()
                BirdListScreen(
                    birds = uiState.birds,
                    isLoading = uiState.isLoading
                )
            }
        }
    }
}
