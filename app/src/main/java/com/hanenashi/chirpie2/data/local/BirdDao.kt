package com.hanenashi.chirpie2.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.hanenashi.chirpie2.data.model.Bird
import com.hanenashi.chirpie2.data.model.BirdListMembership
import kotlinx.coroutines.flow.Flow

@Dao
interface BirdDao {
    @Query("SELECT * FROM birds ORDER BY sortIndex ASC, id ASC")
    fun observeAllBirds(): Flow<List<Bird>>

    @Query("SELECT * FROM bird_list_memberships")
    fun observeListMemberships(): Flow<List<BirdListMembership>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(birds: List<Bird>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(bird: Bird)

    @Query("SELECT COUNT(*) FROM birds")
    suspend fun birdCount(): Int

    @Query("SELECT COALESCE(MAX(id), 0) + 1 FROM birds")
    suspend fun nextBirdId(): Long

    @Query("SELECT COALESCE(MAX(sortIndex), -1) + 1 FROM birds")
    suspend fun nextSortIndex(): Long

    @Query("DELETE FROM birds WHERE id = :birdId")
    suspend fun deleteBird(birdId: Long)

    @Query("UPDATE birds SET sortIndex = :sortIndex WHERE id = :birdId")
    suspend fun updateSortIndex(birdId: Long, sortIndex: Long)

    @Transaction
    suspend fun saveOrder(birdIds: List<Long>) {
        birdIds.forEachIndexed { index, birdId ->
            updateSortIndex(birdId, index.toLong())
        }
    }

    @Query("UPDATE birds SET sortIndex = id")
    suspend fun resetOrder()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addToList(membership: BirdListMembership)

    @Query(
        "DELETE FROM bird_list_memberships " +
            "WHERE birdId = :birdId AND listName = :listName"
    )
    suspend fun removeFromList(birdId: Long, listName: String)

    @Query(
        """
        UPDATE birds SET
            kanjiJapaneseName = :kanjiJapaneseName,
            romanizedJapaneseName = :romanizedJapaneseName,
            englishName = :englishName,
            scientificName = :scientificName,
            czechName = :czechName
        WHERE id = :birdId
        """
    )
    suspend fun updateTextMetadata(
        birdId: Long,
        kanjiJapaneseName: String,
        romanizedJapaneseName: String,
        englishName: String,
        scientificName: String,
        czechName: String
    )
}
