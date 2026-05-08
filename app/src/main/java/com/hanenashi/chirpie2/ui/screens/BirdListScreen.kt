package com.hanenashi.chirpie2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.hanenashi.chirpie2.data.model.Bird

@Composable
fun BirdListScreen(
    birds: List<Bird>,
    isLoading: Boolean,
    onPlayAudio: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedBird by remember { mutableStateOf<Bird?>(null) }

    if (isLoading) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Text(text = "Loading birds...", modifier = Modifier.padding(top = 12.dp))
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 132.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items = birds, key = { it.id }) { bird ->
                BirdTile(
                    bird = bird,
                    onClick = { selectedBird = bird }
                )
            }
        }
    }

    selectedBird?.let { bird ->
        BirdDetailsDialog(
            bird = bird,
            onDismiss = { selectedBird = null },
            onPlayAudio = onPlayAudio
        )
    }
}

@Composable
private fun BirdTile(
    bird: Bird,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        AsyncImage(
            model = bird.imageUrl,
            contentDescription = bird.englishName,
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BirdDetailsDialog(
    bird: Bird,
    onDismiss: () -> Unit,
    onPlayAudio: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = bird.imageUrl,
                    contentDescription = bird.englishName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Fit
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = bird.kanjiJapaneseName, style = MaterialTheme.typography.headlineSmall)
                    Text(text = bird.romanizedJapaneseName, style = MaterialTheme.typography.titleMedium)
                    Text(text = bird.englishName, style = MaterialTheme.typography.bodyLarge)
                    Text(text = bird.scientificName, style = MaterialTheme.typography.bodyMedium)
                    Text(text = bird.czechName, style = MaterialTheme.typography.bodyMedium)
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    bird.audioAssetPaths().forEachIndexed { index, assetPath ->
                        TextButton(onClick = { onPlayAudio(assetPath) }) {
                            Text(text = "Play ${index + 1}")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "Close")
                    }
                }
            }
        }
    }
}
