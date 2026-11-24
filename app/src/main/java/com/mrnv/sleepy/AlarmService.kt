package com.mrnv.sleepy
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.Settings
import androidx.core.app.NotificationCompat
class AlarmService:Service(){
 private var player:MediaPlayer?=null
 private val handler=Handler(Looper.getMainLooper())
 private var vibrator:Vibrator?=null
 override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int{
  val id=intent?.getIntExtra("id",-1)?:-1;val alarm=AlarmStore.load(this).firstOrNull{it.id==id}
  val nm=getSystemService(NotificationManager::class.java);nm.createNotificationChannel(NotificationChannel("alarm","Будильники",NotificationManager.IMPORTANCE_HIGH))
  val open=PendingIntent.getActivity(this,id+20000,Intent(this,AlarmActivity::class.java).putExtra("id",id),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  startForeground(42,NotificationCompat.Builder(this,"alarm").setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("Sleepy").setContentText("Время вставать").setCategory(NotificationCompat.CATEGORY_ALARM).setPriority(NotificationCompat.PRIORITY_MAX).setFullScreenIntent(open,true).setOngoing(true).build())
  if(AlarmStore.setting(this,"vibration",true))vibrate()
  play(alarm);return START_NOT_STICKY
 }
 private fun vibrate(){
  vibrator=if(Build.VERSION.SDK_INT>=31)getSystemService(VibratorManager::class.java).defaultVibrator else @Suppress("DEPRECATION") getSystemService(Vibrator::class.java)
  val pattern=longArrayOf(0,350,250,350,250,600)
  if(Build.VERSION.SDK_INT>=26)vibrator?.vibrate(VibrationEffect.createWaveform(pattern,0)) else @Suppress("DEPRECATION") vibrator?.vibrate(pattern,0)
 }
 private fun play(alarm:AlarmModel?){
  player?.release()
  player=MediaPlayer().apply{
   setOnPreparedListener{mp->mp.start();ramp(mp)}
   setOnErrorListener{mp,_,_->fallback(mp);true}
   try{
    when{alarm?.soundType=="radio"&&alarm.soundUri.isNotBlank()->setDataSource(alarm.soundUri);alarm?.soundType=="file"&&alarm.soundUri.isNotBlank()->setDataSource(this@AlarmService,Uri.parse(alarm.soundUri));else->setDataSource(this@AlarmService,Settings.System.DEFAULT_ALARM_ALERT_URI)}
    prepareAsync()
   }catch(_:Exception){fallback(this)}
  }
 }
 private fun fallback(mp:MediaPlayer){
  try{mp.reset();mp.setOnErrorListener{_,_,_->stopSelf();true};mp.setOnPreparedListener{it.start();ramp(it)};mp.setDataSource(this,Settings.System.DEFAULT_ALARM_ALERT_URI);mp.prepareAsync()}catch(_:Exception){stopSelf()}
 }
 private fun ramp(mp:MediaPlayer){
  val seconds=AlarmStore.rampSeconds(this);if(seconds<=0){mp.setVolume(1f,1f);return}
  val start=System.currentTimeMillis();mp.setVolume(0.08f,0.08f)
  val task=object:Runnable{override fun run(){if(player!==mp||!mp.isPlaying)return;val p=((System.currentTimeMillis()-start).toFloat()/(seconds*1000f)).coerceIn(0f,1f);val v=0.08f+0.92f*p;mp.setVolume(v,v);if(p<1f)handler.postDelayed(this,500)}}
  handler.post(task)
 }
 override fun onDestroy(){handler.removeCallbacksAndMessages(null);vibrator?.cancel();player?.release();player=null;super.onDestroy()}
 override fun onBind(intent:Intent?):IBinder?=null
}