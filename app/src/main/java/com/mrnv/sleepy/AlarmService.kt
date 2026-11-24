package com.mrnv.sleepy

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat

class AlarmService : Service() {
    private var player: MediaPlayer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val id = intent?.getIntExtra("id", -1) ?: -1
        val alarm = AlarmStore.load(this).firstOrNull { it.id == id }
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel("alarm", "Будильники", NotificationManager.IMPORTANCE_HIGH)
        )
        val open = PendingIntent.getActivity(
            this, id + 20000, Intent(this, AlarmActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        startForeground(42, NotificationCompat.Builder(this, "alarm")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Sleepy")
            .setContentText("Время вставать")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setFullScreenIntent(open, true).setOngoing(true).build())
        play(alarm)
        return START_NOT_STICKY
    }

    private fun play(alarm: AlarmModel?) {
        player?.release()
        player = MediaPlayer().apply {
            setOnPreparedListener { it.start() }
            setOnErrorListener { _, _, _ -> stopSelf(); true }
            try {
                when {
                    alarm?.soundType == "radio" && alarm.soundUri.isNotBlank() -> setDataSource(alarm.soundUri)
                    alarm?.soundType == "file" && alarm.soundUri.isNotBlank() -> setDataSource(this@AlarmService, Uri.parse(alarm.soundUri))
                    else -> setDataSource(this@AlarmService, Settings.System.DEFAULT_ALARM_ALERT_URI)
                }
                prepareAsync()
            } catch (_: Exception) { stopSelf() }
        }
    }

    override fun onDestroy() { player?.release(); player = null; super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
