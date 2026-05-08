package com.hanenashi.chirpie2.data.local

import android.content.res.AssetManager
import com.hanenashi.chirpie2.data.model.Bird
import org.json.JSONObject

object BirdAssetSeeder {
    fun load(assets: AssetManager): List<Bird> {
        val birdsJson = JSONObject(assets.readText("birds.json"))

        return birdsJson.keys().asSequence()
            .map { folder ->
                val birdJson = birdsJson.getJSONObject(folder)
                val info = assets.readKeyValueText(birdJson.getString("txt"))
                val mp3Array = birdJson.getJSONArray("mp3")
                val audioPaths = buildList {
                    for (index in 0 until mp3Array.length()) {
                        add(mp3Array.getJSONObject(index).getString("file"))
                    }
                }

                Bird(
                    id = info["id"]?.toLongOrNull() ?: folder.hashCode().toLong(),
                    englishName = info["english"].orEmpty(),
                    romanizedJapaneseName = info["romanized"].orEmpty(),
                    kanjiJapaneseName = info["kanji"].orEmpty(),
                    czechName = info["czech"].orEmpty(),
                    scientificName = info["scientific"].orEmpty(),
                    imageUrl = "file:///android_asset/${birdJson.getString("jpg")}",
                    audioAssetPath = audioPaths.joinToString(separator = "\n")
                )
            }
            .sortedBy { it.id }
            .toList()
    }

    private fun AssetManager.readText(path: String): String =
        open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }

    private fun AssetManager.readKeyValueText(path: String): Map<String, String> =
        readText(path)
            .lineSequence()
            .mapNotNull { line ->
                val separatorIndex = line.indexOf('=')
                if (separatorIndex == -1) {
                    null
                } else {
                    line.substring(0, separatorIndex).trim() to
                        line.substring(separatorIndex + 1).trim()
                }
            }
            .toMap()
}
