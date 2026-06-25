package com.hanenashi.chirpie2.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hanenashi.chirpie2.data.model.Bird
import com.hanenashi.chirpie2.data.model.BirdListMembership
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [Bird::class, BirdListMembership::class],
    version = 4,
    exportSchema = false
)
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
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE birds ADD COLUMN sortIndex INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL("UPDATE birds SET sortIndex = id")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS bird_list_memberships (
                        birdId INTEGER NOT NULL,
                        listName TEXT NOT NULL,
                        PRIMARY KEY(birdId, listName),
                        FOREIGN KEY(birdId) REFERENCES birds(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_bird_list_memberships_birdId " +
                        "ON bird_list_memberships(birdId)"
                )
            }
        }
    }
}
