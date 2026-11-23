package com.example.data.repository

import com.example.BuildConfig
import com.example.data.model.AppUpdateInfo
import com.example.data.remote.RtdbApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class AppUpdateRepository {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val rtdb: RtdbApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(RtdbApiService.BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(RtdbApiService::class.java)
    }

    suspend fun checkForAvailableUpdate(): AppUpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val raw = rtdb.getAppUpdateConfig().string().trim()
            if (raw.isBlank() || raw == "null" || !raw.startsWith("{")) return@withContext null

            val json = JSONObject(raw)
            val remoteCode = when {
                json.has("latestVersionCode") -> json.optInt("latestVersionCode", 0)
                json.has("versionCode") -> json.optInt("versionCode", 0)
                else -> 0
            }

            if (remoteCode <= BuildConfig.VERSION_CODE) return@withContext null

            val versionName = json.optString("latestVersionName")
                .ifBlank { json.optString("versionName") }
                .ifBlank { remoteCode.toString() }

            val titleBn = json.optString("titleBn")
                .ifBlank { json.optString("title") }
                .ifBlank { "নতুন আপডেট পাওয়া গেছে!" }

            val messageBn = json.optString("messageBn")
                .ifBlank { json.optString("changelog") }
                .ifBlank { json.optString("message") }
                .ifBlank { "উন্নত ফিচার, নামাজের অ্যালার্ম ও আরও ভালো পারফরম্যান্সের জন্য অ্যাপটি আপডেট করুন।" }

            val defaultStoreUrl = "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
            val updateUrl = json.optString("updateUrl")
                .ifBlank { json.optString("playStoreUrl") }
                .ifBlank { json.optString("apkUrl") }
                .ifBlank { defaultStoreUrl }

            val forceUpdate = json.optBoolean(
                "forceUpdate",
                json.optBoolean("isForceUpdate", false)
            )

            AppUpdateInfo(
                latestVersionCode = remoteCode,
                latestVersionName = versionName,
                titleBn = titleBn,
                messageBn = messageBn,
                updateUrl = updateUrl,
                forceUpdate = forceUpdate
            )
        } catch (_: Exception) {
            null
        }
    }
}
