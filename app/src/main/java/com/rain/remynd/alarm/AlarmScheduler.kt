package com.rain.remynd.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.rain.remynd.data.RemyndEntity

interface AlarmScheduler {
    fun cancel(entity: RemyndEntity)
    fun schedule(entity: RemyndEntity)
}

@Suppress("MemberVisibilityCanBePrivate")
class MockAlarmScheduler : AlarmScheduler {
    var cancelEntity: RemyndEntity? = null
    var scheduleEntity: RemyndEntity? = null

    override fun cancel(entity: RemyndEntity) {
        cancelEntity = entity
    }

    override fun schedule(entity: RemyndEntity) {
        scheduleEntity = entity
    }
}

internal const val MESSAGE = "MESSAGE"
internal const val ID = "ID"

class AlarmSchedulerImpl(private val context: Context) : AlarmScheduler {
    private val alarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    private fun toPendingIntent(entity: RemyndEntity): PendingIntent {
        return Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.putExtra(MESSAGE, entity.content)
            intent.putExtra(ID, entity.id)
            PendingIntent.getBroadcast(
                context,
                entity.id.toInt(),
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
        }
    }

    override fun cancel(entity: RemyndEntity) {
        alarmManager.cancel(toPendingIntent(entity))
    }

    override fun schedule(entity: RemyndEntity) {
        alarmManager.cancel(toPendingIntent(entity))
        alarmManager.set(AlarmManager.RTC_WAKEUP, entity.triggerAt, toPendingIntent(entity))
    }
}
