package com.rain.remynd.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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

    @ColumnInfo(name = "interval")
    val interval: Long? = null
)
