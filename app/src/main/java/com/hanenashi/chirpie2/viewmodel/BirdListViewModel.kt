package com.hanenashi.chirpie2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hanenashi.chirpie2.data.model.Bird
import com.hanenashi.chirpie2.data.repository.BirdRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class BirdListViewModel(
    repository: BirdRepository
) : ViewModel() {
    val uiState: StateFlow<BirdListUiState> = repository
        .observeBirds()
        .map { BirdListUiState(birds = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BirdListUiState(isLoading = true)
        )
}

data class BirdListUiState(
    val birds: List<Bird> = emptyList(),
    val isLoading: Boolean = true
)

class BirdListViewModelFactory(
    private val repository: BirdRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BirdListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BirdListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
