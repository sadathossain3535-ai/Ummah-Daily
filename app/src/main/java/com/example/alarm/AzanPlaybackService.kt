package com.example.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AzanPlaybackService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val audioPlayer = AzanAudioPlayer()
    private var playbackJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_AZAN) {
            stopPlaybackAndService()
            return START_NOT_STICKY
        }

        val prayerId = intent?.getStringExtra(EXTRA_PRAYER_ID).orEmpty()
        val prayerNameBn = intent?.getStringExtra(EXTRA_PRAYER_NAME_BN)
            ?.takeIf { it.isNotBlank() } ?: "নামাজের"

        ensureNotificationChannel()
        val notification = buildAzanNotification(prayerId, prayerNameBn)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        playbackJob?.cancel()
        audioPlayer.stop()
        playbackJob = serviceScope.launch {
            audioPlayer.playAzanChimeSequence()
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun stopPlaybackAndService() {
        playbackJob?.cancel()
        audioPlayer.stop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "নামাজের আযান ও অ্যালার্ম",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "প্রতি ওয়াক্ত নামাজের সময় আযান ও রিমাইন্ডার"
                    enableVibration(true)
                    setSound(null, null) // AzanAudioPlayer handles USAGE_ALARM audio playback directly
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    private fun buildAzanNotification(prayerId: String, prayerNameBn: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            prayerId.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AzanPlaybackService::class.java).apply {
            action = ACTION_STOP_AZAN
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            9001,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("$prayerNameBn ওয়াক্তের আযান")
            .setContentText("এখন $prayerNameBn নামাজের সময় হয়েছে। নামাজের প্রস্তুতি নিন।")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                "আযান বন্ধ করুন",
                stopPendingIntent
            )
            .build()
    }

    override fun onDestroy() {
        playbackJob?.cancel()
        audioPlayer.stop()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "azan_alarm_channel_v1"
        const val NOTIFICATION_ID = 4401
        const val ACTION_STOP_AZAN = "com.example.alarm.ACTION_STOP_AZAN"
        const val EXTRA_PRAYER_ID = "extra_prayer_id"
        const val EXTRA_PRAYER_NAME_BN = "extra_prayer_name_bn"
    }
}
