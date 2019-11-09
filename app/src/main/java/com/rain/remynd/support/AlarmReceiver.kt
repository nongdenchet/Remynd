package com.rain.remynd.support

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("Quan", intent.getStringExtra("message") ?: "lah")
    }
}
