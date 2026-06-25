package com.hanenashi.chirpie2.data.preferences

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BirdPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val mutableSettings = MutableStateFlow(loadSettings())

    val settings: StateFlow<BirdSettings> = mutableSettings.asStateFlow()

    fun setDisplayMode(displayMode: DisplayMode) {
        updateSettings(mutableSettings.value.copy(displayMode = displayMode))
    }

    fun setSortOrder(sortOrder: SortOrder) {
        updateSettings(mutableSettings.value.copy(sortOrder = sortOrder))
    }

    fun setActiveList(activeList: BirdList) {
        updateSettings(mutableSettings.value.copy(activeList = activeList))
    }

    fun resetOrder() {
        setSortOrder(SortOrder.Custom)
    }

    private fun updateSettings(settings: BirdSettings) {
        preferences.edit {
            putString(KEY_DISPLAY_MODE, settings.displayMode.name)
            putString(KEY_SORT_ORDER, settings.sortOrder.name)
            putString(KEY_ACTIVE_LIST, settings.activeList.name)
        }
        mutableSettings.value = settings
    }

    private fun loadSettings(): BirdSettings = BirdSettings(
        displayMode = preferences.enumValue(KEY_DISPLAY_MODE, DisplayMode.Grid),
        sortOrder = preferences.enumValue(KEY_SORT_ORDER, SortOrder.Custom),
        activeList = preferences.enumValue(KEY_ACTIVE_LIST, BirdList.All)
    )

    private inline fun <reified T : Enum<T>> android.content.SharedPreferences.enumValue(
        key: String,
        defaultValue: T
    ): T {
        val storedValue = getString(key, null) ?: return defaultValue
        return enumValues<T>().firstOrNull { it.name == storedValue } ?: defaultValue
    }

    private companion object {
        const val PREFERENCES_NAME = "bird_settings"
        const val KEY_DISPLAY_MODE = "display_mode"
        const val KEY_SORT_ORDER = "sort_order"
        const val KEY_ACTIVE_LIST = "active_list"
    }
}

data class BirdSettings(
    val displayMode: DisplayMode = DisplayMode.Grid,
    val sortOrder: SortOrder = SortOrder.Custom,
    val activeList: BirdList = BirdList.All
)

enum class DisplayMode(val label: String) {
    Grid("Card grid"),
    List("Compact list")
}

enum class SortOrder(val label: String) {
    Custom("Custom order"),
    Japanese("Japanese name"),
    English("English name"),
    Czech("Czech name"),
    Scientific("Scientific name")
}

enum class BirdList(val label: String) {
    All("All birds"),
    Summer("Summer birds"),
    Winter("Winter birds"),
    Favorites("Favorites"),
    Study("Study list");

    val isEditable: Boolean
        get() = this != All
}
