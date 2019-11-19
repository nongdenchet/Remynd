package com.rain.remynd.ui.details

import com.rain.remynd.view.DateItem
import kotlinx.coroutines.flow.Flow

data class RemyndDetailsViewModel(
    val timeInfo: TimeInfo,
    val dateInfo: DateInfo,
    val content: String,
    val vibrate: Boolean,
    val enabled: Boolean,
    val dateItems: List<DateItem>,
    val intervalInfo: IntervalInfo
)

data class IntervalInfo(
    val interval: Long,
    val display: String
)

data class DateInfo(
    val displayDate: String,
    val year: Int,
    val month: Int,
    val day: Int
)

data class TimeInfo(
    val displayTime: String,
    val clock: String,
    val hour: Int,
    val minute: Int
)

interface RemyndDetailsView {
    fun render(vm: RemyndDetailsViewModel)
    fun contentChanges(): Flow<String>
    fun goBack()
    fun showError(content: String)
}
