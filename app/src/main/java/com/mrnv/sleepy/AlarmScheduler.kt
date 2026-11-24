package com.mrnv.sleepy

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object AlarmScheduler {
    private fun pending(context: Context, id: Int) = PendingIntent.getBroadcast(
        context, id, Intent(context, AlarmReceiver::class.java).putExtra("id", id),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    fun cancel(context: Context, id: Int) {
        context.getSystemService(AlarmManager::class.java).cancel(pending(context, id))
    }

    fun schedule(context: Context, alarm: AlarmModel) {
        cancel(context, alarm.id)
        if (!alarm.enabled) return
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, alarm.hour); set(Calendar.MINUTE, alarm.minute)
        }
        if (alarm.repeatDays.isEmpty()) {
            if (next.timeInMillis <= now.timeInMillis) next.add(Calendar.DAY_OF_YEAR, 1)
        } else {
            var found = false
            for (offset in 0..7) {
                val candidate = Calendar.getInstance().apply {
                    timeInMillis = next.timeInMillis
                    add(Calendar.DAY_OF_YEAR, offset)
                }
                val day = when (candidate.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> 1; Calendar.TUESDAY -> 2; Calendar.WEDNESDAY -> 3
                    Calendar.THURSDAY -> 4; Calendar.FRIDAY -> 5; Calendar.SATURDAY -> 6
                    else -> 7
                }
                if (day in alarm.repeatDays && candidate.timeInMillis > now.timeInMillis) {
                    next.timeInMillis = candidate.timeInMillis; found = true; break
                }
            }
            if (!found) return
        }
        val manager = context.getSystemService(AlarmManager::class.java)
        if (manager.canScheduleExactAlarms()) {
            val show = PendingIntent.getActivity(
                context, alarm.id + 10000, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            manager.setAlarmClock(AlarmManager.AlarmClockInfo(next.timeInMillis, show), pending(context, alarm.id))
        }
    }

    fun rescheduleAll(context: Context) = AlarmStore.load(context).forEach { schedule(context, it) }
}
