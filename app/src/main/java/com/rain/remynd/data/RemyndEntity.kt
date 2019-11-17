package com.rain.remynd.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "remynd_table")
data class RemyndEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "triggerAt")
    val triggerAt: Long,

    @ColumnInfo(name = "active")
    val active: Boolean,

    @ColumnInfo(name = "vibrate")
    val vibrate: Boolean,

    @ColumnInfo(name = "daysOfWeek")
    val daysOfWeek: String? = null,

    @ColumnInfo(name = "interval")
    val interval: Long? = null
) : Parcelable
