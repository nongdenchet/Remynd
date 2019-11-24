package com.rain.remynd.common

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class RemindFormatUtilsImplTest {
    private val now = 1574590207573
    private val utils = RemindFormatUtilsImpl(MockResourcesProvider()) {
        Calendar.getInstance().apply {
            timeInMillis = now
            timeZone = TimeZone.getTimeZone("Asia/Singapore")
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `when time in the past, should raise IllegalArgumentException`() {
        utils.execute(1574590207000)
    }

    @Test
    fun `when time is more than a day, return specific time`() {
        assertEquals(
            "${R.string.alarm_date_scheduled} Tuesday, 26 November",
            utils.execute(Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.DATE, 2)
            }.timeInMillis)
        )
    }

    @Test
    fun `when time is less than a day, return duration format`() {
        assertEquals(
            "${R.string.alarm_time_scheduled} 4;30",
            utils.execute(Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.HOUR, 4)
                add(Calendar.MINUTE, 30)
            }.timeInMillis)
        )
    }
}
