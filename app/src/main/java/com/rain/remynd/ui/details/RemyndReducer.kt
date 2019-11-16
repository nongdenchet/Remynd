package com.rain.remynd.ui.details

import java.util.Calendar

sealed class RemyndFormAction {
    data class UpdateContent(val value: String) : RemyndFormAction()
    data class UpdateVibrate(val value: Boolean) : RemyndFormAction()
    data class UpdateDate(val year: Int, val month: Int, val day: Int) : RemyndFormAction()
    data class UpdateTime(val hourOfDay: Int, val minute: Int) : RemyndFormAction()
}

class RemyndReducer {
    fun reduce(prev: RemyndForm, action: RemyndFormAction): RemyndForm {
        return when (action) {
            is RemyndFormAction.UpdateContent -> updateContent(prev, action)
            is RemyndFormAction.UpdateDate -> updateDate(prev, action)
            is RemyndFormAction.UpdateTime -> updateTime(prev, action)
            is RemyndFormAction.UpdateVibrate -> updateVibrate(prev, action)
        }
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

    private fun updateTime(
        prev: RemyndForm,
        action: RemyndFormAction.UpdateTime
    ): RemyndForm {
        val (hour, minute) = action
        val dateConfig = (prev.dateConfig as DateConfig.SingleDate).let {
            val newDate = Calendar.getInstance()
            newDate.timeInMillis = it.date.timeInMillis
            newDate.set(Calendar.HOUR_OF_DAY, hour)
            newDate.set(Calendar.MINUTE, minute)
            DateConfig.SingleDate(newDate)
        }
        return prev.copy(dateConfig = dateConfig)
    }

    private fun updateDate(
        prev: RemyndForm,
        action: RemyndFormAction.UpdateDate
    ): RemyndForm {
        val (year, month, day) = action
        val dateConfig = (prev.dateConfig as DateConfig.SingleDate).let {
            val newDate = Calendar.getInstance()
            newDate.timeInMillis = it.date.timeInMillis
            newDate.set(Calendar.YEAR, year)
            newDate.set(Calendar.MONTH, month)
            newDate.set(Calendar.DAY_OF_MONTH, day)
            DateConfig.SingleDate(newDate)
        }
        return prev.copy(dateConfig = dateConfig)
    }

    private fun updateContent(
        prev: RemyndForm,
        action: RemyndFormAction.UpdateContent
    ): RemyndForm {
        val content = action.value
        return if (prev.content == content) {
            prev
        } else {
            prev.copy(content = content)
        }
    }
}
