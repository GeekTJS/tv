package com.familytv.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val vodId: Long,
    val vodName: String,
    val vodPic: String,
    val vodYear: String,
    val vodArea: String,
    val vodScore: Double,
    val typeName: String,
    val vodRemarks: String,
    val vodActor: String,
    val vodDirector: String,
    val vodContent: String
)

@Entity(tableName = "histories")
data class HistoryEntity(
    @PrimaryKey val vodId: Long,
    val vodName: String,
    val vodPic: String,
    val vodYear: String,
    val vodArea: String,
    val vodScore: Double,
    val typeName: String,
    val vodRemarks: String,
    val vodActor: String,
    val vodDirector: String,
    val vodContent: String,
    val episodeIndex: Int,
    val progress: Long,
    val watchTime: Long = System.currentTimeMillis()
)
