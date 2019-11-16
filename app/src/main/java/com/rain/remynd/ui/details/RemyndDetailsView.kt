package com.rain.remynd.ui.details

import com.rain.remynd.view.DateItem
import kotlinx.coroutines.flow.Flow

interface RemyndDetailsView {
    fun observeDates(): Flow<List<DateItem>>
    fun render(form: RemyndForm)
}
