package com.mrnv.sleepy
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
class AlarmActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);val id=intent.getIntExtra("id",-1);val snooze=AlarmStore.snoozeMinutes(this);setContent{AlarmScreen(snooze,{AlarmScheduler.scheduleSnooze(this,id,snooze);stopService(Intent(this,AlarmService::class.java));finish()},{stopService(Intent(this,AlarmService::class.java));finish()})}}
}
@Composable private fun AlarmScreen(minutes:Int,snooze:()->Unit,stop:()->Unit){
 Surface(color=Color(0xFF090910),modifier=Modifier.fillMaxSize()){Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center,modifier=Modifier.fillMaxSize().padding(28.dp)){
 Text("S L E E P Y",color=Color(0xFF9B8AFF),fontSize=13.sp);Spacer(Modifier.height(32.dp));Text("☀",fontSize=76.sp);Spacer(Modifier.height(20.dp));Text("Пора просыпаться",color=Color.White,fontSize=30.sp);Text("Пусть день будет хорошим",color=Color(0xFF9998A8),fontSize=15.sp);Spacer(Modifier.height(52.dp))
 Button(onClick=stop,modifier=Modifier.fillMaxWidth().height(60.dp)){Text("Выключить будильник")}
 Spacer(Modifier.height(12.dp));OutlinedButton(onClick=snooze,modifier=Modifier.fillMaxWidth().height(56.dp)){Text("Отложить на $minutes мин")}
 }}
}