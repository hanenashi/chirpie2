package com.hanenashi.chirpie2.data.repository

import com.hanenashi.chirpie2.data.local.BirdDao
import com.hanenashi.chirpie2.data.model.Bird
import kotlinx.coroutines.flow.Flow

class BirdRepository(
    private val birdDao: BirdDao
) {
    fun observeBirds(): Flow<List<Bird>> = birdDao.observeAllBirds()
}
