package com.rain.remynd.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RemyndEntity::class], version = 1)
abstract class RemyndDB : RoomDatabase() {
    abstract fun dao(): RemyndDao
}
