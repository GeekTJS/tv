package com.familytv.app.data.model

import com.google.gson.annotations.SerializedName

data class VodResponse(
    @SerializedName("code") val code: Int = 0,
    @SerializedName("list") val list: List<VodItem> = emptyList(),
    @SerializedName("page") val page: Int = 1,
    @SerializedName("pagecount") val pageCount: Int = 1,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("limit") val limit: Int = 20
)

data class VodItem(
    @SerializedName("vod_id") val vodId: Long = 0L,
    @SerializedName("vod_name") val vodName: String = "",
    @SerializedName("vod_pic") val vodPic: String = "",
    @SerializedName("vod_year") val vodYear: String = "",
    @SerializedName("vod_area") val vodArea: String = "",
    @SerializedName("vod_remarks") val vodRemarks: String = "",
    @SerializedName("vod_actor") val vodActor: String = "",
    @SerializedName("vod_director") val vodDirector: String = "",
    @SerializedName("vod_content") val vodContent: String = "",
    @SerializedName("type_name") val typeName: String = "",
    @SerializedName("vod_score") val vodScore: String = "0.0",
    @SerializedName("vod_play_from") val vodPlayFrom: String = "",
    @SerializedName("vod_play_url") val vodPlayUrl: String = ""
)

data class Category(
    val id: Long,
    val name: String
)

data class PlayEpisode(
    val title: String,
    val url: String
)
