package com.rain.remynd.list

import com.rain.remynd.alarm.bridge.Alarm
import com.rain.remynd.data.RemyndEntity

internal fun RemyndEntity.toAlarm(): Alarm {
    return Alarm(id, content, triggerAt, vibrate, interval)
}
