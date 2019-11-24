package com.rain.remynd.details

import com.rain.remynd.view.DateItem
import java.util.Calendar

internal sealed class RemyndFormAction {
    data class UpdateContent(val value: String) : RemyndFormAction()
    data class UpdateVibrate(val value: Boolean) : RemyndFormAction()
    data class UpdateEnabled(val value: Boolean) : RemyndFormAction()
    data class UpdateDate(val year: Int, val month: Int, val day: Int) : RemyndFormAction()
    data class UpdateTime(val hourOfDay: Int, val minute: Int) : RemyndFormAction()
    data class UpdateItems(val items: List<DateItem>) : RemyndFormAction()
    data class UpdateInterval(val duration: Long?) : RemyndFormAction()
}

internal class RemyndReducer {
    fun reduce(prev: RemyndForm, action: RemyndFormAction): RemyndForm {
        return when (action) {
            is RemyndFormAction.UpdateContent -> updateContent(prev, action)
            is RemyndFormAction.UpdateDate -> updateDate(prev, action)
            is RemyndFormAction.UpdateTime -> updateTime(prev, action)
            is RemyndFormAction.UpdateVibrate -> updateVibrate(prev, action)
            is RemyndFormAction.UpdateEnabled -> updateEnabled(prev, action)
            is RemyndFormAction.UpdateItems -> updateDateItems(prev, action)
            is RemyndFormAction.UpdateInterval -> updateInterval(prev, action)
        }
    }

    private fun updateInterval(
        prev: RemyndForm,
        action: RemyndFormAction.UpdateInterval
    ): RemyndForm {
        val interval = when {
            action.duration == null || action.duration <= 0 -> null
            else -> action.duration
        }
        return if (prev.interval == interval) {
            prev
        } else {
            prev.copy(interval = interval)
        }
    }

    private fun updateDateItems(
        prev: RemyndForm,
        action: RemyndFormAction.UpdateItems
    ): RemyndForm {
        val daysOfWeek = action.items
            .filter { it.checked }
            .map { it.dateInWeek }
        val dateConfig: DateConfig = when (val curr = prev.dateConfig) {
            is DateConfig.SingleDate -> {
                if (daysOfWeek.isNotEmpty()) {
                    DateConfig.RepeatDate(
                        hour = curr.date.get(Calendar.HOUR_OF_DAY),
                        minute = curr.date.get(Calendar.MINUTE),
                        daysOfWeek = daysOfWeek
                    )
                } else {
                    curr
                }
            }
            is DateConfig.RepeatDate -> {
                if (daysOfWeek.isNotEmpty()) {
                    curr.copy(daysOfWeek = daysOfWeek)
                } else {
                    DateConfig.SingleDate(
                        Calendar.getInstance().apply {
                            add(Calendar.DATE, 1)
                            set(Calendar.HOUR_OF_DAY, 19)
                            set(Calendar.MINUTE, 30)
                        }
                    )
                }
            }
        }
        return prev.copy(dateConfig = dateConfig)
    }

    private fun updateVibrate(
        prev: RemyndForm,
        action: RemyndFormAction.UpdateVibrate
    ): RemyndForm {
        val vibrate = action.value
        return if (prev.vibrate == vibrate) {
            prev
        } else {
            prev.copy(vibrate = vibrate)
        }
    }

    private fun updateEnabled(
        prev: RemyndForm,
        action: RemyndFormAction.UpdateEnabled
    ): RemyndForm {
        val vibrate = action.value
        return if (prev.enabled == vibrate) {
            prev
        } else {
            prev.copy(enabled = vibrate)
        }
    }

    private fun updateTime(
        prev: RemyndForm,
        action: RemyndFormAction.UpdateTime
    ): RemyndForm {
        val (hour, minute) = action
        val dateConfig = when (val curr = prev.dateConfig) {
            is DateConfig.SingleDate -> {
                val newDate = Calendar.getInstance()
                newDate.timeInMillis = curr.date.timeInMillis
                newDate.set(Calendar.HOUR_OF_DAY, hour)
                newDate.set(Calendar.MINUTE, minute)
                DateConfig.SingleDate(newDate)
            }
            is DateConfig.RepeatDate -> {
                curr.copy(hour = hour, minute = minute)
            }
        }
        return prev.copy(dateConfig = dateConfig)
    }

    private fun updateDate(
        prev: RemyndForm,
        action: RemyndFormAction.UpdateDate
    ): RemyndForm {
        val (year, month, day) = action
        val dateConfig = when (val curr = prev.dateConfig) {
            is DateConfig.SingleDate -> {
                val newDate = Calendar.getInstance()
                newDate.timeInMillis = curr.date.timeInMillis
                newDate.set(Calendar.YEAR, year)
                newDate.set(Calendar.MONTH, month)
                newDate.set(Calendar.DAY_OF_MONTH, day)
                DateConfig.SingleDate(newDate)
            }
            is DateConfig.RepeatDate -> {
                val newDate = Calendar.getInstance()
                newDate.set(Calendar.YEAR, year)
                newDate.set(Calendar.MONTH, month)
                newDate.set(Calendar.DAY_OF_MONTH, day)
                newDate.set(Calendar.HOUR_OF_DAY, curr.hour)
                newDate.set(Calendar.MINUTE, curr.minute)
                DateConfig.SingleDate(newDate)
            }
        }
        return prev.copy(dateConfig = dateConfig)
    }

    private fun updateContent(
        prev: RemyndForm,
        action: RemyndFormAction.UpdateContent
    ): RemyndForm {
        val content = action.value
        return if (prev.contentInfo.content == content) {
            prev
        } else {
            prev.copy(contentInfo = ContentInfo(true, content))
        }
    }
}
