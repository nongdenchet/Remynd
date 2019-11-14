package com.rain.remynd.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentFactory
import com.rain.remynd.R
import com.rain.remynd.data.RemyndDao
import com.rain.remynd.data.RemyndEntity
import com.rain.remynd.support.AlarmReceiver
import com.rain.remynd.support.dependency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RemyndActivity : AppCompatActivity() {
    private val parentJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + parentJob)
    private val tag = RemyndActivity::class.java.simpleName

    @Inject
    internal lateinit var remyndDao: RemyndDao
    @Inject
    internal lateinit var fragmentFactory: FragmentFactory
    @Inject
    internal lateinit var navigator: RemyndNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        setUpDependency()
        supportFragmentManager.fragmentFactory = fragmentFactory
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remynd)
        if (savedInstanceState == null) {
            initialize()
        }
    }

    private fun initialize() {
        navigator.showRemyndList()
        scope.launch(Dispatchers.IO) {
            val count = remyndDao.count()
            if (count == 0) {
                Log.d(tag, Thread.currentThread().name + ": inserting")
                remyndDao.insert(
                    RemyndEntity(
                        content = "Drink Water",
                        triggerAt = Date().time,
                        active = false
                    )
                )
                remyndDao.insert(
                    RemyndEntity(
                        content = "Test Code",
                        triggerAt = Date().time + TimeUnit.DAYS.toMillis(1),
                        active = true
                    )
                )
            } else {
                Log.d(tag, Thread.currentThread().name + ": $count")
            }
        }
    }

    private fun setUpDependency() {
        DaggerRemyndComponent.factory()
            .create(this, applicationContext.dependency(RemyndDependency::class))
            .inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        parentJob.cancel()
    }

    @Suppress("unused")
    private fun testAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmIntent1 = Intent(this, AlarmReceiver::class.java).let { intent ->
            intent.putExtra("message", "1")
            PendingIntent.getBroadcast(this, 1, intent, 0)
        }
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            SystemClock.elapsedRealtime() + 60 * 1000,
            60 * 1000,
            alarmIntent1
        )
        alarmManager.cancel(alarmIntent1)

        val alarmIntent2 = Intent(this, AlarmReceiver::class.java).let { intent ->
            intent.putExtra("message", "2")
            PendingIntent.getBroadcast(this, 2, intent, 0)
        }
        val calendar: Calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 35)
            if (this.timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            alarmIntent2
        )
    }
}
