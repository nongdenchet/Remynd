package com.rain.remynd.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService

class BootReceiver : BroadcastReceiver() {
    companion object {
        val tag: String = BootReceiver::class.java.simpleName
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            Log.d(tag, "${Thread.currentThread().name}: onReceive boot ${intent.action}")
            return
        }

        Log.d(tag, "${Thread.currentThread().name}: onReceive boot launch")
        JobIntentService.enqueueWork(
            context,
            BootIntentService::class.java,
            BOOT_JOB_ID,
            Intent(context, BootIntentService::class.java)
        )
    }
}
