package com.rain.remynd.ui.list

data class RemyndItemViewModel(
    val id: Long,
    val time: String,
    val date: String,
    val clock: String,
    val content: String,
    val active: Boolean,
    val isEditable: Boolean,
    val isChecked: Boolean
)
