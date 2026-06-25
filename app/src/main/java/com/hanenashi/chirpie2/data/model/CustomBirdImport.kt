package com.hanenashi.chirpie2.data.model

data class CustomBirdImport(
    val kanjiJapaneseName: String,
    val romanizedJapaneseName: String,
    val englishName: String,
    val scientificName: String,
    val czechName: String,
    val imageUri: String,
    val audioUris: List<String>
)
