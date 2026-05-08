package com.hanenashi.chirpie2.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hanenashi.chirpie2.data.model.Bird
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(entities = [Bird::class], version = 2, exportSchema = false)
abstract class ChirpieDatabase : RoomDatabase() {
    abstract fun birdDao(): BirdDao

    companion object {
        @Volatile
        private var instance: ChirpieDatabase? = null

        fun getInstance(context: Context): ChirpieDatabase {
            val appContext = context.applicationContext
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    appContext,
                    ChirpieDatabase::class.java,
                    "chirpie.db"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { database ->
                        instance = database
                        seedFromAssetsIfNeeded(appContext, database)
                    }
            }
        }

        private fun seedFromAssetsIfNeeded(
            context: Context,
            database: ChirpieDatabase
        ) {
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                if (database.birdDao().birdCount() == 0) {
                    database.birdDao().insertAll(BirdAssetSeeder.load(context.assets))
                }
            }
        }
    }
}
