package com.rain.remynd.common

import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

interface RemindFormatUtils {
    fun execute(triggerAt: Long): String
}

class RemindFormatUtilsImpl(private val resourcesProvider: ResourcesProvider) : RemindFormatUtils {
    override fun execute(triggerAt: Long): String {
        val current = Calendar.getInstance()
        if (triggerAt < current.timeInMillis) {
            throw IllegalArgumentException("triggerAt is in the past")
        }

        if (triggerAt - current.timeInMillis >= TimeUnit.DAYS.toMillis(1)) {
            return resourcesProvider.getString(
                R.string.alarm_date_scheduled,
                formatTime("EEEE, dd MMMM", Date(triggerAt))
            )
        }

        val date = Date(triggerAt)
        return resourcesProvider.getString(
            R.string.alarm_time_scheduled,
            formatTime("hh", date),
            formatTime("mm", date)
        )
    }
}
