package com.hanenashi.chirpie2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "birds")
data class Bird(
    @PrimaryKey
    val id: Long,
    val englishName: String,
    val romanizedJapaneseName: String,
    val kanjiJapaneseName: String,
    val czechName: String,
    val scientificName: String,
    val imageUrl: String,
    val audioAssetPath: String
) {
    fun audioAssetPaths(): List<String> = audioAssetPath
        .lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}
