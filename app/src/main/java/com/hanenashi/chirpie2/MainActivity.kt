package com.hanenashi.chirpie2

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
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
                    membershipsByBird = uiState.membershipsByBird,
                    importStatus = uiState.importStatus,
                    isLoading = uiState.isLoading,
                    onPlayAudio = ::playAudioAsset,
                    onDisplayModeChange = viewModel::setDisplayMode,
                    onSortOrderChange = viewModel::setSortOrder,
                    onActiveListChange = viewModel::setActiveList,
                    onListMembershipChange = viewModel::setListMembership,
                    onUpdateTextMetadata = viewModel::updateTextMetadata,
                    onResetTextMetadata = viewModel::resetTextMetadata,
                    onImportCustomBird = viewModel::importCustomBird,
                    onDismissImportMessage = viewModel::clearImportMessage,
                    onResetOrder = viewModel::resetOrder,
                    onSaveOrder = viewModel::saveOrder
                )
            }
        }
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    private fun playAudioAsset(assetPath: String) {
        mediaPlayer?.release()
        mediaPlayer = null

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
                if (mediaPlayer == it) mediaPlayer = null
            }
            prepare()
            start()
        }
    }
}
