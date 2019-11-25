package com.rain.remynd.alarm

import com.rain.remynd.alarm.scheduler.Alarm
import com.rain.remynd.data.RemyndEntity

internal const val MESSAGE = "MESSAGE"
internal const val VIBRATE = "VIBRATE"
internal const val INTERVAL = "INTERVAL"
internal const val ID = "ID"
internal const val CHANNEL_ID = "CHANNEL_ID"
internal const val TYPE = "TYPE"
internal const val DISMISS = "DISMISS"
internal const val REPEAT = "REPEAT"

fun RemyndEntity.toAlarm(): Alarm {
    return Alarm(id, content, triggerAt, vibrate, interval)
}
