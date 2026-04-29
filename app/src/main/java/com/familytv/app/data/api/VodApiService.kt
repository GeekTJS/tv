package com.familytv.app.data.api

import com.familytv.app.data.model.VodResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface VodApiService {
    @GET("api.php/provide/vod/at/json/")
    suspend fun getVideoList(
        @Query("ac") action: String = "videolist",
        @Query("pg") page: Int = 1,
        @Query("t") typeId: Long? = null
    ): VodResponse

    @GET("api.php/provide/vod/at/json/")
    suspend fun getVideoDetail(
        @Query("ac") action: String = "detail",
        @Query("ids") id: Long
    ): VodResponse

    @GET("api.php/provide/vod/at/json/")
    suspend fun searchVideo(
        @Query("ac") action: String = "videolist",
        @Query("wd") keyword: String,
        @Query("pg") page: Int = 1
    ): VodResponse

    @GET("api.php/provide/vod/at/json/")
    suspend fun getCategoryList(
        @Query("ac") action: String = "list"
    ): VodResponse
}
