package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AzanAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_PRAYER_ALARM) return

        val prayerId = intent.getStringExtra(EXTRA_PRAYER_ID).orEmpty()
        val prayerNameBn = intent.getStringExtra(EXTRA_PRAYER_NAME_BN)
            ?.takeIf { it.isNotBlank() } ?: PrayerAlarmScheduler.fallbackNameBn(prayerId)
        val startMinute = intent.getIntExtra(EXTRA_START_MINUTE, -1)

        val serviceIntent = Intent(context, AzanPlaybackService::class.java).apply {
            putExtra(AzanPlaybackService.EXTRA_PRAYER_ID, prayerId)
            putExtra(AzanPlaybackService.EXTRA_PRAYER_NAME_BN, prayerNameBn)
        }
        runCatching {
            ContextCompat.startForegroundService(context, serviceIntent)
        }

        // Schedule next day's alarm cleanly in the background
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (prayerId.isNotBlank() && startMinute >= 0) {
                    PrayerAlarmScheduler.schedulePrayerAlarm(
                        context = context,
                        prayerId = prayerId,
                        prayerNameBn = prayerNameBn,
                        startMinuteOfDay = startMinute
                    )
                }
                PrayerAlarmScheduler.rescheduleAllEnabledAlarms(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_PRAYER_ALARM = "com.example.alarm.ACTION_PRAYER_ALARM"
        const val EXTRA_PRAYER_ID = "extra_prayer_id"
        const val EXTRA_PRAYER_NAME_BN = "extra_prayer_name_bn"
        const val EXTRA_START_MINUTE = "extra_start_minute"
    }
}
