package com.rain.remynd.alarm

import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.rain.remynd.alarm.bridge.AlarmScheduler
import com.rain.remynd.common.dependency
import com.rain.remynd.data.RemyndDao
import com.rain.remynd.data.RemyndEntity
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import javax.inject.Inject

internal const val ALARM_JOB_ID = 1000

class AlarmIntentService : JobIntentService() {
    @Inject
    internal lateinit var remyndDao: RemyndDao
    @Inject
    internal lateinit var alarmScheduler: AlarmScheduler

    companion object {
        val tag: String = AlarmIntentService::class.java.simpleName
    }

    override fun onCreate() {
        super.onCreate()
        applicationContext.dependency(AlarmComponent::class)
            .inject(this)
    }

    override fun onHandleWork(intent: Intent) {
        runBlocking {
            val id = intent.getLongExtra(ID, -1L)
            if (id == -1L) {
                Log.d(tag, "ID is missing")
                return@runBlocking
            }

            val entity: RemyndEntity? = remyndDao.get(id)
            if (entity == null) {
                Log.d(tag, "Not found item: $id")
                return@runBlocking
            }

            Log.d(tag, "${Thread.currentThread().name}: onHandleWork $entity")
            val daysOfWeek = entity.daysOfWeek
            if (daysOfWeek.isNullOrEmpty()) {
                remyndDao.update(entity.copy(active = false))
            } else {
                val time = Calendar.getInstance().apply { timeInMillis = entity.triggerAt }
                val daySet = daysOfWeek
                    .split(";")
                    .mapNotNull { it.toIntOrNull() }
                    .toSet()

                val today = Calendar.getInstance()
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, time.get(Calendar.MINUTE))
                }
                while (today >= calendar || !daySet.contains(calendar.get(Calendar.DAY_OF_WEEK))) {
                    calendar.add(Calendar.DATE, 1)
                }
                calendar.timeInMillis
                alarmScheduler.schedule(entity.toAlarm())
                remyndDao.update(entity.copy(triggerAt = calendar.timeInMillis))
            }
        }
    }
}
