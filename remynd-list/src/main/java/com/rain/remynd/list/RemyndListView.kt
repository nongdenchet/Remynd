package com.rain.remynd.list

import kotlinx.coroutines.flow.Flow

interface RemyndListView {
    fun itemEvents(): Flow<ItemEvent>
    fun addClicks(): Flow<Unit>
    fun introClicks(): Flow<Unit>
    fun removeClicks(): Flow<Unit>
    fun render(items: List<RemyndItemViewModel>)
    fun renderEditMode(value: Boolean)
    fun renderActiveCount(value: String)
    fun showError(content: String, position: Int)
    fun showMessage(content: String)
    fun renderIntro(value: Boolean)
}
