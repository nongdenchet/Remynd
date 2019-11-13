package com.rain.remynd.ui.details

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RemyndDetailsForm(
    val content: String
) : Parcelable
