package com.rain.remynd.ui.details

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.Calendar

@Parcelize
data class RemyndForm(
    val id: Long?,
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
        val hour: Int,
        val minute: Int,
        val daysOfWeek: List<Int>
    ) : DateConfig()
}
