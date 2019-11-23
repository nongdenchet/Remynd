package com.rain.remynd.common

import org.junit.Assert.assertEquals
import org.junit.Test

class DateFormatUtilsTest {

    @Test
    fun formatTime_whenDurationLessThanHour_formatMinutes() {
        assertEquals("15m", formatDuration(900000))
    }

    @Test
    fun formatTime_whenDurationMoreThanHour_formatHourAndMinutes() {
        assertEquals("1h : 15m", formatDuration(4500000))
    }

    @Test
    fun formatTime_whenDurationNotValid_returnNone() {
        assertEquals("None", formatDuration(-1))
        assertEquals("None", formatDuration(0))
        assertEquals("None", formatDuration(null))
    }
}
