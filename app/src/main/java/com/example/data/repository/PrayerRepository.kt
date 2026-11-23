package com.example.data.repository

import com.example.config.AppConfigRegistry
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.ActiveStatusSnapshot
import com.example.data.model.AladhanResponse
import com.example.data.model.FardPrayerDetailsCatalog
import com.example.data.model.FardPrayerItem
import com.example.data.model.ForbiddenTimeItem
import com.example.data.model.NaflPrayerItem
import com.example.data.model.PrayerScheduleState
import com.example.data.model.PrayerWindowCategory
import com.example.data.remote.AladhanApiService
import com.example.util.BengaliFormatter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.Calendar
import java.util.concurrent.TimeUnit

class PrayerRepository(
    private val prefsRepo: UserPreferencesRepository
) {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val responseAdapter = moshi.adapter(AladhanResponse::class.java)

    private val apiService: AladhanApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(AppConfigRegistry.ALADHAN_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AladhanApiService::class.java)
    }

    suspend fun getCachedResponse(city: String): AladhanResponse? {
        val json = prefsRepo.getCachedScheduleJson(city) ?: return null
        return runCatching { responseAdapter.fromJson(json) }.getOrNull()
    }

    suspend fun fetchPrayerTimings(city: String): Result<AladhanResponse> {
        return try {
            val response = apiService.getTimingsByCity(city = city)
            val json = responseAdapter.toJson(response)
            prefsRepo.saveCachedScheduleJson(city, json)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun buildScheduleState(
        response: AladhanResponse,
        cityApiName: String,
        isFromCache: Boolean
    ): PrayerScheduleState {
        val timings = response.data.timings
        val cityObj = AppConfigRegistry.bangladeshCities.find { it.apiCity == cityApiName }
            ?: AppConfigRegistry.bangladeshCities.first()

        val fajrMin = parseMinutes(timings.fajr)
        val sunriseMin = parseMinutes(timings.sunrise)
        val dhuhrMin = parseMinutes(timings.dhuhr)
        val asrMin = parseMinutes(timings.asr)
        val sunsetMin = parseMinutes(timings.sunset)
        val maghribMin = parseMinutes(timings.maghrib)
        val ishaMin = parseMinutes(timings.isha)
        val imsakMin = parseMinutes(timings.imsak).takeIf { it > 0 } ?: fajrMin

        val nowCal = Calendar.getInstance()
        val nowHour = nowCal.get(Calendar.HOUR_OF_DAY)
        val nowMinute = nowCal.get(Calendar.MINUTE)
        val nowSecond = nowCal.get(Calendar.SECOND)
        val nowTotalSeconds = (nowHour * 3600L) + (nowMinute * 60L) + nowSecond
        val nowMinutes = nowHour * 60 + nowMinute

        // Forbidden windows (exact spec: Sunrise 15m, Zawal 10m before Dhuhr, Sunset 15m before Sunset)
        val forbSunriseStart = sunriseMin
        val forbSunriseEnd = sunriseMin + 15
        val forbZawalStart = (dhuhrMin - 10).coerceAtLeast(0)
        val forbZawalEnd = dhuhrMin
        val forbSunsetStart = (sunsetMin - 15).coerceAtLeast(0)
        val forbSunsetEnd = sunsetMin

        val isForbSunrise = isInRange(nowTotalSeconds, forbSunriseStart, forbSunriseEnd)
        val isForbZawal = isInRange(nowTotalSeconds, forbZawalStart, forbZawalEnd)
        val isForbSunset = isInRange(nowTotalSeconds, forbSunsetStart, forbSunsetEnd)

        val forbiddenTimes = listOf(
            ForbiddenTimeItem(
                id = "forb_sunrise",
                titleBn = "সূর্যোদয়ের সময়",
                reasonBn = "সূর্যোদয়ের পর ১৫ মিনিট",
                startMinuteOfDay = forbSunriseStart,
                endMinuteOfDay = forbSunriseEnd,
                windowTextBn = "${formatMinShort(forbSunriseStart)} - ${formatMinShort(forbSunriseEnd)}",
                isActive = isForbSunrise
            ),
            ForbiddenTimeItem(
                id = "forb_zawal",
                titleBn = "দ্বিপ্রহর (জাওয়াল)",
                reasonBn = "যোহরের পূর্বের ১০ মিনিট",
                startMinuteOfDay = forbZawalStart,
                endMinuteOfDay = forbZawalEnd,
                windowTextBn = "${formatMinShort(forbZawalStart)} - ${formatMinShort(forbZawalEnd)}",
                isActive = isForbZawal
            ),
            ForbiddenTimeItem(
                id = "forb_sunset",
                titleBn = "সূর্যাস্তের সময়",
                reasonBn = "সূর্যাস্তের পূর্বের ১৫ মিনিট",
                startMinuteOfDay = forbSunsetStart,
                endMinuteOfDay = forbSunsetEnd,
                windowTextBn = "${formatMinShort(forbSunsetStart)} - ${formatMinShort(forbSunsetEnd)}",
                isActive = isForbSunset
            )
        )

        // Nafl windows (exact spec: Ishraq +15..+90, Chasht +90..Dhuhr-10, Awabeen Maghrib+15..Isha, Tahajjud Isha+60..Fajr)
        val ishraqStart = sunriseMin + 15
        val ishraqEnd = sunriseMin + 90
        val chashtStart = sunriseMin + 90
        val chashtEnd = (dhuhrMin - 10).coerceAtLeast(chashtStart + 1)
        val awabinStart = maghribMin + 15
        val awabinEnd = ishaMin
        val tahajjudStart = (ishaMin + 60) % 1440
        val tahajjudEnd = fajrMin

        val isIshraqActive = isInRange(nowTotalSeconds, ishraqStart, ishraqEnd)
        val isChashtActive = isInRange(nowTotalSeconds, chashtStart, chashtEnd)
        val isAwabinActive = isInRange(nowTotalSeconds, awabinStart, awabinEnd)
        val isTahajjudActive = isInWrapRange(nowTotalSeconds, tahajjudStart, tahajjudEnd)

        val naflPrayers = listOf(
            NaflPrayerItem(
                id = "nafl_ishraq",
                nameBn = "ইশরাক",
                subtitleBn = "সূর্যোদয়ের ১৫ মিনিট পর থেকে ৯০ মিনিট",
                startMinuteOfDay = ishraqStart,
                endMinuteOfDay = ishraqEnd,
                windowTextBn = "${formatMinShort(ishraqStart)} - ${formatMinShort(ishraqEnd)}",
                rakatInfoBn = "২ থেকে ৪ রাকাত",
                isActive = isIshraqActive
            ),
            NaflPrayerItem(
                id = "nafl_chasht",
                nameBn = "চাশত (দুহা)",
                subtitleBn = "ইশরাকের পর থেকে যোহরের ১০ মিনিট আগ পর্যন্ত",
                startMinuteOfDay = chashtStart,
                endMinuteOfDay = chashtEnd,
                windowTextBn = "${formatMinShort(chashtStart)} - ${formatMinShort(chashtEnd)}",
                rakatInfoBn = "২ থেকে ১২ রাকাত",
                isActive = isChashtActive
            ),
            NaflPrayerItem(
                id = "nafl_awabin",
                nameBn = "আওয়াবীন",
                subtitleBn = "মাগরিবের ১৫ মিনিট পর থেকে ইশা পর্যন্ত",
                startMinuteOfDay = awabinStart,
                endMinuteOfDay = awabinEnd,
                windowTextBn = "${formatMinShort(awabinStart)} - ${formatMinShort(awabinEnd)}",
                rakatInfoBn = "৬ রাকাত",
                isActive = isAwabinActive
            ),
            NaflPrayerItem(
                id = "nafl_tahajjud",
                nameBn = "তাহাজ্জুদ",
                subtitleBn = "ইশার ১ ঘণ্টা পর থেকে ফজর পর্যন্ত",
                startMinuteOfDay = tahajjudStart,
                endMinuteOfDay = tahajjudEnd,
                windowTextBn = "${formatMinShort(tahajjudStart)} - ${formatMinShort(tahajjudEnd)}",
                rakatInfoBn = "২ থেকে ৮+ রাকাত",
                isActive = isTahajjudActive
            )
        )

        // Determine active & next Fard prayer
        val isFajrActive = isInRange(nowTotalSeconds, fajrMin, sunriseMin)
        val isDhuhrActive = isInRange(nowTotalSeconds, dhuhrMin, asrMin)
        val isAsrActive = isInRange(nowTotalSeconds, asrMin, maghribMin)
        val isMaghribActive = isInRange(nowTotalSeconds, maghribMin, ishaMin)
        val isIshaActive = isInWrapRange(nowTotalSeconds, ishaMin, fajrMin)

        val nextFardId = when {
            nowMinutes < fajrMin -> "fajr"
            nowMinutes < dhuhrMin -> "dhuhr"
            nowMinutes < asrMin -> "asr"
            nowMinutes < maghribMin -> "maghrib"
            nowMinutes < ishaMin -> "isha"
            else -> "fajr"
        }

        val fardSpecs = listOf(
            Triple("fajr", fajrMin, sunriseMin),
            Triple("dhuhr", dhuhrMin, asrMin),
            Triple("asr", asrMin, maghribMin),
            Triple("maghrib", maghribMin, ishaMin),
            Triple("isha", ishaMin, fajrMin)
        )

        val fardPrayers = fardSpecs.map { (id, startM, endM) ->
            val meta = FardPrayerDetailsCatalog.byId.getValue(id)
            val active = when (id) {
                "fajr" -> isFajrActive
                "dhuhr" -> isDhuhrActive
                "asr" -> isAsrActive
                "maghrib" -> isMaghribActive
                "isha" -> isIshaActive
                else -> false
            }
            FardPrayerItem(
                id = id,
                nameBn = meta.nameBn,
                nameAr = meta.nameAr,
                startMinuteOfDay = startM,
                endMinuteOfDay = endM,
                startTimeBn = formatMin12h(startM),
                endTimeBn = formatMin12h(endM),
                rakatSummaryBn = meta.rakatSummaryBn,
                rakatBreakdown = meta.rakatBreakdown,
                virtueBn = meta.virtueBn,
                amalBn = meta.amalBn,
                isCurrentActive = active,
                isNextUpcoming = !active && nextFardId == id
            )
        }

        // Compute Ring Status Snapshot (Remaining Percentage 0f..1f)
        val status = computeActiveStatus(
            nowTotalSeconds = nowTotalSeconds,
            fajrMin = fajrMin,
            dhuhrMin = dhuhrMin,
            asrMin = asrMin,
            maghribMin = maghribMin,
            ishaMin = ishaMin,
            imsakMin = imsakMin,
            forbiddenTimes = forbiddenTimes,
            naflPrayers = naflPrayers,
            fardPrayers = fardPrayers
        )

        val greg = response.data.date.gregorian
        val hijri = response.data.date.hijri
        val gregDateBn = BengaliFormatter.formatGregorianDateBn(
            day = greg.day.toIntOrNull() ?: nowCal.get(Calendar.DAY_OF_MONTH),
            monthNumber = greg.month.number,
            year = greg.year.toIntOrNull() ?: nowCal.get(Calendar.YEAR),
            weekdayEn = greg.weekday.en
        )
        val hijriDateBn = BengaliFormatter.formatHijriDateBn(
            day = hijri.day,
            monthNumber = hijri.month.number,
            year = hijri.year
        )
        val dayNameBn = BengaliFormatter.weekdayNameBn(greg.weekday.en)
            .ifBlank { BengaliFormatter.weekdayNameBn(nowCal) }

        return PrayerScheduleState(
            cityApiName = cityApiName,
            cityNameBn = cityObj.nameBn,
            dayNameBn = dayNameBn,
            hijriDateBn = hijriDateBn,
            gregorianDateBn = gregDateBn,
            sunriseBn = formatMin12h(sunriseMin),
            sunsetBn = formatMin12h(sunsetMin),
            tahajjudTimeBn = formatMin12h(tahajjudStart),
            fardPrayers = fardPrayers,
            naflPrayers = naflPrayers,
            forbiddenTimes = forbiddenTimes,
            status = status,
            isFromCache = isFromCache,
            lastUpdatedLabelBn = if (isFromCache) "অফলাইন" else "লাইভ"
        )
    }

    private fun computeActiveStatus(
        nowTotalSeconds: Long,
        fajrMin: Int,
        dhuhrMin: Int,
        asrMin: Int,
        maghribMin: Int,
        ishaMin: Int,
        imsakMin: Int,
        forbiddenTimes: List<ForbiddenTimeItem>,
        naflPrayers: List<NaflPrayerItem>,
        fardPrayers: List<FardPrayerItem>
    ): ActiveStatusSnapshot {
        val activeForbidden = forbiddenTimes.firstOrNull { it.isActive }
        val activeFard = fardPrayers.firstOrNull { it.isCurrentActive }
        val activeNafl = naflPrayers.firstOrNull { it.isActive }

        val (category, badge, title, nextLabel, countdown, remainingFrac) = when {
            activeForbidden != null -> {
                val startSec = activeForbidden.startMinuteOfDay * 60L
                val endSec = activeForbidden.endMinuteOfDay * 60L
                val total = (endSec - startSec).coerceAtLeast(1L)
                val remaining = (endSec - nowTotalSeconds).coerceAtLeast(0L)
                val frac = (remaining.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                StatusTuple(
                    category = PrayerWindowCategory.FORBIDDEN,
                    badge = "নিষিদ্ধ সময়",
                    title = activeForbidden.titleBn,
                    nextLabel = "নিষিদ্ধ সময় শেষ হতে বাকি",
                    countdown = remaining,
                    progress = frac
                )
            }
            activeFard != null -> {
                val startSec = activeFard.startMinuteOfDay * 60L
                val endSec = if (activeFard.endMinuteOfDay > activeFard.startMinuteOfDay) {
                    activeFard.endMinuteOfDay * 60L
                } else {
                    (activeFard.endMinuteOfDay + 24 * 60) * 60L
                }
                val adjustedNow = if (nowTotalSeconds < startSec) nowTotalSeconds + 24 * 3600L else nowTotalSeconds
                val total = (endSec - startSec).coerceAtLeast(1L)
                val remaining = (endSec - adjustedNow).coerceAtLeast(0L)
                val frac = (remaining.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                StatusTuple(
                    category = PrayerWindowCategory.FARD,
                    badge = "চলমান ওয়াক্ত",
                    title = activeFard.nameBn,
                    nextLabel = "ওয়াক্ত শেষ হতে বাকি",
                    countdown = remaining,
                    progress = frac
                )
            }
            activeNafl != null -> {
                val startSec = activeNafl.startMinuteOfDay * 60L
                val endSec = activeNafl.endMinuteOfDay * 60L
                val total = (endSec - startSec).coerceAtLeast(1L)
                val remaining = (endSec - nowTotalSeconds).coerceAtLeast(0L)
                val frac = (remaining.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                StatusTuple(
                    category = PrayerWindowCategory.NAFL,
                    badge = "চলমান ওয়াক্ত",
                    title = activeNafl.nameBn,
                    nextLabel = "ওয়াক্ত শেষ হতে বাকি",
                    countdown = remaining,
                    progress = frac
                )
            }
            else -> {
                val targetMin = when {
                    nowTotalSeconds < fajrMin * 60L -> fajrMin
                    nowTotalSeconds < dhuhrMin * 60L -> dhuhrMin
                    nowTotalSeconds < asrMin * 60L -> asrMin
                    nowTotalSeconds < maghribMin * 60L -> maghribMin
                    nowTotalSeconds < ishaMin * 60L -> ishaMin
                    else -> fajrMin + 24 * 60
                }
                val remaining = (targetMin * 60L - nowTotalSeconds).coerceAtLeast(0L)
                StatusTuple(
                    category = PrayerWindowCategory.NAFL,
                    badge = "চলমান ওয়াক্ত",
                    title = "প্রস্তুতির সময়",
                    nextLabel = "ওয়াক্ত শেষ হতে বাকি",
                    countdown = remaining,
                    progress = 0.5f
                )
            }
        }

        // Sehri & Iftar live countdowns with next-occurrence date
        val imsakSec = imsakMin * 60L
        val maghribSec = maghribMin * 60L
        val isBeforeSehri = nowTotalSeconds < imsakSec
        val sehriCountdown = if (isBeforeSehri) {
            imsakSec - nowTotalSeconds
        } else {
            (imsakSec + 24 * 3600L) - nowTotalSeconds
        }

        val isBeforeIftar = nowTotalSeconds < maghribSec
        val iftarCountdown = if (isBeforeIftar) {
            maghribSec - nowTotalSeconds
        } else {
            (maghribSec + 24 * 3600L) - nowTotalSeconds
        }

        return ActiveStatusSnapshot(
            category = category,
            statusBadgeBn = badge,
            currentTitleBn = title,
            nextLabelBn = nextLabel,
            countdownSeconds = countdown,
            progressFraction = remainingFrac,
            sehriEndTimeBn = formatMin12h(imsakMin),
            iftarTimeBn = formatMin12h(maghribMin),
            sehriNextDateBn = BengaliFormatter.formatShortOccurrenceDateBn(if (isBeforeSehri) 0 else 1),
            iftarNextDateBn = BengaliFormatter.formatShortOccurrenceDateBn(if (isBeforeIftar) 0 else 1),
            sehriCountdownSeconds = sehriCountdown,
            iftarCountdownSeconds = iftarCountdown,
            isBeforeSehri = isBeforeSehri,
            isBeforeIftar = isBeforeIftar
        )
    }

    private data class StatusTuple(
        val category: PrayerWindowCategory,
        val badge: String,
        val title: String,
        val nextLabel: String,
        val countdown: Long,
        val progress: Float
    )

    private fun parseMinutes(rawTime: String): Int {
        val clean = rawTime.substringBefore(" ").trim()
        val parts = clean.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return h * 60 + m
    }

    private fun isInRange(nowSec: Long, startMin: Int, endMin: Int): Boolean {
        return nowSec >= startMin * 60L && nowSec < endMin * 60L
    }

    private fun isInWrapRange(nowSec: Long, startMin: Int, endMin: Int): Boolean {
        val startSec = startMin * 60L
        val endSec = endMin * 60L
        return if (startSec <= endSec) {
            nowSec >= startSec && nowSec < endSec
        } else {
            nowSec >= startSec || nowSec < endSec
        }
    }

    private fun formatMin12h(totalMin: Int): String {
        val norm = ((totalMin % 1440) + 1440) % 1440
        return BengaliFormatter.format12HourBengali(norm / 60, norm % 60)
    }

    private fun formatMinShort(totalMin: Int): String {
        val norm = ((totalMin % 1440) + 1440) % 1440
        return BengaliFormatter.formatShort12HourBengali(norm / 60, norm % 60)
    }
}
