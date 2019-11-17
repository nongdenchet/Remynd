package com.rain.remynd.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import com.rain.remynd.R
import com.rain.remynd.data.RemyndEntity
import com.rain.remynd.ui.RemyndActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val entity: RemyndEntity = intent.getParcelableExtra(ENTITY) ?: return
        Log.d("AlarmReceiver", entity.toString())

        // Fire notification
        val pendingIntent = PendingIntent.getActivity(
            context,
            entity.id.toInt(),
            Intent(context, RemyndActivity::class.java),
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val notification = NotificationCompat.Builder(context, "channel")
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(entity.content)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .run { notify(entity.id.toInt(), notification) }

        // Update database job
        val jobIntent = Intent(context, AlarmIntentService::class.java)
            .apply { this.putExtra(ENTITY, entity) }

        JobIntentService.enqueueWork(
            context,
            AlarmIntentService::class.java,
            ALARM_JOB_ID,
            jobIntent
        )
    }
}
