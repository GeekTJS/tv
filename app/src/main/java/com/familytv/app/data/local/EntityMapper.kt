package com.familytv.app.data.local

import com.familytv.app.data.model.VodItem

fun VodItem.toFavoriteEntity(): FavoriteEntity {
    return FavoriteEntity(
        vodId = vodId,
        vodName = vodName,
        vodPic = vodPic,
        vodYear = vodYear,
        vodArea = vodArea,
        vodScore = vodScore,
        typeName = typeName,
        vodRemarks = vodRemarks,
        vodActor = vodActor,
        vodDirector = vodDirector,
        vodContent = vodContent
    )
}

fun FavoriteEntity.toVodItem(): VodItem {
    return VodItem(
        vodId = vodId,
        vodName = vodName,
        vodPic = vodPic,
        vodYear = vodYear,
        vodArea = vodArea,
        vodScore = vodScore,
        typeName = typeName,
        vodRemarks = vodRemarks,
        vodActor = vodActor,
        vodDirector = vodDirector,
        vodContent = vodContent
    )
}

fun VodItem.toHistoryEntity(episodeIndex: Int, progress: Long): HistoryEntity {
    return HistoryEntity(
        vodId = vodId,
        vodName = vodName,
        vodPic = vodPic,
        vodYear = vodYear,
        vodArea = vodArea,
        vodScore = vodScore,
        typeName = typeName,
        vodRemarks = vodRemarks,
        vodActor = vodActor,
        vodDirector = vodDirector,
        vodContent = vodContent,
        episodeIndex = episodeIndex,
        progress = progress
    )
}

fun HistoryEntity.toVodItem(): VodItem {
    return VodItem(
        vodId = vodId,
        vodName = vodName,
        vodPic = vodPic,
        vodYear = vodYear,
        vodArea = vodArea,
        vodScore = vodScore,
        typeName = typeName,
        vodRemarks = vodRemarks,
        vodActor = vodActor,
        vodDirector = vodDirector,
        vodContent = vodContent
    )
}
