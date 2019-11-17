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

internal const val ENTITY = "ENTITY"

class AlarmSchedulerImpl(private val context: Context) :
    AlarmScheduler {
    private val alarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    private fun toPendingIntent(entity: RemyndEntity): PendingIntent {
        return Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.putExtra(ENTITY, entity)
            PendingIntent.getBroadcast(context, entity.id.toInt(), intent, 0)
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
