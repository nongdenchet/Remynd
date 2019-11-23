@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.rain.remynd.common

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
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
 * Observe checked changes from [EditText]
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
