package com.mrnv.sleepy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class AlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AlarmScreen { stopService(Intent(this, AlarmService::class.java)); finish() } }
    }
}

@Composable private fun AlarmScreen(stop: () -> Unit) {
    Surface(color = Color(0xFF0B0B10), modifier = Modifier.fillMaxSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize().padding(28.dp)) {
            Text("Пора просыпаться", color = Color(0xFFB6A7FF), fontSize = 18.sp)
            Spacer(Modifier.height(16.dp)); Text("☀", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp)); Text("Доброе утро", color = Color.White, fontSize = 34.sp)
            Spacer(Modifier.height(48.dp))
            Button(onClick = stop, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Остановить") }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Отложить на 10 минут") }
        }
    }
}
