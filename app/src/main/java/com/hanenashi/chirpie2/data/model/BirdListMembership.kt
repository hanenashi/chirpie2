package com.hanenashi.chirpie2.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "bird_list_memberships",
    primaryKeys = ["birdId", "listName"],
    foreignKeys = [
        ForeignKey(
            entity = Bird::class,
            parentColumns = ["id"],
            childColumns = ["birdId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("birdId")]
)
data class BirdListMembership(
    val birdId: Long,
    val listName: String
)
