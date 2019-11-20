package com.rain.remynd.support

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun formatTime(format: String, date: Date): String {
    return SimpleDateFormat(format, Locale.getDefault()).format(date)
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
