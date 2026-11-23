package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ummah_daily_prefs")

data class TasbihPrefs(
    val count: Int = 0,
    val totalCount: Long = 0L,
    val targetCount: Int = 33,
    val selectedDhikrId: String = "general_tasbih"
)

data class LocalTasbihUser(
    val myId: String = "",
    val name: String = "",
    val phone: String = "",
    val area: String = "",
    val district: String = "",
    val location: String = ""
) {
    val resolvedLocation: String
        get() = location.ifBlank {
            listOf(area, district).filter { it.isNotBlank() }.joinToString(", ")
        }

    val isRegistered: Boolean
        get() = myId.isNotBlank() && name.isNotBlank() && phone.isNotBlank() && resolvedLocation.isNotBlank()
}

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val SELECTED_CITY = stringPreferencesKey("selected_city")
        val ENABLED_ALARMS = stringSetPreferencesKey("enabled_alarms")
        val TASBIH_COUNT = intPreferencesKey("tasbih_count")
        val TASBIH_TOTAL = longPreferencesKey("tasbih_total")
        val TASBIH_TARGET = intPreferencesKey("tasbih_target")
        val TASBIH_DHIKR_ID = stringPreferencesKey("tasbih_dhikr_id")

        // Exact Onboarding Keys matching spec
        val TASBIH_USER_ID = stringPreferencesKey("ummah_user_id")
        val TASBIH_USER_NAME = stringPreferencesKey("ummah_user_name")
        val TASBIH_USER_PHONE = stringPreferencesKey("ummah_user_phone")
        val TASBIH_USER_AREA = stringPreferencesKey("ummah_user_area")
        val TASBIH_USER_DISTRICT = stringPreferencesKey("ummah_user_district")
        val TASBIH_USER_LOCATION = stringPreferencesKey("ummah_user_location")

        // Caches
        val TASBIH_COUNTS_MAP_JSON = stringPreferencesKey("tasbih_counts_map_json")
        val TASBIH_CUSTOM_DHIKRS_JSON = stringPreferencesKey("tasbih_custom_dhikrs_json")
        val TASBIH_AMALNAMA_CACHE_JSON = stringPreferencesKey("tasbih_amalnama_cache_json")
        val TASBIH_PRAYERS_CACHE_JSON = stringPreferencesKey("tasbih_prayers_cache_json")

        fun cachedScheduleKey(city: String) = stringPreferencesKey("cached_schedule_${city.lowercase()}")
    }

    val selectedCityFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.SELECTED_CITY] ?: "Dhaka"
    }

    val enabledAlarmsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.ENABLED_ALARMS] ?: emptySet()
    }

    val tasbihPrefsFlow: Flow<TasbihPrefs> = context.dataStore.data.map { prefs ->
        TasbihPrefs(
            count = prefs[Keys.TASBIH_COUNT] ?: 0,
            totalCount = prefs[Keys.TASBIH_TOTAL] ?: 0L,
            targetCount = prefs[Keys.TASBIH_TARGET] ?: 100,
            selectedDhikrId = prefs[Keys.TASBIH_DHIKR_ID] ?: "general_tasbih"
        )
    }

    val localTasbihUserFlow: Flow<LocalTasbihUser> = context.dataStore.data.map { prefs ->
        val area = prefs[Keys.TASBIH_USER_AREA] ?: ""
        val district = prefs[Keys.TASBIH_USER_DISTRICT] ?: ""
        val loc = prefs[Keys.TASBIH_USER_LOCATION]
            ?: listOf(area, district).filter { it.isNotBlank() }.joinToString(", ")
        LocalTasbihUser(
            myId = prefs[Keys.TASBIH_USER_ID] ?: "",
            name = prefs[Keys.TASBIH_USER_NAME] ?: "",
            phone = prefs[Keys.TASBIH_USER_PHONE] ?: "",
            area = area,
            district = district,
            location = loc
        )
    }

    val customDhikrsJsonFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.TASBIH_CUSTOM_DHIKRS_JSON] ?: "[]"
    }

    val dhikrCountsJsonFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.TASBIH_COUNTS_MAP_JSON] ?: "{}"
    }

    suspend fun setSelectedCity(city: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED_CITY] = city
        }
    }

    suspend fun togglePrayerAlarm(prayerId: String): Boolean {
        var enabledNow = false
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.ENABLED_ALARMS]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(prayerId)) {
                current.remove(prayerId)
                enabledNow = false
            } else {
                current.add(prayerId)
                enabledNow = true
            }
            prefs[Keys.ENABLED_ALARMS] = current
        }
        return enabledNow
    }

    suspend fun saveCachedScheduleJson(city: String, json: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.cachedScheduleKey(city)] = json
        }
    }

    suspend fun getCachedScheduleJson(city: String): String? {
        return context.dataStore.data.first()[Keys.cachedScheduleKey(city)]
    }

    suspend fun saveTasbihState(
        count: Int,
        totalCount: Long,
        targetCount: Int,
        selectedDhikrId: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TASBIH_COUNT] = count
            prefs[Keys.TASBIH_TOTAL] = totalCount
            prefs[Keys.TASBIH_TARGET] = targetCount
            prefs[Keys.TASBIH_DHIKR_ID] = selectedDhikrId
        }
    }

    suspend fun saveDhikrCountsMapJson(json: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TASBIH_COUNTS_MAP_JSON] = json
        }
    }

    suspend fun getDhikrCountsMapJson(): String {
        return context.dataStore.data.first()[Keys.TASBIH_COUNTS_MAP_JSON] ?: "{}"
    }

    suspend fun saveTasbihUser(user: LocalTasbihUser) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TASBIH_USER_ID] = user.myId
            prefs[Keys.TASBIH_USER_NAME] = user.name
            prefs[Keys.TASBIH_USER_PHONE] = user.phone
            prefs[Keys.TASBIH_USER_AREA] = user.area
            prefs[Keys.TASBIH_USER_DISTRICT] = user.district
            prefs[Keys.TASBIH_USER_LOCATION] = user.resolvedLocation
        }
    }

    suspend fun clearTasbihUser() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.TASBIH_USER_ID)
            prefs.remove(Keys.TASBIH_USER_NAME)
            prefs.remove(Keys.TASBIH_USER_PHONE)
            prefs.remove(Keys.TASBIH_USER_AREA)
            prefs.remove(Keys.TASBIH_USER_DISTRICT)
            prefs.remove(Keys.TASBIH_USER_LOCATION)
        }
    }

    suspend fun saveCustomDhikrsJson(json: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TASBIH_CUSTOM_DHIKRS_JSON] = json
        }
    }

    suspend fun saveAmalnamaCacheJson(json: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TASBIH_AMALNAMA_CACHE_JSON] = json
        }
    }

    suspend fun getAmalnamaCacheJson(): String? {
        return context.dataStore.data.first()[Keys.TASBIH_AMALNAMA_CACHE_JSON]
    }

    suspend fun savePrayersCacheJson(json: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TASBIH_PRAYERS_CACHE_JSON] = json
        }
    }

    suspend fun getPrayersCacheJson(): String? {
        return context.dataStore.data.first()[Keys.TASBIH_PRAYERS_CACHE_JSON]
    }
}
