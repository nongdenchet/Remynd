package com.rain.remynd.alarm.scheduler

data class Alarm(
    val id: Long,
    val content: String,
    val triggerAt: Long,
    val vibrate: Boolean,
    val interval: Long?
)
