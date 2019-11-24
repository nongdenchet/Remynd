package com.rain.remynd.common

import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

interface RemindFormatUtils {
    fun execute(triggerAt: Long): String
}

class RemindFormatUtilsImpl(
    private val resourcesProvider: ResourcesProvider,
    private val currentTime: () -> Calendar = { Calendar.getInstance() }
) : RemindFormatUtils {
    override fun execute(triggerAt: Long): String {
        val current = currentTime()
        require(triggerAt >= current.timeInMillis) { "triggerAt is in the past" }

        val duration = triggerAt - current.timeInMillis
        if (duration >= TimeUnit.DAYS.toMillis(1)) {
            return resourcesProvider.getString(
                R.string.alarm_date_scheduled,
                formatTime("EEEE, dd MMMM", Date(triggerAt))
            )
        }

        val hour = TimeUnit.MILLISECONDS.toHours(duration)
        val minute = TimeUnit.MILLISECONDS.toMinutes(duration - TimeUnit.HOURS.toMillis(hour))
        return resourcesProvider.getString(R.string.alarm_time_scheduled, hour, minute)
    }
}
