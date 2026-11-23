package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AladhanResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: AladhanData
)

@JsonClass(generateAdapter = true)
data class AladhanData(
    @Json(name = "timings") val timings: AladhanTimings,
    @Json(name = "date") val date: AladhanDateInfo
)

@JsonClass(generateAdapter = true)
data class AladhanTimings(
    @Json(name = "Fajr") val fajr: String,
    @Json(name = "Sunrise") val sunrise: String,
    @Json(name = "Dhuhr") val dhuhr: String,
    @Json(name = "Asr") val asr: String,
    @Json(name = "Sunset") val sunset: String,
    @Json(name = "Maghrib") val maghrib: String,
    @Json(name = "Isha") val isha: String,
    @Json(name = "Imsak") val imsak: String,
    @Json(name = "Midnight") val midnight: String
)

@JsonClass(generateAdapter = true)
data class AladhanDateInfo(
    @Json(name = "readable") val readable: String,
    @Json(name = "hijri") val hijri: HijriDateDto,
    @Json(name = "gregorian") val gregorian: GregorianDateDto
)

@JsonClass(generateAdapter = true)
data class HijriDateDto(
    @Json(name = "date") val date: String,
    @Json(name = "day") val day: String,
    @Json(name = "month") val month: HijriMonthDto,
    @Json(name = "year") val year: String
)

@JsonClass(generateAdapter = true)
data class HijriMonthDto(
    @Json(name = "number") val number: Int,
    @Json(name = "en") val en: String,
    @Json(name = "ar") val ar: String? = null
)

@JsonClass(generateAdapter = true)
data class GregorianDateDto(
    @Json(name = "date") val date: String,
    @Json(name = "day") val day: String,
    @Json(name = "month") val month: GregorianMonthDto,
    @Json(name = "year") val year: String,
    @Json(name = "weekday") val weekday: WeekdayDto
)

@JsonClass(generateAdapter = true)
data class GregorianMonthDto(
    @Json(name = "number") val number: Int,
    @Json(name = "en") val en: String
)

@JsonClass(generateAdapter = true)
data class WeekdayDto(
    @Json(name = "en") val en: String
)
