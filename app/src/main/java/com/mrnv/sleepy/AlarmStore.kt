package com.mrnv.sleepy

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object AlarmStore {
    private const val PREFS = "sleepy"
    private const val KEY = "alarms"
    private const val SLEEP = "sleep_sessions"

    fun load(context: Context): List<AlarmModel> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]") ?: "[]"
        val arr = JSONArray(raw)
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val days = buildSet { val a = o.optJSONArray("days") ?: JSONArray(); for (j in 0 until a.length()) add(a.getInt(j)) }
                add(AlarmModel(o.getInt("id"), o.getInt("hour"), o.getInt("minute"), o.getBoolean("enabled"), days, o.optString("uri"), o.optString("type", "default"), o.optString("label", "Будильник")))
            }
        }
    }

    fun save(context: Context, alarms: List<AlarmModel>) {
        val arr = JSONArray()
        alarms.forEach { a ->
            val o = JSONObject().apply {
                put("id", a.id); put("hour", a.hour); put("minute", a.minute); put("enabled", a.enabled)
                put("uri", a.soundUri); put("type", a.soundType); put("label", a.label)
                put("days", JSONArray(a.repeatDays.toList()))
            }
            arr.put(o)
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, arr.toString()).apply()
    }

    fun addSleep(context: Context, bedtime: Long, wake: Long) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = JSONArray(p.getString(SLEEP, "[]") ?: "[]")
        arr.put(JSONObject().apply { put("bed", bedtime); put("wake", wake) })
        while (arr.length() > 30) arr.remove(0)
        p.edit().putString(SLEEP, arr.toString()).apply()
    }

    fun sleepSessions(context: Context): List<Pair<Long, Long>> {
        val arr = JSONArray(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(SLEEP, "[]") ?: "[]")
        return buildList { for (i in 0 until arr.length()) { val o = arr.getJSONObject(i); add(o.getLong("bed") to o.getLong("wake")) } }
    }
}
