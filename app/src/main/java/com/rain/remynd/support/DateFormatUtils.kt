package com.rain.remynd.support

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTime(format: String, date: Date): String {
    return SimpleDateFormat(format, Locale.getDefault()).format(date)
}
