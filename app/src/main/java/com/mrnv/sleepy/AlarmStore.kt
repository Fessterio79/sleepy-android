package com.mrnv.sleepy
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
object AlarmStore {
 private const val PREFS="sleepy"; private const val KEY="alarms"; private const val SLEEP="sleep_sessions"
 fun load(context:Context):List<AlarmModel>{
  val arr=JSONArray(context.getSharedPreferences(PREFS,0).getString(KEY,"[]")?:"[]")
  return buildList { for(i in 0 until arr.length()){ val o=arr.getJSONObject(i); val days=buildSet{val a=o.optJSONArray("days")?:JSONArray();for(j in 0 until a.length())add(a.getInt(j))}; add(AlarmModel(o.getInt("id"),o.getInt("hour"),o.getInt("minute"),o.optBoolean("enabled",true),days,o.optString("uri"),o.optString("type","default"),o.optString("label","Будильник"))) } }
 }
 fun save(context:Context,alarms:List<AlarmModel>){ val arr=JSONArray(); alarms.forEach{a->arr.put(JSONObject().apply{put("id",a.id);put("hour",a.hour);put("minute",a.minute);put("enabled",a.enabled);put("uri",a.soundUri);put("type",a.soundType);put("label",a.label);put("days",JSONArray(a.repeatDays.toList()))})}; context.getSharedPreferences(PREFS,0).edit().putString(KEY,arr.toString()).apply() }
 fun addSleep(context:Context,bedtime:Long,wake:Long){val p=context.getSharedPreferences(PREFS,0);val arr=JSONArray(p.getString(SLEEP,"[]")?:"[]");arr.put(JSONObject().apply{put("bed",bedtime);put("wake",wake)});while(arr.length()>60)arr.remove(0);p.edit().putString(SLEEP,arr.toString()).apply()}
 fun sleepSessions(context:Context):List<Pair<Long,Long>>{val arr=JSONArray(context.getSharedPreferences(PREFS,0).getString(SLEEP,"[]")?:"[]");return buildList{for(i in 0 until arr.length()){val o=arr.getJSONObject(i);add(o.getLong("bed") to o.getLong("wake"))}}}
 fun bedtime(context:Context)=context.getSharedPreferences(PREFS,0).getLong("active_bedtime",0L)
 fun setBedtime(context:Context,value:Long){context.getSharedPreferences(PREFS,0).edit().putLong("active_bedtime",value).apply()}
 fun setting(context:Context,key:String,fallback:Boolean)=context.getSharedPreferences(PREFS,0).getBoolean(key,fallback)
 fun setSetting(context:Context,key:String,value:Boolean){context.getSharedPreferences(PREFS,0).edit().putBoolean(key,value).apply()}
 fun snoozeMinutes(context:Context)=context.getSharedPreferences(PREFS,0).getInt("snooze_minutes",10)
 fun setSnoozeMinutes(context:Context,value:Int){context.getSharedPreferences(PREFS,0).edit().putInt("snooze_minutes",value).apply()}
 fun rampSeconds(context:Context)=context.getSharedPreferences(PREFS,0).getInt("ramp_seconds",30)
 fun setRampSeconds(context:Context,value:Int){context.getSharedPreferences(PREFS,0).edit().putInt("ramp_seconds",value).apply()}
}