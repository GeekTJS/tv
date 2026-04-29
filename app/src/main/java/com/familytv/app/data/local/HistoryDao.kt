package com.familytv.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM histories ORDER BY watchTime DESC")
    fun getAll(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM histories WHERE vodId = :id")
    suspend fun getById(id: Long): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HistoryEntity)

    @Query("UPDATE histories SET episodeIndex = :episodeIndex, progress = :progress WHERE vodId = :id")
    suspend fun updateProgress(id: Long, episodeIndex: Int, progress: Long)

    @Delete
    suspend fun delete(entity: HistoryEntity)

    @Query("DELETE FROM histories")
    suspend fun deleteAll()
}
