package com.rain.remynd.ui.details

import com.rain.remynd.data.RemyndEntity
import com.rain.remynd.support.formatTime
import com.rain.remynd.support.indexToDate
import com.rain.remynd.view.DateItem
import java.util.Calendar
import java.util.concurrent.TimeUnit

class RemyndDetailsViewModelMapper {
    fun toViewModel(form: RemyndForm): RemyndDetailsViewModel {
        return RemyndDetailsViewModel(
            dateInfo = mapDateInfo(form.dateConfig),
            timeInfo = mapTimeInfo(form.dateConfig),
            dateItems = mapDateItems(form.dateConfig),
            intervalInfo = mapIntervalInfo(form.interval),
            content = form.contentInfo,
            vibrate = form.vibrate,
            enabled = form.enabled
        )
    }

    private fun mapIntervalInfo(interval: Long?): IntervalInfo {
        val display = when {
            interval == null || interval <= 0 -> "None"
            interval < TimeUnit.HOURS.toMillis(1) ->
                String.format("%dm", TimeUnit.MILLISECONDS.toMinutes(interval))
            else -> {
                val hour = TimeUnit.MILLISECONDS.toHours(interval)
                val minuteMilliseconds = interval - TimeUnit.HOURS.toMillis(hour)
                val minute = TimeUnit.MILLISECONDS.toMinutes(minuteMilliseconds)

                String.format("%dh : %dm", hour, minute)
            }
        }

        return IntervalInfo(
            interval = interval ?: 0,
            display = display
        )
    }

    fun toEntity(form: RemyndForm): RemyndEntity {
        val triggerAt = when (val date = form.dateConfig) {
            is DateConfig.SingleDate -> date.date.timeInMillis
            is DateConfig.RepeatDate -> {
                val today = Calendar.getInstance()
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, date.hour)
                    set(Calendar.MINUTE, date.minute)
                }
                val daySet = date.daysOfWeek.toSet()
                while (today >= calendar || !daySet.contains(calendar.get(Calendar.DAY_OF_WEEK))) {
                    calendar.add(Calendar.DATE, 1)
                }
                calendar.timeInMillis
            }
        }

        val daysOfWeek = when (val date = form.dateConfig) {
            is DateConfig.SingleDate -> ""
            is DateConfig.RepeatDate -> date.daysOfWeek.joinToString(";")
        }

        return RemyndEntity(
            id = form.id ?: 0,
            content = form.contentInfo.content,
            active = form.enabled,
            vibrate = form.vibrate,
            interval = form.interval,
            triggerAt = triggerAt,
            daysOfWeek = daysOfWeek
        )
    }

    private fun mapTimeInfo(dateConfig: DateConfig): TimeInfo {
        return when (dateConfig) {
            is DateConfig.SingleDate -> {
                val date = dateConfig.date
                TimeInfo(
                    displayTime = formatTime("hh:mm", date.time),
                    clock = formatTime("a", date.time),
                    hour = date.get(Calendar.HOUR_OF_DAY),
                    minute = date.get(Calendar.MINUTE)
                )
            }
            is DateConfig.RepeatDate -> {
                val date = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, dateConfig.hour)
                    set(Calendar.MINUTE, dateConfig.minute)
                }
                TimeInfo(
                    displayTime = formatTime("hh:mm", date.time),
                    clock = formatTime("a", date.time),
                    hour = dateConfig.hour,
                    minute = dateConfig.minute
                )
            }
        }
    }

    private fun mapDateItems(dateConfig: DateConfig): List<DateItem> {
        return when (dateConfig) {
            is DateConfig.SingleDate -> {
                Array(7) { DateItem(Calendar.SUNDAY + it, false) }
                    .toList()
            }
            is DateConfig.RepeatDate -> {
                val daysSet = dateConfig.daysOfWeek.toSet()
                return Array(7) { Calendar.SUNDAY + it }
                    .map { DateItem(it, daysSet.contains(it)) }
                    .toList()
            }
        }
    }

    private fun mapDateInfo(dateConfig: DateConfig): DateInfo {
        return when (dateConfig) {
            is DateConfig.SingleDate -> {
                val date = dateConfig.date
                DateInfo(
                    year = date.get(Calendar.YEAR),
                    month = date.get(Calendar.MONTH),
                    day = date.get(Calendar.DAY_OF_MONTH),
                    displayDate = formatTime("EEE, dd MMM", date.time)
                )
            }
            is DateConfig.RepeatDate -> {
                val tomorrow = Calendar.getInstance().apply { add(Calendar.DATE, 1) }
                val daysSet = dateConfig.daysOfWeek.toSet()
                val displayDate = Array(7) { Calendar.SUNDAY + it }
                    .filter { daysSet.contains(it) }
                    .map { indexToDate[it] }
                    .joinToString(", ")
                DateInfo(
                    year = tomorrow.get(Calendar.YEAR),
                    month = tomorrow.get(Calendar.MONTH),
                    day = tomorrow.get(Calendar.DAY_OF_MONTH),
                    displayDate = displayDate
                )
            }
        }
    }
}
