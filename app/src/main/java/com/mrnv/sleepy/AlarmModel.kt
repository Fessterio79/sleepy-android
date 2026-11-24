package com.mrnv.sleepy

data class AlarmModel(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean,
    val repeatDays: Set<Int>,
    val soundUri: String = "",
    val soundType: String = "default",
    val label: String = "Будильник"
)
