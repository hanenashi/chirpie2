package com.hanenashi.chirpie2.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hanenashi.chirpie2.data.model.Bird

@Composable
fun BirdListScreen(
    birds: List<Bird>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Text(text = "Loading birds...", modifier = Modifier.padding(top = 12.dp))
        }
        return
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(items = birds, key = { it.id }) { bird ->
            BirdRow(bird = bird)
        }
    }
}

@Composable
private fun BirdRow(bird: Bird) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(text = bird.englishName, style = MaterialTheme.typography.titleMedium)
        Text(text = bird.scientificName, style = MaterialTheme.typography.bodyMedium)
        Text(text = "JP: ${bird.kanjiJapaneseName} (${bird.romanizedJapaneseName})")
        Text(text = "CZ: ${bird.czechName}")
    }
}
