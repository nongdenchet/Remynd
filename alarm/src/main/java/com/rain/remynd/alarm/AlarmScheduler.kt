package com.rain.remynd.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.rain.remynd.alarm.bridge.Alarm
import com.rain.remynd.alarm.bridge.AlarmScheduler

class AlarmSchedulerImpl(private val context: Context) : AlarmScheduler {
    private val alarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    private fun toPendingIntent(alarm: Alarm): PendingIntent {
        return prepareIntent(alarm.id) { intent ->
            intent.putExtra(MESSAGE, alarm.content)
            intent.putExtra(ID, alarm.id)
            intent.putExtra(VIBRATE, alarm.vibrate)
            intent.putExtra(INTERVAL, alarm.interval ?: 0)
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

    override fun cancel(alarm: Alarm) {
        alarmManager.cancel(toPendingIntent(alarm))
    }

    override fun schedule(alarm: Alarm) {
        alarmManager.cancel(toPendingIntent(alarm))
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarm.triggerAt, toPendingIntent(alarm))
    }
}
