package com.rain.remynd.ui.details

import android.util.Log
import androidx.lifecycle.LifecycleObserver
import com.rain.remynd.data.RemyndDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Calendar

@Suppress("EXPERIMENTAL_API_USAGE")
class RemyndDetailsPresenter(
    private val view: RemyndDetailsView,
    private val remyndDao: RemyndDao
) : LifecycleObserver {
    private val tag = RemyndDetailsPresenter::class.java.simpleName
    private val parentJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + parentJob)
    private val form = BroadcastChannel<RemyndForm>(Channel.CONFLATED)

    fun bind(id: Long?, state: RemyndForm?) {
        Log.d(tag, "bind")
        scope.launch(Dispatchers.IO) {
            form.send(initForm(id, state))
        }
        scope.launch(Dispatchers.Main) {
            form.consumeEach { view.render(it) }
        }
    }

    fun setDate(year: Int, monthOfYear: Int, dayOfMonth: Int) {

        Log.d(tag, "Date picked: $year, $monthOfYear, $dayOfMonth")
        scope.launch(Dispatchers.Main) {
            form.asFlow()
                .take(1)
                .collect { curr ->
                    val dateConfig = (curr.dateConfig as DateConfig.SingleDate).let {
                        val newDate = Calendar.getInstance()
                        newDate.timeInMillis = it.date.timeInMillis
                        newDate.set(Calendar.YEAR, year)
                        newDate.set(Calendar.MONTH, monthOfYear)
                        newDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        DateConfig.SingleDate(newDate)
                    }
                    form.send(curr.copy(dateConfig = dateConfig))
                }
        }
    }

    fun setTime(hourOfDay: Int, minute: Int) {
        Log.d(tag, "Time picked: $hourOfDay, $minute")
        scope.launch(Dispatchers.Main) {
            form.asFlow()
                .take(1)
                .collect { curr ->
                    val dateConfig = (curr.dateConfig as DateConfig.SingleDate).let {
                        val newDate = Calendar.getInstance()
                        newDate.timeInMillis = it.date.timeInMillis
                        newDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        newDate.set(Calendar.MINUTE, minute)
                        DateConfig.SingleDate(newDate)
                    }
                    form.send(curr.copy(dateConfig = dateConfig))
                }
        }
    }

    fun currentForm(): RemyndForm {
        return runBlocking { form.asFlow().first() }
    }

    private suspend fun initForm(id: Long?, state: RemyndForm?): RemyndForm {
        if (state != null) {
            return state
        }

        if (id == null) {
            val tomorrow = Calendar.getInstance()
            tomorrow.add(Calendar.DATE, 1)
            tomorrow.set(Calendar.HOUR_OF_DAY, 19)
            tomorrow.set(Calendar.MINUTE, 30)
            return RemyndForm(
                content = "",
                dateConfig = DateConfig.SingleDate(tomorrow),
                enabled = true,
                vibrate = false,
                interval = null
            )
        }

        return remyndDao.get(id).let {
            val time = Calendar.getInstance()
            time.timeInMillis = it.triggerAt
            RemyndForm(
                content = it.content,
                dateConfig = DateConfig.SingleDate(time),
                enabled = it.active,
                vibrate = false,
                interval = it.interval
            )
        }
    }

    fun unbind() {
        Log.d(tag, "unbind")
        parentJob.cancel()
    }
}
