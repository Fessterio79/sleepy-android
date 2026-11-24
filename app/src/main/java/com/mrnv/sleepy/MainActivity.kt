package com.mrnv.sleepy
import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val Bg=Color(0xFF090910)
private val Card=Color(0xFF171722)
private val Soft=Color(0xFF222231)
private val Accent=Color(0xFF9B8AFF)
private val Muted=Color(0xFF9291A3)
private val DayNames=listOf("Пн","Вт","Ср","Чт","Пт","Сб","Вс")

class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);window.statusBarColor=android.graphics.Color.rgb(9,9,16);window.navigationBarColor=android.graphics.Color.rgb(9,9,16);setContent{MaterialTheme(colorScheme=darkColorScheme(primary=Accent,background=Bg,surface=Card)){SleepyApp()}}}
}

@Composable fun SleepyApp(){
 val context=LocalContext.current
 var tab by remember{mutableIntStateOf(0)}
 var alarms by remember{mutableStateOf(AlarmStore.load(context))}
 val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){}
 LaunchedEffect(Unit){if(android.os.Build.VERSION.SDK_INT>=33)permission.launch(Manifest.permission.POST_NOTIFICATIONS)}
 fun save(list:List<AlarmModel>){alarms=list;AlarmStore.save(context,list);list.forEach{AlarmScheduler.schedule(context,it)}}
 Surface(color=Bg,modifier=Modifier.fillMaxSize()){
  Column(Modifier.fillMaxSize()){
   Box(Modifier.weight(1f)){
    when(tab){
     0->HomeScreen(alarms,{a->save(alarms.map{if(it.id==a.id)it.copy(enabled=!it.enabled)else it})},{a->save(alarms+a)},{a->save(alarms.map{if(it.id==a.id)a else it})},{a->AlarmScheduler.cancel(context,a.id);save(alarms.filterNot{it.id==a.id})})
     1->SleepScreen(context)
     else->SettingsScreen(context)
    }
   }
   NavigationBar(containerColor=Color(0xFF11111A),contentColor=Muted){
    NavigationBarItem(selected=tab==0,onClick={tab=0},icon={Text("◷",fontSize=20.sp)},label={Text("Будильники")},colors=NavigationBarItemDefaults.colors(selectedIconColor=Accent,selectedTextColor=Accent,indicatorColor=Soft))
    NavigationBarItem(selected=tab==1,onClick={tab=1},icon={Text("☾",fontSize=20.sp)},label={Text("Сон")},colors=NavigationBarItemDefaults.colors(selectedIconColor=Accent,selectedTextColor=Accent,indicatorColor=Soft))
    NavigationBarItem(selected=tab==2,onClick={tab=2},icon={Text("⚙",fontSize=20.sp)},label={Text("Настройки")},colors=NavigationBarItemDefaults.colors(selectedIconColor=Accent,selectedTextColor=Accent,indicatorColor=Soft))
   }
  }
 }
}
@Composable private fun Header(title:String,subtitle:String){Column{Text("S L E E P Y",color=Accent,fontSize=11.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(10.dp));Text(title,color=Color.White,fontSize=30.sp,fontWeight=FontWeight.Bold);Text(subtitle,color=Muted,fontSize=14.sp)}}
@Composable private fun CardBox(content:@Composable ColumnScope.()->Unit){Surface(color=Card,shape=RoundedCornerShape(24.dp),modifier=Modifier.fillMaxWidth(),content=content)}

@Composable private fun HomeScreen(alarms:List<AlarmModel>,toggle:(AlarmModel)->Unit,add:(AlarmModel)->Unit,edit:(AlarmModel)->Unit,delete:(AlarmModel)->Unit){
 var editing by remember{mutableStateOf<AlarmModel?>(null)}
 var show by remember{mutableStateOf(false)}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=20.dp)){
  Spacer(Modifier.height(28.dp));Header("Будильники","Твой ритм начинается здесь");Spacer(Modifier.height(24.dp))
  Row(verticalAlignment=Alignment.CenterVertically,modifier=Modifier.fillMaxWidth()){
   Column(Modifier.weight(1f)){Text("МОИ БУДИЛЬНИКИ",color=Muted,fontSize=11.sp,fontWeight=FontWeight.Bold);Text(alarms.count{it.enabled}.toString()+" активных",color=Color.White,fontSize=14.sp)}
   TextButton(onClick={editing=null;show=true}){Text("＋ Добавить",color=Accent,fontWeight=FontWeight.SemiBold)}
  }
  Spacer(Modifier.height(10.dp))
  if(alarms.isEmpty()){CardBox{Column(Modifier.fillMaxWidth().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("☾",fontSize=38.sp);Spacer(Modifier.height(8.dp));Text("Пока тихо",color=Color.White,fontSize=18.sp,fontWeight=FontWeight.SemiBold);Text("Добавь первый будильник",color=Muted,fontSize=13.sp)}}}
  alarms.sortedWith(compareBy<AlarmModel>{it.hour}.thenBy{it.minute}).forEach{a->
   Spacer(Modifier.height(10.dp));CardBox{Column(Modifier.fillMaxWidth().padding(18.dp)){
    Row(verticalAlignment=Alignment.CenterVertically){
     Column(Modifier.weight(1f)){Text(String.format(Locale.getDefault(),"%02d:%02d",a.hour,a.minute),color=if(a.enabled)Color.White else Muted,fontSize=42.sp,fontWeight=FontWeight.SemiBold);Text(a.label,color=Muted,fontSize=13.sp)}
     Switch(checked=a.enabled,onCheckedChange={toggle(a)},colors=SwitchDefaults.colors(checkedThumbColor=Color.White,checkedTrackColor=Accent))
    }
    Spacer(Modifier.height(7.dp));Text(if(a.repeatDays.isEmpty())"Один раз" else a.repeatDays.sorted().joinToString("   "){DayNames[it-1]},color=Accent,fontSize=12.sp)
    Row(verticalAlignment=Alignment.CenterVertically){TextButton(onClick={editing=a;show=true}){Text("Изменить",color=Color.White)};TextButton(onClick={delete(a)}){Text("Удалить",color=Color(0xFFFF858D))};Spacer(Modifier.weight(1f));Text(when(a.soundType){"radio"->"Радио";"file"->"Своя музыка";else->"Обычный звук"},color=Muted,fontSize=11.sp)}
   }}
  }
  Spacer(Modifier.height(12.dp));CardBox{Column(Modifier.padding(18.dp)){Text("ПОДСКАЗКА",color=Accent,fontSize=11.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(5.dp));Text("Разреши Sleepy точные будильники в настройках Android, чтобы сигнал срабатывал вовремя.",color=Muted,fontSize=13.sp)}}
  Spacer(Modifier.height(24.dp))
 }
 if(show)AlarmEditorDialog(editing,{show=false}){a->if(editing==null)add(a)else edit(a);show=false}
}

@Composable private fun AlarmEditorDialog(existing:AlarmModel?,dismiss:()->Unit,save:(AlarmModel)->Unit){
 val context=LocalContext.current
 var hour by remember{mutableIntStateOf(existing?.hour?:Calendar.getInstance().get(Calendar.HOUR_OF_DAY))}
 var minute by remember{mutableIntStateOf(existing?.minute?:((Calendar.getInstance().get(Calendar.MINUTE)+4)/5*5)%60)}
 var label by remember{mutableStateOf(existing?.label?:"Будильник")}
 var days by remember{mutableStateOf(existing?.repeatDays?:emptySet())}
 var type by remember{mutableStateOf(existing?.soundType?:"default")}
 var fileUri by remember{mutableStateOf(if(existing?.soundType=="file")existing.soundUri else "")}
 var radioUrl by remember{mutableStateOf(if(existing?.soundType=="radio")existing.soundUri else "")}
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){u->if(u!=null){fileUri=u.toString();type="file";try{context.contentResolver.takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION)}catch(_:Exception){}}}
 AlertDialog(onDismissRequest=dismiss,containerColor=Card,title={Text(if(existing==null)"Новый будильник" else "Настроить будильник",color=Color.White,fontWeight=FontWeight.Bold)},text={
  Column(Modifier.heightIn(max=520.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)){
   Text("ВРЕМЯ",color=Muted,fontSize=11.sp,fontWeight=FontWeight.Bold)
   Surface(color=Soft,shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth().clickable{TimePickerDialog(context,{_,h,m->hour=h;minute=m},hour,minute,true).show()}){
    Row(Modifier.padding(18.dp),verticalAlignment=Alignment.CenterVertically){Text(String.format(Locale.getDefault(),"%02d:%02d",hour,minute),color=Color.White,fontSize=42.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.weight(1f));Text("Изменить ›",color=Accent,fontSize=13.sp)}
   }
   OutlinedTextField(value=label,onValueChange={label=it},label={Text("Название")},singleLine=true,modifier=Modifier.fillMaxWidth())
   Text("ПОВТОР",color=Muted,fontSize=11.sp,fontWeight=FontWeight.Bold)
   Row(horizontalArrangement=Arrangement.spacedBy(3.dp)){DayNames.forEachIndexed{i,d->val n=i+1;FilterChip(selected=n in days,onClick={days=if(n in days)days-n else days+n},label={Text(d)})}}
   Text("ЗВУК",color=Muted,fontSize=11.sp,fontWeight=FontWeight.Bold)
   Row(horizontalArrangement=Arrangement.spacedBy(4.dp)){FilterChip(selected=type=="default",onClick={type="default"},label={Text("Обычный")});FilterChip(selected=type=="file",onClick={type="file"},label={Text("Музыка")});FilterChip(selected=type=="radio",onClick={type="radio"},label={Text("Радио")})}
   if(type=="file")Button(onClick={picker.launch(arrayOf("audio/*"))},modifier=Modifier.fillMaxWidth()){Text(if(fileUri.isBlank())"Выбрать аудиофайл" else "Выбрать другой файл")}
   if(type=="radio")OutlinedTextField(value=radioUrl,onValueChange={radioUrl=it},label={Text("URL радиопотока")},placeholder={Text("https://...")},singleLine=true,modifier=Modifier.fillMaxWidth())
   Text(if(days.isEmpty())"Сработает один раз" else "Повтор по выбранным дням",color=Muted,fontSize=12.sp)
  }
 },confirmButton={Button(onClick={val id=existing?.id?:((System.currentTimeMillis()%100000000).toInt());val uri=when(type){"file"->fileUri;"radio"->radioUrl.trim();else->""};save(AlarmModel(id,hour,minute,true,days,uri,type,label.trim().ifBlank{"Будильник"}))},enabled=(type!="file"||fileUri.isNotBlank())&&(type!="radio"||radioUrl.startsWith("http"))){Text("Сохранить")}},dismissButton={TextButton(onClick=dismiss){Text("Отмена")}})
}

@Composable private fun SleepScreen(context:Context){
 var sessions by remember{mutableStateOf(AlarmStore.sleepSessions(context))}
 var bedtime by remember{mutableLongStateOf(AlarmStore.bedtime(context))}
 val avg=if(sessions.isEmpty())0L else sessions.map{it.second-it.first}.average().toLong()
 val fmt=remember{SimpleDateFormat("d MMM, HH:mm",Locale("ru"))}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)){
  Spacer(Modifier.height(28.dp));Header("Твой сон","История и привычки");Spacer(Modifier.height(24.dp))
  CardBox{Column(Modifier.padding(22.dp)){Text("СРЕДНЯЯ ПРОДОЛЖИТЕЛЬНОСТЬ",color=Muted,fontSize=11.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(10.dp));Text(if(avg>0)(avg/3600000).toString()+" ч "+((avg%3600000)/60000)+" мин" else "—",color=Color.White,fontSize=34.sp,fontWeight=FontWeight.Bold);Text(if(sessions.isEmpty())"Добавляй записи, чтобы увидеть статистику" else "По "+sessions.size+" последним записям",color=Muted,fontSize=12.sp)}}
  Spacer(Modifier.height(14.dp));CardBox{Column(Modifier.padding(18.dp)){Text(if(bedtime>0)"Запись сна начата" else "Отметь время сна",color=Color.White,fontSize=17.sp,fontWeight=FontWeight.SemiBold);Spacer(Modifier.height(6.dp));Text(if(bedtime>0)"Утром нажми кнопку пробуждения" else "Нажми перед тем, как лечь спать",color=Muted,fontSize=13.sp);Spacer(Modifier.height(14.dp));Button(onClick={if(bedtime==0L){bedtime=System.currentTimeMillis();AlarmStore.setBedtime(context,bedtime)}else{val now=System.currentTimeMillis();if(now>bedtime){AlarmStore.addSleep(context,bedtime,now);sessions=AlarmStore.sleepSessions(context)};bedtime=0L;AlarmStore.setBedtime(context,0L)}},modifier=Modifier.fillMaxWidth().height(52.dp)){Text(if(bedtime>0)"Я проснулся" else "Я ложусь спать")}}}
  Spacer(Modifier.height(20.dp));Text("ПОСЛЕДНИЕ ЗАПИСИ",color=Muted,fontSize=11.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(8.dp))
  if(sessions.isEmpty())Text("Здесь появится история сна.",color=Muted,fontSize=13.sp)
  sessions.sortedByDescending{it.first}.take(7).forEach{p->Spacer(Modifier.height(7.dp));CardBox{Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(fmt.format(Date(p.first)),color=Color.White,fontSize=14.sp);Text(fmt.format(Date(p.second)),color=Muted,fontSize=12.sp)};val d=(p.second-p.first).coerceAtLeast(0);Text((d/3600000).toString()+" ч "+((d%3600000)/60000)+" мин",color=Accent,fontWeight=FontWeight.SemiBold)}}}
  Text("Записи вводятся вручную; приложение не определяет фазы сна.",color=Muted,fontSize=11.sp);Spacer(Modifier.height(24.dp))
 }
}

@Composable private fun SettingsScreen(context:Context){
 var vibration by remember{mutableStateOf(AlarmStore.setting(context,"vibration",true))}
 var snooze by remember{mutableIntStateOf(AlarmStore.snoozeMinutes(context))}
 var ramp by remember{mutableIntStateOf(AlarmStore.rampSeconds(context))}
 val exact=android.os.Build.VERSION.SDK_INT<31||context.getSystemService(android.app.AlarmManager::class.java).canScheduleExactAlarms()
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)){
  Spacer(Modifier.height(28.dp));Header("Настройки","Настрой пробуждение под себя");Spacer(Modifier.height(24.dp))
  CardBox{Column(Modifier.padding(18.dp)){
   Text("БУДИЛЬНИК",color=Muted,fontSize=11.sp,fontWeight=FontWeight.Bold)
   Row(verticalAlignment=Alignment.CenterVertically,modifier=Modifier.fillMaxWidth().padding(top=10.dp)){Column(Modifier.weight(1f)){Text("Вибрация",color=Color.White);Text("Вместе со звуковым сигналом",color=Muted,fontSize=12.sp)};Switch(checked=vibration,onCheckedChange={vibration=it;AlarmStore.setSetting(context,"vibration",it)})}
   Spacer(Modifier.height(12.dp));Text("Повтор сигнала: "+snooze+" минут",color=Color.White);Slider(value=snooze.toFloat(),onValueChange={snooze=it.toInt()},valueRange=5f..30f,steps=4)
   Text("Нарастание громкости: "+ramp+" сек",color=Color.White);Slider(value=ramp.toFloat(),onValueChange={ramp=it.toInt()},valueRange=0f..60f,steps=5)
   Button(onClick={AlarmStore.setSnoozeMinutes(context,snooze);AlarmStore.setRampSeconds(context,ramp)},modifier=Modifier.fillMaxWidth()){Text("Сохранить настройки")}
  }}
  Spacer(Modifier.height(12.dp));CardBox{Column(Modifier.padding(18.dp)){Text("РАЗРЕШЕНИЯ",color=Muted,fontSize=11.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(8.dp));Text(if(exact)"Точные будильники разрешены" else "Нужно разрешить точные будильники",color=if(exact)Color(0xFF83D6A2)else Color(0xFFFFB86B),fontWeight=FontWeight.SemiBold);Spacer(Modifier.height(8.dp));Button(onClick={if(android.os.Build.VERSION.SDK_INT>=31)context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+context.packageName)))else context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+context.packageName)))},modifier=Modifier.fillMaxWidth()){Text("Открыть настройки Android")}}}
  Spacer(Modifier.height(20.dp));Text("Sleepy 2.0",color=Accent,fontWeight=FontWeight.Bold);Text("Твой будильник. Твой ритм.",color=Muted,fontSize=12.sp);Spacer(Modifier.height(24.dp))
 }
}
