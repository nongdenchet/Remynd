package com.rain.remynd.ui.list

import kotlinx.coroutines.flow.Flow

interface RemyndListView {
    fun itemEvents(): Flow<ItemEvent>
    fun addRemyndClicks(): Flow<Unit>
    fun render(items: List<RemyndItemViewModel>)
    fun renderActiveCount(value: String)
    fun showError(content: String, position: Int)
}
