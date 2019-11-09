package com.rain.remynd.ui.list

import kotlinx.coroutines.flow.Flow

interface RemyndListView {
    fun clickEvents(): Flow<Int>
    fun switchEvents(): Flow<Pair<Int, Boolean>>
    fun render(items: List<RemyndItemViewModel>)
}
