package com.rain.remynd.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

interface AlarmScheduler {
    fun cancel(ids: Set<Long>)
    fun cancel(input: AlarmInput)
    fun schedule(input: AlarmInput)
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
        return prepareIntent(input.id) { intent ->
            intent.putExtra(MESSAGE, input.content)
            intent.putExtra(ID, input.id)
            intent.putExtra(VIBRATE, input.vibrate)
            intent.putExtra(INTERVAL, input.interval ?: 0)
            intent.putExtra(TYPE, ReceiverType.ALARM.name)
        }
    }

    private fun prepareIntent(id: Long, factory: (Intent) -> Intent): PendingIntent {
        return Intent(context, AlarmReceiver::class.java)
            .let(factory)
            .let { intent ->
                PendingIntent.getBroadcast(
                    context,
                    id.toInt(),
                    intent,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
            }
    }

    override fun cancel(ids: Set<Long>) {
        ids.forEach { id ->
            alarmManager.cancel(prepareIntent(id) { it })
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
