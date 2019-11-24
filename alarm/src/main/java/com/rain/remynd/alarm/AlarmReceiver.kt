package com.rain.remynd.alarm

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rain.remynd.alarm.bridge.Alarm
import com.rain.remynd.alarm.bridge.AlarmScheduler
import com.rain.remynd.common.dependency
import com.rain.remynd.common.formatDuration
import java.util.Calendar
import kotlin.reflect.KClass

enum class ReceiverType {
    ALARM, REPEAT, DISMISS, UNKNOWN;

    companion object {
        fun getByVal(value: String?): ReceiverType {
            return values().firstOrNull { it.name == value } ?: UNKNOWN
        }
    }
}

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        val tag: String = AlarmReceiver::class.java.simpleName
    }

    override fun onReceive(context: Context, intent: Intent) {
        val rawType = intent.getStringExtra(TYPE)
        when (ReceiverType.getByVal(rawType)) {
            ReceiverType.ALARM -> launchAlarm(context, intent)
            ReceiverType.REPEAT -> scheduleAlarm(context, intent)
            ReceiverType.DISMISS -> dismissAlarm(context, intent)
            ReceiverType.UNKNOWN -> Log.d(tag, "Unknown type: $rawType")
        }
    }

    private fun dismissAlarm(context: Context, intent: Intent) {
        val id = intent.getLongExtra(ID, -1L)
        if (id == -1L) {
            Log.d(tag, "ID is missing for scheduleAlarm")
            return
        }

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(id.toInt())
    }

    private fun scheduleAlarm(context: Context, intent: Intent) {
        val message: String? = intent.getStringExtra(MESSAGE)
        if (message == null) {
            Log.d(tag, "Message is null for scheduleAlarm")
            return
        }

        val id = intent.getLongExtra(ID, -1L)
        if (id == -1L) {
            Log.d(tag, "ID is missing for scheduleAlarm")
            return
        }
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(id.toInt())

        val interval = intent.getLongExtra(INTERVAL, 0)
        if (interval <= 0) {
            Log.d(tag, "interval invalid for scheduleAlarm")
            return
        }

        val vibrate = intent.getBooleanExtra(VIBRATE, false)
        val scheduler = getAlarmScheduler(context)
        val time = Calendar.getInstance().apply {
            add(Calendar.MILLISECOND, interval.toInt())
        }
        scheduler.schedule(Alarm(id, message, time.timeInMillis, vibrate, interval))
    }

    private fun launchAlarm(context: Context, intent: Intent) {
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

        val interval = intent.getLongExtra(INTERVAL, 0)
        val vibrate = intent.getBooleanExtra(VIBRATE, false)

        val pendingIntent = PendingIntent.getActivity(
            context,
            id.toInt(),
            Intent(context, getTargetActivity(context).java),
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.alarm_reminder))
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_calendar)
            .setContentIntent(pendingIntent)
            .setLights(Color.RED, 3000, 3000)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
        if (vibrate) {
            builder.setVibrate(LongArray(3) { 1000 })
        }
        if (interval > 0) {
            builder.addAction(
                0, context.getString(R.string.alarm_dismiss),
                dismissPendingIntent(context, id)
            )
            builder.addAction(
                0, context.getString(R.string.alarm_resend, formatDuration(interval)),
                repeatPendingIntent(context, id, message, vibrate, interval)
            )
        }

        createNotificationChannel(context)
        with(NotificationManagerCompat.from(context)) {
            Log.d(tag, "Sending notification: $id, $message")
            notify(id.toInt(), builder.build())
        }
        fireIntentService(context, id)
    }

    private fun fireIntentService(context: Context, id: Long) {
        val jobIntent = Intent(context, AlarmIntentService::class.java)
            .apply { this.putExtra(ID, id) }

        JobIntentService.enqueueWork(
            context,
            AlarmIntentService::class.java,
            ALARM_JOB_ID,
            jobIntent
        )
    }

    private fun dismissPendingIntent(context: Context, id: Long): PendingIntent {
        return Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.putExtra(ID, id)
            intent.putExtra(TYPE, ReceiverType.DISMISS.name)
            intent.action = DISMISS
            PendingIntent.getBroadcast(
                context,
                id.toInt(),
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
        }
    }

    private fun repeatPendingIntent(
        context: Context,
        id: Long,
        message: String,
        vibrate: Boolean,
        interval: Long
    ): PendingIntent {
        return Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.putExtra(MESSAGE, message)
            intent.putExtra(ID, id)
            intent.putExtra(VIBRATE, vibrate)
            intent.putExtra(INTERVAL, interval)
            intent.putExtra(TYPE, ReceiverType.REPEAT.name)
            intent.action = REPEAT
            PendingIntent.getBroadcast(
                context,
                id.toInt(),
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
        }
    }

    private fun getAlarmScheduler(context: Context): AlarmScheduler {
        return context.applicationContext
            .dependency(AlarmComponent::class)
            .alarmScheduler()
    }

    private fun getTargetActivity(context: Context): KClass<out Activity> {
        return context.applicationContext
            .dependency(AlarmComponent::class)
            .targetActivity()
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val name = context.getString(R.string.alarm_channel_name)
            val descriptionText = context.getString(R.string.alarm_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            nm.createNotificationChannel(channel)
        }
    }
}
