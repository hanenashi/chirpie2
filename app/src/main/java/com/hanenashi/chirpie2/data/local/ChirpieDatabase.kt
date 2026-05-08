package com.hanenashi.chirpie2.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hanenashi.chirpie2.data.model.Bird
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(entities = [Bird::class], version = 1, exportSchema = false)
abstract class ChirpieDatabase : RoomDatabase() {
    abstract fun birdDao(): BirdDao

    companion object {
        @Volatile
        private var instance: ChirpieDatabase? = null

        fun getInstance(context: Context): ChirpieDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ChirpieDatabase::class.java,
                    "chirpie.db"
                )
                    .addCallback(SeedDatabaseCallback)
                    .build()
                    .also { instance = it }
            }
        }

        private object SeedDatabaseCallback : Callback() {
            private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                instance?.let { database ->
                    scope.launch {
                        database.birdDao().insertAll(seedBirds)
                    }
                }
            }
        }

        private val seedBirds = listOf(
            Bird(
                englishName = "Eurasian Blue Tit",
                romanizedJapaneseName = "Ruribitaki",
                kanjiJapaneseName = "瑠璃鶲",
                czechName = "Sýkora modřinka",
                scientificName = "Cyanistes caeruleus",
                imageUrl = "https://example.com/blue_tit.jpg",
                audioAssetPath = "audio/blue_tit.mp3"
            ),
            Bird(
                englishName = "Common Blackbird",
                romanizedJapaneseName = "Kurotsugumi",
                kanjiJapaneseName = "黒鶫",
                czechName = "Kos černý",
                scientificName = "Turdus merula",
                imageUrl = "https://example.com/blackbird.jpg",
                audioAssetPath = "audio/blackbird.mp3"
            ),
            Bird(
                englishName = "Barn Swallow",
                romanizedJapaneseName = "Tsubame",
                kanjiJapaneseName = "燕",
                czechName = "Vlaštovka obecná",
                scientificName = "Hirundo rustica",
                imageUrl = "https://example.com/barn_swallow.jpg",
                audioAssetPath = "audio/barn_swallow.mp3"
            )
        )
    }
}
