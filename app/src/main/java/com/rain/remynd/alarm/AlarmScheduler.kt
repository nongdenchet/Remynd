package com.rain.remynd.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

interface AlarmScheduler {
    fun cancel(input: AlarmInput)
    fun schedule(input: AlarmInput)
}

@Suppress("MemberVisibilityCanBePrivate")
class MockAlarmScheduler : AlarmScheduler {
    var cancel: AlarmInput? = null
    var schedule: AlarmInput? = null

    override fun cancel(input: AlarmInput) {
        cancel = input
    }

    override fun schedule(input: AlarmInput) {
        schedule = input
    }
}

internal const val MESSAGE = "MESSAGE"
internal const val VIBRATE = "VIBRATE"
internal const val INTERVAL = "INTERVAL"
internal const val ID = "ID"

data class AlarmInput(
    val id: Long,
    val content: String,
    val triggerAt: Long,
    val vibrate: Boolean,
    val interval: Long?
)

class AlarmSchedulerImpl(private val context: Context) : AlarmScheduler {
    private val alarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    private fun toPendingIntent(input: AlarmInput): PendingIntent {
        return Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.putExtra(MESSAGE, input.content)
            intent.putExtra(ID, input.id)
            intent.putExtra(VIBRATE, input.vibrate)
            intent.putExtra(INTERVAL, input.interval ?: 0)
            intent.putExtra(TYPE, ReceiverType.ALARM.name)
            PendingIntent.getBroadcast(
                context,
                input.id.toInt(),
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
        }
    }

    override fun cancel(input: AlarmInput) {
        alarmManager.cancel(toPendingIntent(input))
    }

    override fun schedule(input: AlarmInput) {
        alarmManager.cancel(toPendingIntent(input))
        alarmManager.set(AlarmManager.RTC_WAKEUP, input.triggerAt, toPendingIntent(input))
    }
}
