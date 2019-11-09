package com.rain.remynd.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import com.rain.remynd.support.AlarmReceiver
import com.rain.remynd.R
import java.util.Calendar

class RemyndActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remynd)
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
