package com.rain.remynd.alarm

import com.rain.remynd.data.RemyndEntity

data class AlarmInput(
    val id: Long,
    val content: String,
    val triggerAt: Long,
    val vibrate: Boolean,
    val interval: Long?
)

fun RemyndEntity.toAlarm(): AlarmInput {
    return AlarmInput(id, content, triggerAt, vibrate, interval)
}
