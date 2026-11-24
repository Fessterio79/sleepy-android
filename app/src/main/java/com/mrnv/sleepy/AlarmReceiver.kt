package com.mrnv.sleepy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", -1)
        ContextCompat.startForegroundService(context, Intent(context, AlarmService::class.java).putExtra("id", id))
        AlarmStore.load(context).firstOrNull { it.id == id }?.let {
            if (it.repeatDays.isNotEmpty()) AlarmScheduler.schedule(context, it)
        }
    }
}
