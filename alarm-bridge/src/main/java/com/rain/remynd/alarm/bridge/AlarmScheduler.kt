package com.rain.remynd.alarm.bridge

interface AlarmScheduler {
    fun cancel(ids: Set<Long>)
    fun cancel(alarm: Alarm)
    fun schedule(alarm: Alarm)
}
