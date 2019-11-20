package com.rain.remynd.support

import com.rain.remynd.alarm.AlarmInput
import com.rain.remynd.data.RemyndEntity

fun RemyndEntity.toAlarm(): AlarmInput {
    return AlarmInput(id, content, triggerAt, vibrate, interval)
}
