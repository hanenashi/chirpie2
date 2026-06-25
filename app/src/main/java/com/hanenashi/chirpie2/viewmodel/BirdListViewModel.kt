package com.hanenashi.chirpie2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hanenashi.chirpie2.data.model.Bird
import com.hanenashi.chirpie2.data.model.BirdTextMetadata
import com.hanenashi.chirpie2.data.model.CustomBirdImport
import com.hanenashi.chirpie2.data.preferences.BirdPreferences
import com.hanenashi.chirpie2.data.preferences.BirdList
import com.hanenashi.chirpie2.data.preferences.DisplayMode
import com.hanenashi.chirpie2.data.preferences.SortOrder
import com.hanenashi.chirpie2.data.repository.BirdRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Locale

class BirdListViewModel(
    private val repository: BirdRepository,
    private val preferences: BirdPreferences
) : ViewModel() {
    private val importStatus = MutableStateFlow(CustomBirdImportStatus())

    val uiState: StateFlow<BirdListUiState> = combine(
        repository.observeBirds(),
        repository.observeListMemberships(),
        preferences.settings,
        importStatus
    ) { birds, memberships, settings, currentImportStatus ->
        val membershipsByBird = memberships
            .groupBy { it.birdId }
            .mapValues { (_, values) ->
                values.mapNotNull { membership ->
                    BirdList.entries.firstOrNull { it.name == membership.listName }
                }.toSet()
            }
        BirdListUiState(
            birds = sortBirds(
                filterBirds(birds, settings.activeList, membershipsByBird),
                settings.sortOrder
            ),
            displayMode = settings.displayMode,
            sortOrder = settings.sortOrder,
            activeList = settings.activeList,
            gridColumns = settings.gridColumns,
            membershipsByBird = membershipsByBird,
            importStatus = currentImportStatus,
            isLoading = false
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BirdListUiState(isLoading = true)
        )

    fun setDisplayMode(displayMode: DisplayMode) {
        preferences.setDisplayMode(displayMode)
    }

    fun setSortOrder(sortOrder: SortOrder) {
        preferences.setSortOrder(sortOrder)
    }

    fun setActiveList(activeList: BirdList) {
        preferences.setActiveList(activeList)
    }

    fun setGridColumns(gridColumns: Int) {
        preferences.setGridColumns(gridColumns)
    }

    fun setListMembership(birdId: Long, list: BirdList, isMember: Boolean) {
        viewModelScope.launch {
            repository.setListMembership(birdId, list, isMember)
        }
    }

    fun updateTextMetadata(metadata: BirdTextMetadata) {
        viewModelScope.launch {
            repository.updateTextMetadata(metadata)
        }
    }

    fun resetTextMetadata(birdId: Long) {
        viewModelScope.launch {
            repository.resetTextMetadata(birdId)
        }
    }

    fun importCustomBird(import: CustomBirdImport) {
        viewModelScope.launch {
            importStatus.value = CustomBirdImportStatus(isImporting = true)
            runCatching {
                repository.importCustomBird(import)
            }.onSuccess {
                preferences.setActiveList(BirdList.All)
                importStatus.value = CustomBirdImportStatus(
                    message = "Custom bird imported."
                )
            }.onFailure { error ->
                importStatus.value = CustomBirdImportStatus(
                    message = error.message ?: "The selected files could not be imported.",
                    isError = true
                )
            }
        }
    }

    fun deleteCustomBird(bird: Bird) {
        viewModelScope.launch {
            repository.deleteCustomBird(bird)
        }
    }

    fun clearImportMessage() {
        importStatus.value = CustomBirdImportStatus()
    }

    fun resetOrder() {
        viewModelScope.launch {
            repository.resetOrder()
            preferences.resetOrder()
        }
    }

    fun saveOrder(birdIds: List<Long>) {
        viewModelScope.launch {
            repository.saveOrder(birdIds)
            preferences.setSortOrder(SortOrder.Custom)
        }
    }
}

data class BirdListUiState(
    val birds: List<Bird> = emptyList(),
    val displayMode: DisplayMode = DisplayMode.Grid,
    val sortOrder: SortOrder = SortOrder.Custom,
    val activeList: BirdList = BirdList.All,
    val gridColumns: Int = 3,
    val membershipsByBird: Map<Long, Set<BirdList>> = emptyMap(),
    val importStatus: CustomBirdImportStatus = CustomBirdImportStatus(),
    val isLoading: Boolean = true
)

data class CustomBirdImportStatus(
    val isImporting: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false
)

class BirdListViewModelFactory(
    private val repository: BirdRepository,
    private val preferences: BirdPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BirdListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BirdListViewModel(repository, preferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

internal fun filterBirds(
    birds: List<Bird>,
    activeList: BirdList,
    membershipsByBird: Map<Long, Set<BirdList>>
): List<Bird> {
    if (activeList == BirdList.All) return birds
    return birds.filter { activeList in membershipsByBird[it.id].orEmpty() }
}

internal fun sortBirds(birds: List<Bird>, sortOrder: SortOrder): List<Bird> {
    val selector: (Bird) -> String = when (sortOrder) {
        SortOrder.Custom -> return birds.sortedWith(
            compareBy<Bird>(Bird::sortIndex).thenBy(Bird::id)
        )
        SortOrder.Japanese -> Bird::kanjiJapaneseName
        SortOrder.English -> Bird::englishName
        SortOrder.Czech -> Bird::czechName
        SortOrder.Scientific -> Bird::scientificName
    }

    val locale = when (sortOrder) {
        SortOrder.Japanese -> Locale.JAPANESE
        SortOrder.Czech -> Locale.forLanguageTag("cs")
        else -> Locale.ENGLISH
    }
    val collator = Collator.getInstance(locale).apply {
        strength = Collator.PRIMARY
    }
    return birds.sortedWith(
        compareBy(collator, selector)
            .thenBy(Bird::id)
    )
}

private fun compareBy(
    collator: Collator,
    selector: (Bird) -> String
): Comparator<Bird> = Comparator { first, second ->
    collator.compare(selector(first), selector(second))
}
