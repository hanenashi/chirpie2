package com.hanenashi.chirpie2.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hanenashi.chirpie2.data.model.Bird
import kotlinx.coroutines.flow.Flow

@Dao
interface BirdDao {
    @Query("SELECT * FROM birds ORDER BY englishName ASC")
    fun observeAllBirds(): Flow<List<Bird>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(birds: List<Bird>)

    @Query("SELECT COUNT(*) FROM birds")
    suspend fun birdCount(): Int
}
