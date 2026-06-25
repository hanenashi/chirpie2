package com.hanenashi.chirpie2.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.hanenashi.chirpie2.data.local.BirdAssetSeeder
import com.hanenashi.chirpie2.data.local.BirdDao
import com.hanenashi.chirpie2.data.model.Bird
import com.hanenashi.chirpie2.data.model.BirdListMembership
import com.hanenashi.chirpie2.data.model.BirdTextMetadata
import com.hanenashi.chirpie2.data.model.CustomBirdImport
import com.hanenashi.chirpie2.data.preferences.BirdList
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BirdRepository(
    private val birdDao: BirdDao,
    context: Context
) {
    private val appContext = context.applicationContext
    private val assets = appContext.assets

    fun observeBirds(): Flow<List<Bird>> = birdDao.observeAllBirds()

    fun observeListMemberships(): Flow<List<BirdListMembership>> =
        birdDao.observeListMemberships()

    suspend fun saveOrder(birdIds: List<Long>) {
        birdDao.saveOrder(birdIds)
    }

    suspend fun resetOrder() {
        birdDao.resetOrder()
    }

    suspend fun setListMembership(birdId: Long, list: BirdList, isMember: Boolean) {
        require(list.isEditable)
        if (isMember) {
            birdDao.addToList(BirdListMembership(birdId, list.name))
        } else {
            birdDao.removeFromList(birdId, list.name)
        }
    }

    suspend fun updateTextMetadata(metadata: BirdTextMetadata) {
        birdDao.updateTextMetadata(
            birdId = metadata.birdId,
            kanjiJapaneseName = metadata.kanjiJapaneseName.trim(),
            romanizedJapaneseName = metadata.romanizedJapaneseName.trim(),
            englishName = metadata.englishName.trim(),
            scientificName = metadata.scientificName.trim(),
            czechName = metadata.czechName.trim()
        )
    }

    suspend fun resetTextMetadata(birdId: Long) {
        val sourceBird = BirdAssetSeeder.load(assets).firstOrNull { it.id == birdId } ?: return
        birdDao.updateTextMetadata(
            birdId = sourceBird.id,
            kanjiJapaneseName = sourceBird.kanjiJapaneseName,
            romanizedJapaneseName = sourceBird.romanizedJapaneseName,
            englishName = sourceBird.englishName,
            scientificName = sourceBird.scientificName,
            czechName = sourceBird.czechName
        )
    }

    suspend fun importCustomBird(import: CustomBirdImport) = withContext(Dispatchers.IO) {
        require(import.imageUri.isNotBlank()) { "Choose a bird image." }
        require(import.audioUris.isNotEmpty()) { "Choose at least one MP3." }
        require(import.audioUris.size <= 2) { "Choose no more than two MP3 files." }
        require(
            listOf(
                import.kanjiJapaneseName,
                import.romanizedJapaneseName,
                import.englishName,
                import.scientificName,
                import.czechName
            ).any { it.isNotBlank() }
        ) { "Enter at least one bird name." }

        val birdId = birdDao.nextBirdId()
        val destination = File(appContext.filesDir, "custom_birds/$birdId")
        check(destination.mkdirs()) { "Could not create custom bird storage." }

        try {
            val imageExtension = imageExtension(import.imageUri.toUri())
            val imageFile = File(destination, "image.$imageExtension")
            copyUri(import.imageUri.toUri(), imageFile)

            val audioFiles = import.audioUris.mapIndexed { index, uriString ->
                File(destination, "audio_${index + 1}.mp3").also { file ->
                    copyUri(uriString.toUri(), file)
                }
            }

            birdDao.insert(
                Bird(
                    id = birdId,
                    englishName = import.englishName.trim(),
                    romanizedJapaneseName = import.romanizedJapaneseName.trim(),
                    kanjiJapaneseName = import.kanjiJapaneseName.trim(),
                    czechName = import.czechName.trim(),
                    scientificName = import.scientificName.trim(),
                    imageUrl = Uri.fromFile(imageFile).toString(),
                    audioAssetPath = audioFiles.joinToString("\n") {
                        Uri.fromFile(it).toString()
                    },
                    sortIndex = birdDao.nextSortIndex()
                )
            )
        } catch (error: Exception) {
            destination.deleteRecursively()
            throw error
        }
    }

    suspend fun deleteCustomBird(bird: Bird) = withContext(Dispatchers.IO) {
        require(!bird.imageUrl.startsWith("file:///android_asset/")) {
            "Bundled birds cannot be deleted."
        }
        birdDao.deleteBird(bird.id)
        File(appContext.filesDir, "custom_birds/${bird.id}").deleteRecursively()
    }

    private fun copyUri(uri: Uri, destination: File) {
        val input = appContext.contentResolver.openInputStream(uri)
            ?: error("Could not open selected file.")
        input.use { source ->
            destination.outputStream().use(source::copyTo)
        }
    }

    private fun imageExtension(uri: Uri): String {
        return when (appContext.contentResolver.getType(uri)) {
            "image/png" -> "png"
            else -> "jpg"
        }
    }
}
