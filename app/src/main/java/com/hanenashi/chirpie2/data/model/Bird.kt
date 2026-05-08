package com.hanenashi.chirpie2.data.model

data class Bird(
    val id: Long,
    val englishName: String,
    val japaneseRomanized: String,
    val japaneseKanji: String,
    val czechName: String,
    val scientificName: String,
    val imagePath: String,
    val audioPath: String
)
