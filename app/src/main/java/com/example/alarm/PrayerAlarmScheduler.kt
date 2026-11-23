package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.FardPrayerDetailsCatalog
import com.example.data.model.FardPrayerItem
import com.example.data.repository.PrayerRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar

object PrayerAlarmScheduler {

    private val requestCodeByPrayerId = mapOf(
        "fajr" to 1001,
        "dhuhr" to 1002,
        "asr" to 1003,
        "maghrib" to 1004,
        "isha" to 1005
    )

    fun canScheduleExactAlarms(context: Context): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun syncPrayerAlarms(
        context: Context,
        prayers: List<FardPrayerItem>,
        enabledIds: Set<String>
    ) {
        prayers.forEach { prayer ->
            if (enabledIds.contains(prayer.id)) {
                schedulePrayerAlarm(
                    context = context,
                    prayerId = prayer.id,
                    prayerNameBn = prayer.nameBn,
                    startMinuteOfDay = prayer.startMinuteOfDay
                )
            } else {
                cancelPrayerAlarm(context, prayer.id)
            }
        }
        updateBootReceiverState(context, enabledIds.isNotEmpty())
    }

    fun schedulePrayerAlarm(
        context: Context,
        prayerId: String,
        prayerNameBn: String,
        startMinuteOfDay: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildAlarmPendingIntent(context, prayerId, prayerNameBn, startMinuteOfDay)
        val triggerAtMillis = computeNextTriggerMillis(startMinuteOfDay)

        if (canScheduleExactAlarms(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else {
            // Graceful fallback when exact alarm permission is denied on Android 14+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }
    }

    fun cancelPrayerAlarm(context: Context, prayerId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCode = requestCodeByPrayerId[prayerId] ?: prayerId.hashCode()
        val intent = Intent(context, AzanAlarmReceiver::class.java).apply {
            action = AzanAlarmReceiver.ACTION_PRAYER_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    suspend fun rescheduleAllEnabledAlarms(context: Context) {
        val prefs = UserPreferencesRepository(context)
        val enabledIds = prefs.enabledAlarmsFlow.first()
        updateBootReceiverState(context, enabledIds.isNotEmpty())
        if (enabledIds.isEmpty()) return

        val city = prefs.selectedCityFlow.first()
        val repo = PrayerRepository(prefs)
        val cached = repo.getCachedResponse(city)
        if (cached != null) {
            val schedule = repo.buildScheduleState(cached, city, isFromCache = true)
            syncPrayerAlarms(context, schedule.fardPrayers, enabledIds)
        }
    }

    fun fallbackNameBn(prayerId: String): String =
        FardPrayerDetailsCatalog.byId[prayerId]?.nameBn ?: "নামাজের"

    private fun buildAlarmPendingIntent(
        context: Context,
        prayerId: String,
        prayerNameBn: String,
        startMinuteOfDay: Int
    ): PendingIntent {
        val requestCode = requestCodeByPrayerId[prayerId] ?: prayerId.hashCode()
        val intent = Intent(context, AzanAlarmReceiver::class.java).apply {
            action = AzanAlarmReceiver.ACTION_PRAYER_ALARM
            putExtra(AzanAlarmReceiver.EXTRA_PRAYER_ID, prayerId)
            putExtra(AzanAlarmReceiver.EXTRA_PRAYER_NAME_BN, prayerNameBn)
            putExtra(AzanAlarmReceiver.EXTRA_START_MINUTE, startMinuteOfDay)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun computeNextTriggerMillis(startMinuteOfDay: Int): Long {
        val norm = ((startMinuteOfDay % 1440) + 1440) % 1440
        val hour = norm / 60
        val minute = norm % 60
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.timeInMillis <= now.timeInMillis + 1500L) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }

    private fun updateBootReceiverState(context: Context, enabled: Boolean) {
        val receiver = ComponentName(context, AzanBootReceiver::class.java)
        val newState = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        runCatching {
            context.packageManager.setComponentEnabledSetting(
                receiver,
                newState,
                PackageManager.DONT_KILL_APP
            )
        }
    }
}
