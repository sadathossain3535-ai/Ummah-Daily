package com.example.data.remote

import com.example.config.AppConfigRegistry
import com.example.data.model.AladhanResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AladhanApiService {

    @GET("v1/timingsByCity")
    suspend fun getTimingsByCity(
        @Query("city") city: String,
        @Query("country") country: String = AppConfigRegistry.ALADHAN_COUNTRY,
        @Query("method") method: Int = AppConfigRegistry.ALADHAN_METHOD
    ): AladhanResponse
}
