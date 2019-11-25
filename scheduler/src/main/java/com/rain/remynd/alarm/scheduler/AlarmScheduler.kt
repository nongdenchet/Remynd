package com.rain.remynd.alarm.scheduler

interface AlarmScheduler {
    fun cancel(ids: Set<Long>)
    fun cancel(alarm: Alarm)
    fun schedule(alarm: Alarm)
}
