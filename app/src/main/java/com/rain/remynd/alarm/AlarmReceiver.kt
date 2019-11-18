package com.rain.remynd.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rain.remynd.R
import com.rain.remynd.ui.RemyndActivity

internal const val CHANNEL_ID = "CHANNEL_ID"

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        val tag: String = AlarmReceiver::class.java.simpleName
    }

    override fun onReceive(context: Context, intent: Intent) {
        val message: String? = intent.getStringExtra(MESSAGE)
        if (message == null) {
            Log.d(tag, "Message is null")
            return
        }

        val id = intent.getLongExtra(ID, -1L)
        if (id == -1L) {
            Log.d(tag, "ID is missing")
            return
        }

        // Fire notification
        val pendingIntent = PendingIntent.getActivity(
            context,
            id.toInt(),
            Intent(context, RemyndActivity::class.java),
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_calendar)
            .setContentIntent(pendingIntent)
            .build()

        createNotificationChannel(context)
        with(NotificationManagerCompat.from(context)) {
            Log.d(tag, "Sending notification: $id, $message")
            notify(id.toInt(), notification)
        }

        // Update database job
        val jobIntent = Intent(context, AlarmIntentService::class.java)
            .apply { this.putExtra(ID, id) }

        JobIntentService.enqueueWork(
            context,
            AlarmIntentService::class.java,
            ALARM_JOB_ID,
            jobIntent
        )
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            nm.createNotificationChannel(channel)
        }
    }
}
