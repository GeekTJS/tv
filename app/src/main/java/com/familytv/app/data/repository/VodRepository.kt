package com.familytv.app.data.repository

import com.familytv.app.data.api.VodApiService
import com.familytv.app.data.local.FavoriteDao
import com.familytv.app.data.local.HistoryDao
import com.familytv.app.data.local.toFavoriteEntity
import com.familytv.app.data.local.toHistoryEntity
import com.familytv.app.data.local.toVodItem
import com.familytv.app.data.model.PlayEpisode
import com.familytv.app.data.model.VodItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VodRepository @Inject constructor(
    private val api: VodApiService,
    private val favoriteDao: FavoriteDao,
    private val historyDao: HistoryDao
) {
    suspend fun getVideoList(page: Int, typeId: Long? = null): Result<List<VodItem>> {
        return try {
            val response = api.getVideoList(page = page, typeId = typeId)
            if (response.code == 1) {
                Result.success(response.list)
            } else {
                Result.failure(Exception("获取数据失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVideoDetail(id: Long): Result<VodItem> {
        return try {
            val response = api.getVideoDetail(id = id)
            if (response.code == 1 && response.list.isNotEmpty()) {
                Result.success(response.list.first())
            } else {
                Result.failure(Exception("获取详情失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchVideo(keyword: String, page: Int): Result<List<VodItem>> {
        return try {
            val response = api.searchVideo(keyword = keyword, page = page)
            if (response.code == 1) {
                Result.success(response.list)
            } else {
                Result.failure(Exception("搜索失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseEpisodes(vodItem: VodItem): List<PlayEpisode> {
        val episodes = mutableListOf<PlayEpisode>()
        if (vodItem.vodPlayUrl.isEmpty()) return episodes
        
        val playUrls = vodItem.vodPlayUrl.split("#")
        for (playUrl in playUrls) {
            val parts = playUrl.split("$")
            if (parts.size >= 2) {
                episodes.add(PlayEpisode(
                    title = parts[0].trim(),
                    url = parts[1].trim()
                ))
            }
        }
        return episodes
    }

    suspend fun addToFavorite(vodItem: VodItem) {
        favoriteDao.insert(vodItem.toFavoriteEntity())
    }

    suspend fun removeFromFavorite(id: Long) {
        favoriteDao.deleteById(id)
    }

    suspend fun isFavorite(id: Long): Boolean {
        return favoriteDao.getById(id) != null
    }

    fun getFavorites(): Flow<List<VodItem>> {
        return favoriteDao.getAll().map { entities ->
            entities.map { entity -> entity.toVodItem() }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun addHistory(vodItem: VodItem, episodeIndex: Int, progress: Long) {
        historyDao.insert(vodItem.toHistoryEntity(episodeIndex, progress))
    }

    suspend fun updateProgress(id: Long, episodeIndex: Int, progress: Long) {
        historyDao.updateProgress(id, episodeIndex, progress)
    }

    fun getHistories(): Flow<List<VodItem>> {
        return historyDao.getAll().map { entities ->
            entities.map { entity -> entity.toVodItem() }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun clearHistory() {
        historyDao.deleteAll()
    }

    suspend fun getHistoryProgress(id: Long): Pair<Int, Long>? {
        val entity = historyDao.getById(id)
        return if (entity != null) Pair(entity.episodeIndex, entity.progress) else null
    }
}
