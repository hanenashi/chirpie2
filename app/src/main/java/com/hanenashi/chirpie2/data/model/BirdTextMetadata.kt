package com.hanenashi.chirpie2.data.model

data class BirdTextMetadata(
    val birdId: Long,
    val kanjiJapaneseName: String,
    val romanizedJapaneseName: String,
    val englishName: String,
    val scientificName: String,
    val czechName: String
)

fun Bird.textMetadata(): BirdTextMetadata = BirdTextMetadata(
    birdId = id,
    kanjiJapaneseName = kanjiJapaneseName,
    romanizedJapaneseName = romanizedJapaneseName,
    englishName = englishName,
    scientificName = scientificName,
    czechName = czechName
)
