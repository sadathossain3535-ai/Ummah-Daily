package com.example.data.repository

import com.example.data.local.LocalTasbihUser
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.AmalnamaSummary
import com.example.data.model.DailyPrayerChecklist
import com.example.data.model.PrayerChecklistKeys
import com.example.data.model.RankedUserRow
import com.example.data.model.RtdbUserMeta
import com.example.data.model.TasbihDhikrCatalog
import com.example.data.model.TasbihDhikrItem
import com.example.data.model.WaqtType
import com.example.data.model.detectWaqtByHour
import com.example.data.remote.RtdbApiService
import com.example.util.BengaliFormatter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

sealed interface OnboardingOutcome {
    data class RecoveredExisting(val user: LocalTasbihUser, val totalCount: Long) : OnboardingOutcome
    data class CreatedNew(val user: LocalTasbihUser, val syncedToCloud: Boolean) : OnboardingOutcome
}

class TasbihRepository(
    private val prefsRepo: UserPreferencesRepository
) {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val customDhikrsType = Types.newParameterizedType(List::class.java, TasbihDhikrItem::class.java)
    private val customDhikrsAdapter = moshi.adapter<List<TasbihDhikrItem>>(customDhikrsType)

    private val countsMapType = Types.newParameterizedType(
        Map::class.java,
        String::class.java,
        Int::class.javaObjectType
    )
    private val countsMapAdapter = moshi.adapter<Map<String, Int>>(countsMapType)

    private val amalnamaMapType = Types.newParameterizedType(
        Map::class.java,
        String::class.java,
        Types.newParameterizedType(Map::class.java, String::class.java, Long::class.javaObjectType)
    )
    private val amalnamaCacheAdapter = moshi.adapter<Map<String, Map<String, Long>>>(amalnamaMapType)

    private val rawPrayersMapType = Types.newParameterizedType(
        Map::class.java,
        String::class.java,
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    )
    private val rawPrayersCacheAdapter = moshi.adapter<Map<String, Map<String, Any>>>(rawPrayersMapType)

    private val genericMapAdapter = moshi.adapter<Map<String, Any>>(
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    )

    private val rtdb: RtdbApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(RtdbApiService.BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(RtdbApiService::class.java)
    }

    fun todayDateKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun offsetDateKey(baseDate: String, deltaDays: Int): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val parsed = runCatching { sdf.parse(baseDate) }.getOrNull() ?: Date()
        val cal = Calendar.getInstance().apply {
            time = parsed
            add(Calendar.DAY_OF_YEAR, deltaDays)
        }
        return sdf.format(cal.time)
    }

    fun formatDateBn(dateKey: String): String {
        val parts = dateKey.split("-")
        if (parts.size != 3) return BengaliFormatter.toBengaliDigits(dateKey)
        val y = parts[0].toIntOrNull() ?: return dateKey
        val m = parts[1].toIntOrNull() ?: return dateKey
        val d = parts[2].toIntOrNull() ?: return dateKey
        return BengaliFormatter.formatGregorianDateBn(d, m, y, null)
    }

    fun cleanPhone(raw: String): String = raw.replace(Regex("[^0-9]"), "")

    fun detectCurrentWaqt(): WaqtType {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return detectWaqtByHour(hour)
    }

    suspend fun loadLocalCountsMap(): Map<String, Int> {
        val json = prefsRepo.getDhikrCountsMapJson()
        return runCatching { countsMapAdapter.fromJson(json) }.getOrNull() ?: emptyMap()
    }

    suspend fun saveLocalCountsMap(counts: Map<String, Int>) {
        prefsRepo.saveDhikrCountsMapJson(countsMapAdapter.toJson(counts))
    }

    suspend fun registerOrRecoverUser(
        name: String,
        rawPhone: String,
        area: String,
        district: String
    ): Result<OnboardingOutcome> = withContext(Dispatchers.IO) {
        val cleanedPhone = cleanPhone(rawPhone)
        if (cleanedPhone.length < 10) {
            return@withContext Result.failure(IllegalArgumentException("সঠিক মোবাইল নম্বর দিন (কমপক্ষে ১০ সংখ্যা)"))
        }

        val trimmedName = name.trim()
        val trimmedArea = area.trim()
        val trimmedDistrict = district.trim()
        val combinedLocation = listOf(trimmedArea, trimmedDistrict)
            .filter { it.isNotBlank() }
            .joinToString(", ")

        try {
            var existingId = parsePhoneIndexId(rtdb.getUserIdByPhone(cleanedPhone).string().trim())
            if (existingId.isNullOrBlank() && cleanedPhone.startsWith("880") && cleanedPhone.length >= 13) {
                val localBd = "0" + cleanedPhone.substring(3)
                existingId = parsePhoneIndexId(rtdb.getUserIdByPhone(localBd).string().trim())
            }

            if (!existingId.isNullOrBlank()) {
                val meta = runCatching { rtdb.getUserMeta(existingId) }.getOrNull()
                val rawCountsJson = runCatching { rtdb.getUserCountsRaw(existingId).string() }.getOrNull()
                val recoveredCounts = parseUserCountsMap(rawCountsJson)

                val resolvedArea = meta?.area?.takeIf { it.isNotBlank() } ?: trimmedArea
                val resolvedDistrict = meta?.district?.takeIf { it.isNotBlank() } ?: trimmedDistrict
                val resolvedLocation = meta?.location?.takeIf { it.isNotBlank() }
                    ?: listOf(resolvedArea, resolvedDistrict).filter { it.isNotBlank() }.joinToString(", ")
                    ?: combinedLocation

                val recoveredUser = LocalTasbihUser(
                    myId = existingId,
                    name = meta?.name?.takeIf { it.isNotBlank() } ?: trimmedName,
                    phone = cleanedPhone,
                    area = resolvedArea,
                    district = resolvedDistrict,
                    location = resolvedLocation
                )
                prefsRepo.saveTasbihUser(recoveredUser)
                saveLocalCountsMap(recoveredCounts)

                // Warm up amalnama & prayers caches from cloud
                val amalRaw = runCatching { rtdb.getAllAmalnamaRaw(existingId).string() }.getOrNull()
                val amalMap = parseAllAmalnamaMap(amalRaw)
                if (amalMap.isNotEmpty()) {
                    prefsRepo.saveAmalnamaCacheJson(amalnamaCacheAdapter.toJson(amalMap))
                }
                val prayersRaw = runCatching { rtdb.getAllPrayersDataRaw(existingId).string() }.getOrNull()
                val prayersMap = parseAllPrayersRawMap(prayersRaw)
                if (prayersMap.isNotEmpty()) {
                    prefsRepo.savePrayersCacheJson(rawPrayersCacheAdapter.toJson(prayersMap))
                }

                // Sum lifetime total from leaderboard or amalnama
                val lifetimeTotal = maxOf(
                    amalMap.values.sumOf { dayMap ->
                        dayMap.filterKeys { it != "total" }.values.sum()
                    },
                    recoveredCounts.values.sumOf { it.toLong() }
                )

                val selectedId = recoveredCounts.keys.firstOrNull() ?: "general_tasbih"
                val currentCount = recoveredCounts[selectedId] ?: 0
                prefsRepo.saveTasbihState(
                    count = currentCount,
                    totalCount = lifetimeTotal,
                    targetCount = 100,
                    selectedDhikrId = selectedId
                )

                // Ensure metadata exists on users_meta
                runCatching {
                    rtdb.putUserMeta(
                        existingId,
                        RtdbUserMeta(
                            myId = existingId,
                            name = recoveredUser.name,
                            phone = recoveredUser.phone,
                            area = recoveredUser.area,
                            district = recoveredUser.district,
                            location = recoveredUser.resolvedLocation,
                            createdAt = meta?.createdAt ?: System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }

                return@withContext Result.success(
                    OnboardingOutcome.RecoveredExisting(recoveredUser, lifetimeTotal)
                )
            } else {
                val newId = generateUsrId()
                val newUser = LocalTasbihUser(
                    myId = newId,
                    name = trimmedName,
                    phone = cleanedPhone,
                    area = trimmedArea,
                    district = trimmedDistrict,
                    location = combinedLocation
                )
                val quotedIdBody = "\"$newId\"".toRequestBody("application/json; charset=utf-8".toMediaType())
                rtdb.mapPhoneToUserId(cleanedPhone, quotedIdBody)

                val now = System.currentTimeMillis()
                rtdb.putUserMeta(
                    newId,
                    RtdbUserMeta(
                        myId = newId,
                        name = trimmedName,
                        phone = cleanedPhone,
                        area = trimmedArea,
                        district = trimmedDistrict,
                        location = combinedLocation,
                        createdAt = now,
                        updatedAt = now
                    )
                )
                rtdb.patchLeaderboardEntry(
                    newId,
                    mapOf(
                        "myId" to newId,
                        "name" to trimmedName,
                        "location" to combinedLocation,
                        "area" to trimmedArea,
                        "district" to trimmedDistrict,
                        "totalCount" to 0L,
                        "updatedAt" to now
                    )
                )

                prefsRepo.saveTasbihUser(newUser)
                saveLocalCountsMap(emptyMap())
                prefsRepo.saveTasbihState(0, 0L, 100, "general_tasbih")
                return@withContext Result.success(
                    OnboardingOutcome.CreatedNew(newUser, syncedToCloud = true)
                )
            }
        } catch (_: Exception) {
            val fallbackId = generateUsrId()
            val localUser = LocalTasbihUser(
                myId = fallbackId,
                name = trimmedName,
                phone = cleanedPhone,
                area = trimmedArea,
                district = trimmedDistrict,
                location = combinedLocation
            )
            prefsRepo.saveTasbihUser(localUser)
            return@withContext Result.success(
                OnboardingOutcome.CreatedNew(localUser, syncedToCloud = false)
            )
        }
    }

    private fun generateUsrId(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        val rand = (1..5).map { chars.random() }.joinToString("")
        return "usr_${System.currentTimeMillis()}_$rand"
    }

    private fun parsePhoneIndexId(raw: String): String? {
        if (raw.isBlank() || raw == "null") return null
        return if (raw.startsWith("\"") && raw.endsWith("\"") && raw.length >= 2) {
            raw.substring(1, raw.length - 1).takeIf { it.isNotBlank() && it != "null" }
        } else if (raw.startsWith("{")) {
            val map = runCatching { genericMapAdapter.fromJson(raw) }.getOrNull()
            (map?.get("myId") as? String) ?: (map?.get("userId") as? String)
        } else {
            raw.takeIf { it.isNotBlank() && it != "null" }
        }
    }

    private fun parseUserCountsMap(raw: String?): Map<String, Int> {
        if (raw.isNullOrBlank() || raw == "null") return emptyMap()
        return runCatching {
            val obj = JSONObject(raw)
            val result = linkedMapOf<String, Int>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val v = obj.opt(key)
                if (v is Number) {
                    result[key] = v.toInt()
                }
            }
            result
        }.getOrNull() ?: emptyMap()
    }

    private fun parseAllAmalnamaMap(raw: String?): Map<String, Map<String, Long>> {
        if (raw.isNullOrBlank() || raw == "null") return emptyMap()
        return runCatching {
            val root = JSONObject(raw)
            val result = linkedMapOf<String, Map<String, Long>>()
            val dateKeys = root.keys()
            while (dateKeys.hasNext()) {
                val dateKey = dateKeys.next()
                val dayObj = root.optJSONObject(dateKey) ?: continue
                val dayMap = linkedMapOf<String, Long>()
                val dKeys = dayObj.keys()
                while (dKeys.hasNext()) {
                    val dhikrKey = dKeys.next()
                    val v = dayObj.opt(dhikrKey)
                    if (v is Number) {
                        dayMap[dhikrKey] = v.toLong()
                    }
                }
                result[dateKey] = dayMap
            }
            result
        }.getOrNull() ?: emptyMap()
    }

    private fun parseAllPrayersRawMap(raw: String?): Map<String, Map<String, Any>> {
        if (raw.isNullOrBlank() || raw == "null") return emptyMap()
        return runCatching {
            val root = JSONObject(raw)
            val result = linkedMapOf<String, Map<String, Any>>()
            val dateKeys = root.keys()
            while (dateKeys.hasNext()) {
                val dateKey = dateKeys.next()
                val dayObj = root.optJSONObject(dateKey) ?: continue
                result[dateKey] = jsonObjectToMap(dayObj)
            }
            result
        }.getOrNull() ?: emptyMap()
    }

    private fun jsonObjectToMap(obj: JSONObject): Map<String, Any> {
        val map = linkedMapOf<String, Any>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            val v = obj.opt(k)
            if (v != null && v != JSONObject.NULL) {
                map[k] = v
            }
        }
        return map
    }

    suspend fun loadCustomDhikrs(json: String): List<TasbihDhikrItem> {
        return runCatching { customDhikrsAdapter.fromJson(json) }.getOrNull() ?: emptyList()
    }

    suspend fun saveCustomDhikrs(list: List<TasbihDhikrItem>) {
        prefsRepo.saveCustomDhikrsJson(customDhikrsAdapter.toJson(list))
    }

    suspend fun recordTasbihDelta(
        user: LocalTasbihUser,
        tasbeehId: String,
        delta: Int,
        newCurrentCount: Int,
        newLifetimeTotal: Long
    ): Boolean = withContext(Dispatchers.IO) {
        val today = todayDateKey()
        val now = System.currentTimeMillis()

        // 1. Update local per-dhikr counts map
        val countsMap = loadLocalCountsMap().toMutableMap()
        countsMap[tasbeehId] = newCurrentCount
        saveLocalCountsMap(countsMap)

        // 2. Update local Amalnama cache immediately
        val cachedAmal = getCachedAmalnamaMap().toMutableMap()
        val todayMap = (cachedAmal[today] ?: emptyMap()).toMutableMap()
        val prevTodayDhikr = todayMap[tasbeehId] ?: 0L
        val updatedTodayDhikr = prevTodayDhikr + delta.coerceAtLeast(0)
        if (delta > 0) {
            todayMap[tasbeehId] = updatedTodayDhikr
            cachedAmal[today] = todayMap
            prefsRepo.saveAmalnamaCacheJson(amalnamaCacheAdapter.toJson(cachedAmal))
        }

        if (!user.isRegistered) return@withContext false

        // 3. Sync to exact Firebase RTDB nodes
        try {
            val countBody = newCurrentCount.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            rtdb.putDhikrCount(user.myId, tasbeehId, countBody)

            if (delta > 0) {
                val amalBody = updatedTodayDhikr.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                rtdb.putAmalnamaDhikrCount(user.myId, today, tasbeehId, amalBody)

                rtdb.patchLeaderboardEntry(
                    user.myId,
                    mapOf(
                        "myId" to user.myId,
                        "name" to user.name,
                        "location" to user.resolvedLocation,
                        "area" to user.area,
                        "district" to user.district,
                        "totalCount" to newLifetimeTotal,
                        "updatedAt" to now
                    )
                )
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun resetDhikrCloudCount(
        user: LocalTasbihUser,
        tasbeehId: String
    ): Boolean = withContext(Dispatchers.IO) {
        val countsMap = loadLocalCountsMap().toMutableMap()
        countsMap[tasbeehId] = 0
        saveLocalCountsMap(countsMap)

        if (!user.isRegistered) return@withContext false
        try {
            val zeroBody = "0".toRequestBody("application/json; charset=utf-8".toMediaType())
            rtdb.putDhikrCount(user.myId, tasbeehId, zeroBody)
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getPrayersChecklist(myId: String, date: String): DailyPrayerChecklist =
        withContext(Dispatchers.IO) {
            val localMap = getCachedPrayersMap().toMutableMap()
            val cloudRaw = if (myId.isNotBlank()) {
                runCatching { rtdb.getPrayersForDateRaw(myId, date).string() }.getOrNull()
            } else {
                null
            }
            val parsedCloud = if (!cloudRaw.isNullOrBlank() && cloudRaw != "null") {
                runCatching { jsonObjectToMap(JSONObject(cloudRaw)) }.getOrNull()
            } else {
                null
            }

            if (parsedCloud != null) {
                localMap[date] = parsedCloud
                prefsRepo.savePrayersCacheJson(rawPrayersCacheAdapter.toJson(localMap))
                DailyPrayerChecklist.fromRawMap(date, parsedCloud)
            } else {
                DailyPrayerChecklist.fromRawMap(date, localMap[date])
            }
        }

    suspend fun savePrayersChecklist(
        myId: String,
        date: String,
        checklist: DailyPrayerChecklist
    ): Boolean = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val payload = linkedMapOf<String, Any>()
        PrayerChecklistKeys.allItemKeys.forEach { key ->
            payload[key] = checklist.isChecked(key)
        }
        payload["date"] = date
        payload["updatedAt"] = now

        val localMap = getCachedPrayersMap().toMutableMap()
        localMap[date] = payload
        prefsRepo.savePrayersCacheJson(rawPrayersCacheAdapter.toJson(localMap))

        if (myId.isBlank()) return@withContext false
        try {
            rtdb.putPrayersForDate(myId, date, payload)
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getAmalnamaSummary(
        myId: String,
        startDate: String,
        endDate: String,
        allDhikrs: List<TasbihDhikrItem>
    ): AmalnamaSummary = withContext(Dispatchers.IO) {
        val localAmal = getCachedAmalnamaMap().toMutableMap()
        if (myId.isNotBlank()) {
            val cloudRaw = runCatching { rtdb.getAllAmalnamaRaw(myId).string() }.getOrNull()
            val cloudAmal = parseAllAmalnamaMap(cloudRaw)
            if (cloudAmal.isNotEmpty()) {
                cloudAmal.forEach { (date, counts) -> localAmal[date] = counts }
                prefsRepo.saveAmalnamaCacheJson(amalnamaCacheAdapter.toJson(localAmal))
            }
        }

        val localPrayers = getCachedPrayersMap().toMutableMap()
        if (myId.isNotBlank()) {
            val cloudRaw = runCatching { rtdb.getAllPrayersDataRaw(myId).string() }.getOrNull()
            val cloudPrayers = parseAllPrayersRawMap(cloudRaw)
            if (cloudPrayers.isNotEmpty()) {
                cloudPrayers.forEach { (date, map) -> localPrayers[date] = map }
                prefsRepo.savePrayersCacheJson(rawPrayersCacheAdapter.toJson(localPrayers))
            }
        }

        val from = minOf(startDate, endDate)
        val to = maxOf(startDate, endDate)

        val dhikrNameById = allDhikrs.associate { it.id to it.titleBn }
        val dhikrTotals = linkedMapOf<String, Long>()
        var totalTasbih = 0L

        localAmal.forEach { (date, map) ->
            if (date in from..to) {
                var dayTotal = 0L
                map.forEach { (dhikrKey, count) ->
                    if (dhikrKey != "total" && count > 0L) {
                        dayTotal += count
                        val displayName = dhikrNameById[dhikrKey] ?: dhikrKey
                        dhikrTotals[displayName] = (dhikrTotals[displayName] ?: 0L) + count
                    }
                }
                totalTasbih += maxOf(dayTotal, map["total"] ?: 0L)
            }
        }

        var fajrDays = 0
        var dhuhrDays = 0
        var asrDays = 0
        var maghribDays = 0
        var ishaDays = 0
        var tahajjudDays = 0
        var rozaDays = 0

        localPrayers.forEach { (date, rawMap) ->
            if (date in from..to) {
                val cl = DailyPrayerChecklist.fromRawMap(date, rawMap)
                if (cl.isChecked(PrayerChecklistKeys.FAJR_FARZ)) fajrDays++
                if (cl.isChecked(PrayerChecklistKeys.DHUHR_FARZ)) dhuhrDays++
                if (cl.isChecked(PrayerChecklistKeys.ASR_FARZ)) asrDays++
                if (cl.isChecked(PrayerChecklistKeys.MAGHRIB_FARZ)) maghribDays++
                if (cl.isChecked(PrayerChecklistKeys.ISHA_FARZ)) ishaDays++
                if (cl.isChecked(PrayerChecklistKeys.NAFL_TAHAJJUD)) tahajjudDays++
                if (cl.hasAnyRoza()) rozaDays++
            }
        }

        val daysCount = countDaysInclusive(from, to)
        val rangeLabel = if (from == to) {
            formatDateBn(from)
        } else {
            "${formatDateBn(from)} — ${formatDateBn(to)}"
        }

        AmalnamaSummary(
            titleRangeBn = rangeLabel,
            startDate = from,
            endDate = to,
            totalTasbih = totalTasbih,
            dhikrBreakdown = dhikrTotals.entries
                .sortedByDescending { it.value }
                .map { it.key to it.value },
            fajrDays = fajrDays,
            dhuhrDays = dhuhrDays,
            asrDays = asrDays,
            maghribDays = maghribDays,
            ishaDays = ishaDays,
            tahajjudDays = tahajjudDays,
            totalRozaDays = rozaDays,
            daysCovered = daysCount
        )
    }

    suspend fun getLeaderboard(currentUser: LocalTasbihUser): Result<List<RankedUserRow>> =
        withContext(Dispatchers.IO) {
            try {
                val rawJson = rtdb.getGlobalLeaderboardRaw().string().trim()
                if (rawJson.isBlank() || rawJson == "null") {
                    return@withContext Result.success(emptyList())
                }
                val root = JSONObject(rawJson)
                val parsed = mutableListOf<RankedUserRow>()
                val keys = root.keys()
                while (keys.hasNext()) {
                    val entryKey = keys.next()
                    val obj = root.optJSONObject(entryKey) ?: continue
                    val myId = obj.optString("myId").ifBlank { entryKey }
                    val name = obj.optString("name").ifBlank { "মুসলিম বান্দা" }
                    val rawLocation = obj.optString("location")
                    val area = obj.optString("area")
                    val district = obj.optString("district")
                    val locDisplay = rawLocation.ifBlank {
                        listOf(area, district).filter { it.isNotBlank() }.joinToString(", ")
                    }.ifBlank { "বাংলাদেশ" }

                    val countVal = when {
                        obj.has("totalCount") -> obj.optLong("totalCount", 0L)
                        else -> obj.optLong("totalTasbih", 0L)
                    }

                    parsed.add(
                        RankedUserRow(
                            rank = 0,
                            myId = myId,
                            name = name,
                            locationDisplay = locDisplay,
                            totalCount = countVal,
                            isCurrentUser = myId == currentUser.myId
                        )
                    )
                }

                val sorted = parsed
                    .sortedByDescending { it.totalCount }
                    .mapIndexed { idx, row -> row.copy(rank = idx + 1) }

                Result.success(sorted)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private suspend fun getCachedAmalnamaMap(): Map<String, Map<String, Long>> {
        val json = prefsRepo.getAmalnamaCacheJson() ?: return emptyMap()
        return runCatching { amalnamaCacheAdapter.fromJson(json) }.getOrNull() ?: emptyMap()
    }

    private suspend fun getCachedPrayersMap(): Map<String, Map<String, Any>> {
        val json = prefsRepo.getPrayersCacheJson() ?: return emptyMap()
        return runCatching { rawPrayersCacheAdapter.fromJson(json) }.getOrNull() ?: emptyMap()
    }

    private fun countDaysInclusive(from: String, to: String): Int {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val d1 = runCatching { sdf.parse(from) }.getOrNull() ?: return 1
        val d2 = runCatching { sdf.parse(to) }.getOrNull() ?: return 1
        val diff = ((d2.time - d1.time) / (24L * 3600L * 1000L)).toInt() + 1
        return diff.coerceAtLeast(1)
    }
}
