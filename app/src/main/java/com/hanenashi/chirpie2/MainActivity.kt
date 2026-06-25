package com.hanenashi.chirpie2

import android.media.MediaPlayer
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import com.hanenashi.chirpie2.data.local.ChirpieDatabase
import com.hanenashi.chirpie2.data.preferences.BirdPreferences
import com.hanenashi.chirpie2.data.repository.BirdRepository
import com.hanenashi.chirpie2.ui.screens.BirdListScreen
import com.hanenashi.chirpie2.ui.theme.ChirpieTheme
import com.hanenashi.chirpie2.viewmodel.BirdListViewModel
import com.hanenashi.chirpie2.viewmodel.BirdListViewModelFactory

class MainActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var playingAudioAsset by mutableStateOf<String?>(null)
    private var screenStoppedAsset: String? = null
    private var screenStoppedAt = 0L

    private val viewModel: BirdListViewModel by viewModels {
        BirdListViewModelFactory(
            BirdRepository(
                ChirpieDatabase.getInstance(this).birdDao(),
                this
            ),
            BirdPreferences(this)
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
                    displayMode = uiState.displayMode,
                    sortOrder = uiState.sortOrder,
                    activeList = uiState.activeList,
                    gridColumns = uiState.gridColumns,
                    membershipsByBird = uiState.membershipsByBird,
                    importStatus = uiState.importStatus,
                    isLoading = uiState.isLoading,
                    playingAudioAsset = playingAudioAsset,
                    onToggleAudio = ::toggleAudioAsset,
                    onScreenPress = ::stopAudioFromScreenPress,
                    onDisplayModeChange = viewModel::setDisplayMode,
                    onSortOrderChange = viewModel::setSortOrder,
                    onActiveListChange = viewModel::setActiveList,
                    onGridColumnsChange = viewModel::setGridColumns,
                    onListMembershipChange = viewModel::setListMembership,
                    onUpdateTextMetadata = viewModel::updateTextMetadata,
                    onResetTextMetadata = viewModel::resetTextMetadata,
                    onImportCustomBird = viewModel::importCustomBird,
                    onDeleteCustomBird = viewModel::deleteCustomBird,
                    onDismissImportMessage = viewModel::clearImportMessage,
                    onResetOrder = viewModel::resetOrder,
                    onSaveOrder = viewModel::saveOrder
                )
            }
        }
    }

    override fun onDestroy() {
        stopAudio()
        super.onDestroy()
    }

    private fun toggleAudioAsset(assetPath: String) {
        val stoppedByThisPress = screenStoppedAsset == assetPath &&
            SystemClock.uptimeMillis() - screenStoppedAt < SCREEN_PRESS_WINDOW_MS
        screenStoppedAsset = null
        if (stoppedByThisPress || playingAudioAsset == assetPath) {
            stopAudio()
            return
        }

        stopAudio()

        mediaPlayer = MediaPlayer().apply {
            if (assetPath.startsWith("file:")) {
                setDataSource(this@MainActivity, assetPath.toUri())
            } else {
                assets.openFd(assetPath).use { descriptor ->
                    setDataSource(
                        descriptor.fileDescriptor,
                        descriptor.startOffset,
                        descriptor.length
                    )
                }
            }
            setOnCompletionListener {
                it.release()
                if (mediaPlayer == it) {
                    mediaPlayer = null
                    playingAudioAsset = null
                }
            }
            prepare()
            start()
        }
        playingAudioAsset = assetPath
    }

    private fun stopAudioFromScreenPress() {
        val asset = playingAudioAsset ?: return
        screenStoppedAsset = asset
        screenStoppedAt = SystemClock.uptimeMillis()
        stopAudio()
    }

    private fun stopAudio() {
        mediaPlayer?.release()
        mediaPlayer = null
        playingAudioAsset = null
    }

    private companion object {
        const val SCREEN_PRESS_WINDOW_MS = 750L
    }
}
