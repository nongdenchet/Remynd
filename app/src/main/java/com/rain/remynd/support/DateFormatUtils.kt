package com.rain.remynd.support

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun formatTime(format: String, date: Date): String {
    return SimpleDateFormat(format, Locale.getDefault()).format(date)
}

fun formatDuration(duration: Long?): String {
    return when {
        duration == null || duration <= 0 -> "None"
        duration < TimeUnit.HOURS.toMillis(1) ->
            String.format("%dm", TimeUnit.MILLISECONDS.toMinutes(duration))
        else -> {
            val hour = TimeUnit.MILLISECONDS.toHours(duration)
            val minuteMilliseconds = duration - TimeUnit.HOURS.toMillis(hour)
            val minute = TimeUnit.MILLISECONDS.toMinutes(minuteMilliseconds)

            String.format("%dh : %dm", hour, minute)
        }
    }
}

val indexToDateSymbol = mapOf(
    Calendar.SUNDAY to 'S',
    Calendar.MONDAY to 'M',
    Calendar.TUESDAY to 'T',
    Calendar.WEDNESDAY to 'W',
    Calendar.THURSDAY to 'T',
    Calendar.FRIDAY to 'F',
    Calendar.SATURDAY to 'S'
)

val indexToDate = mapOf(
    Calendar.SUNDAY to "Sun",
    Calendar.MONDAY to "Mon",
    Calendar.TUESDAY to "Tue",
    Calendar.WEDNESDAY to "Wed",
    Calendar.THURSDAY to "Thu",
    Calendar.FRIDAY to "Fri",
    Calendar.SATURDAY to "Sat"
)
