package com.rain.remynd.ui

import com.rain.remynd.data.RemyndDao

interface RemyndListDependency {
    fun remyndDao(): RemyndDao
}
