package com.rain.remynd.ui.details

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.Calendar

@Parcelize
data class RemyndForm(
    val content: String,
    val dateConfig: DateConfig,
    val enabled: Boolean,
    val vibrate: Boolean,
    val interval: Long?
) : Parcelable

sealed class DateConfig : Parcelable {
    @Parcelize
    data class SingleDate(val date: Calendar) : DateConfig()

    @Parcelize
    data class RepeatDate(
        val hourOfDay: Int,
        val minute: Int,
        val second: Int,
        val daysOfWeek: List<Int>
    ) : DateConfig()
}