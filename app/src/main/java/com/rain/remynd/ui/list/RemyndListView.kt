package com.rain.remynd.ui.list

import kotlinx.coroutines.flow.Flow

interface RemyndListView {
    fun itemEvents(): Flow<ItemEvent>
    fun render(items: List<RemyndItemViewModel>)
    fun renderActiveCount(value: String)
}
