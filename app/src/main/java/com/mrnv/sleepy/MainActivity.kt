package com.mrnv.sleepy

import android.Manifest
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

private val Bg = Color(0xFF0B0B10)
private val CardBg = Color(0xFF15151D)
private val Accent = Color(0xFF8B78FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SleepyApp() }
    }
}

@Composable fun SleepyApp() {
    val context = LocalContext.current
    var tab by remember { mutableIntStateOf(0) }
    var alarms by remember { mutableStateOf(AlarmStore.load(context)) }
    val notifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= 33) notifications.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    Surface(color = Bg, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            when (tab) {
                0 -> HomeScreen(alarms, { a ->
                    val updated = alarms.map { if (it.id == a.id) it.copy(enabled = !it.enabled) else it }
                    alarms = updated; AlarmStore.save(context, updated); updated.forEach { AlarmScheduler.schedule(context, it) }
                }, { a ->
                    alarms = alarms + a; AlarmStore.save(context, alarms); AlarmScheduler.schedule(context, a)
                })
                1 -> SleepScreen(context)
                else -> SettingsScreen(context)
            }
            NavigationBar(containerColor = Color(0xFF111118)) {
                NavigationBarItem(selected = tab == 0, onClick = { tab = 0 }, icon = { Text("⌁") }, label = { Text("Будильники") })
                NavigationBarItem(selected = tab == 1, onClick = { tab = 1 }, icon = { Text("◒") }, label = { Text("Сон") })
                NavigationBarItem(selected = tab == 2, onClick = { tab = 2 }, icon = { Text("⚙") }, label = { Text("Настройки") })
            }
        }
    }
}

@Composable private fun HomeScreen(alarms: List<AlarmModel>, onToggle: (AlarmModel) -> Unit, onAdd: (AlarmModel) -> Unit) {
    val context = LocalContext.current
    var show by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(38.dp))
        Text("Sleepy", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Спокойный сон. Точное пробуждение.", color = Color(0xFF85858F), fontSize = 14.sp)
        Spacer(Modifier.height(24.dp))
        Text("БУДИЛЬНИКИ", color = Color(0xFF777782), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp)) {
            items(alarms, key = { it.id }) { alarm ->
                Surface(color = CardBg, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(20.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(String.format("%02d:%02d", alarm.hour, alarm.minute), color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.SemiBold)
                            Text(alarm.label, color = Color(0xFF85858F), fontSize = 13.sp)
                            Text(if (alarm.repeatDays.isEmpty()) "Один раз" else "Повтор: " + alarm.repeatDays.sorted().joinToString(", "), color = Accent, fontSize = 11.sp)
                        }
                        Switch(checked = alarm.enabled, onCheckedChange = { onToggle(alarm) })
                    }
                }
            }
            item { OutlinedButton(onClick = { show = true }, modifier = Modifier.fillMaxWidth().height(58.dp)) { Text("＋  Добавить будильник") } }
        }
    }
    if (show) AddAlarmDialog({ show = false }, { onAdd(it); show = false }, context)
}

@Composable private fun AddAlarmDialog(onDismiss: () -> Unit, onSave: (AlarmModel) -> Unit, context: Context) {
    var hour by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableIntStateOf(((Calendar.getInstance().get(Calendar.MINUTE) + 5) / 5 * 5) % 60) }
    var label by remember { mutableStateOf("Будильник") }
    var radio by remember { mutableStateOf(false) }
    var uri by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(setOf(1, 2, 3, 4, 5)) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { picked ->
        if (picked != null) {
            uri = picked.toString()
            try { context.contentResolver.takePersistableUriPermission(picked, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            radio = false
        }
    }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Новый будильник") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(String.format("%02d:%02d", hour, minute), fontSize = 38.sp, color = Accent, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { val t = (hour * 60 + minute - 1 + 1440) % 1440; hour = t / 60; minute = t % 60 }) { Text("− 1 мин") }
                Button(onClick = { val t = (hour * 60 + minute + 1) % 1440; hour = t / 60; minute = t % 60 }) { Text("+ 1 мин") }
            }
            OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Название") }, singleLine = true)
            Text("Повтор")
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                listOf("П" to 1, "В" to 2, "С" to 3, "Ч" to 4, "П" to 5, "С" to 6, "В" to 7).forEach { (day, value) ->
                    FilterChip(selected = value in selected, onClick = { selected = if (value in selected) selected - value else selected + value }, label = { Text(day) })
                }
            }
            Button(onClick = { picker.launch(arrayOf("audio/*")) }) { Text("Выбрать песню") }
            Text(if (uri.isBlank()) "Стандартный звук" else "Аудиофайл выбран", color = Color.Gray, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = radio, onCheckedChange = { radio = it })
                Text("Радио по URL")
            }
            if (radio) OutlinedTextField(value = uri, onValueChange = { uri = it }, label = { Text("URL потока") }, singleLine = true)
        }
    }, confirmButton = {
        Button(onClick = {
            onSave(AlarmModel((System.currentTimeMillis() % 1000000).toInt(), hour, minute, true, selected, uri, if (radio) "radio" else if (uri.isNotBlank()) "file" else "default", label))
        }) { Text("Сохранить") }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } })
}

@Composable private fun SleepScreen(context: Context) {
    val sessions = remember { AlarmStore.sleepSessions(context) }
    val average = if (sessions.isEmpty()) 0L else sessions.map { it.second - it.first }.average().toLong()
    val hours = average / 3600000
    val minutes = (average % 3600000) / 60000
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Spacer(Modifier.height(38.dp))
        Text("Сон", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Ваша история и ритм", color = Color(0xFF85858F), fontSize = 14.sp)
        Spacer(Modifier.height(24.dp))
        Surface(color = CardBg, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp)) {
                Text("СРЕДНЕЕ ВРЕМЯ СНА", color = Color(0xFF777782), fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                Text(if (average > 0) "${hours}ч ${minutes}м" else "Пока нет данных", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                Text("Статистика появится после добавления записей сна", color = Color(0xFF85858F), fontSize = 13.sp)
            }
        }
    }
}

@Composable private fun SettingsScreen(context: Context) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Spacer(Modifier.height(38.dp))
        Text("Настройки", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Surface(color = CardBg, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().clickable {
            context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}")))
        }) {
            Column(Modifier.padding(18.dp)) {
                Text("Точные будильники", color = Color.White, fontSize = 16.sp)
                Text("Открыть разрешение Android", color = Color(0xFF85858F), fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Sleepy • Версия 0.1.0", color = Color.Gray)
    }
}
