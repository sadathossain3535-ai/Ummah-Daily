package com.example.data.remote

import com.example.data.model.RtdbUserMeta
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path

interface RtdbApiService {

    companion object {
        const val BASE_URL = "https://ummah-daily-tasbeeh-default-rtdb.asia-southeast1.firebasedatabase.app/"
    }

    // Phone Index Recovery: phone_index/${cleanPhone}
    @GET("phone_index/{cleanPhone}.json")
    suspend fun getUserIdByPhone(@Path("cleanPhone") cleanPhone: String): ResponseBody

    @PUT("phone_index/{cleanPhone}.json")
    suspend fun mapPhoneToUserId(
        @Path("cleanPhone") cleanPhone: String,
        @Body userIdJson: RequestBody
    ): ResponseBody

    // User Meta: users_meta/${myId}
    @GET("users_meta/{myId}.json")
    suspend fun getUserMeta(@Path("myId") myId: String): RtdbUserMeta?

    @PUT("users_meta/{myId}.json")
    suspend fun putUserMeta(
        @Path("myId") myId: String,
        @Body meta: RtdbUserMeta
    ): RtdbUserMeta?

    // Per-Dhikr User Data: users_data/${myId} & users_data/${myId}/${tasbeehId}
    @GET("users_data/{myId}.json")
    suspend fun getUserCountsRaw(@Path("myId") myId: String): ResponseBody

    @PUT("users_data/{myId}/{tasbeehId}.json")
    suspend fun putDhikrCount(
        @Path("myId") myId: String,
        @Path("tasbeehId") tasbeehId: String,
        @Body countBody: RequestBody
    ): ResponseBody

    // Amalnama: amalnama/${myId}/${YYYY-MM-DD}/${tasbeehId}
    @GET("amalnama/{myId}.json")
    suspend fun getAllAmalnamaRaw(@Path("myId") myId: String): ResponseBody

    @PUT("amalnama/{myId}/{date}/{tasbeehId}.json")
    suspend fun putAmalnamaDhikrCount(
        @Path("myId") myId: String,
        @Path("date") date: String,
        @Path("tasbeehId") tasbeehId: String,
        @Body countBody: RequestBody
    ): ResponseBody

    // Prayers & Roza: prayers_data/${myId}/${date}
    @GET("prayers_data/{myId}.json")
    suspend fun getAllPrayersDataRaw(@Path("myId") myId: String): ResponseBody

    @GET("prayers_data/{myId}/{date}.json")
    suspend fun getPrayersForDateRaw(
        @Path("myId") myId: String,
        @Path("date") date: String
    ): ResponseBody

    @PUT("prayers_data/{myId}/{date}.json")
    suspend fun putPrayersForDate(
        @Path("myId") myId: String,
        @Path("date") date: String,
        @Body checklistMap: Map<String, @JvmSuppressWildcards Any>
    ): ResponseBody

    // Global Leaderboard: global_leaderboard/${myId}
    @GET("global_leaderboard.json")
    suspend fun getGlobalLeaderboardRaw(): ResponseBody

    @PATCH("global_leaderboard/{myId}.json")
    suspend fun patchLeaderboardEntry(
        @Path("myId") myId: String,
        @Body entry: Map<String, @JvmSuppressWildcards Any>
    ): ResponseBody

    // Automatic In-App Update Metadata: app_update.json
    @GET("app_update.json")
    suspend fun getAppUpdateConfig(): ResponseBody
}
