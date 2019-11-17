@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.rain.remynd.support

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import com.rain.remynd.view.DateItem
import com.rain.remynd.view.WeekDatePicker
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Observe stream clicks from [View]
 */
fun View.clicks(): Flow<Unit> = callbackFlow {
    this@clicks.setOnClickListener {
        this.offer(Unit)
    }
    awaitClose { this@clicks.setOnClickListener(null) }
}

/**
 * Observe stream dates from [WeekDatePicker]
 */
fun WeekDatePicker.observe(): Flow<List<DateItem>> = callbackFlow {
    offer(getData())
    this@observe.setOnDataChangeListener {
        this.offer(it)
    }
    awaitClose { this@observe.setOnDataChangeListener(null) }
}

/**
 * Observe checked changes
 */
fun Switch.observe(): Flow<Boolean> = callbackFlow {
    offer(isChecked)
    this@observe.setOnCheckedChangeListener { _, isChecked ->
        this.offer(isChecked)
    }
    awaitClose { this@observe.setOnCheckedChangeListener(null) }
}

/**
 * Observe checked changes
 */
fun EditText.text(): Flow<String> = callbackFlow {
    offer(this@text.text.toString())
    val listener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            offer(s?.toString() ?: "")
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
    }
    this@text.addTextChangedListener(listener)
    awaitClose { this@text.removeTextChangedListener(listener) }
}
