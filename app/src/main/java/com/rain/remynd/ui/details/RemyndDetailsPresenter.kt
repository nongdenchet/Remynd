package com.rain.remynd.ui.details

import android.util.Log
import androidx.lifecycle.LifecycleObserver
import com.rain.remynd.data.RemyndDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Calendar

@Suppress("EXPERIMENTAL_API_USAGE")
class RemyndDetailsPresenter(
    private val view: RemyndDetailsView,
    private val remyndDao: RemyndDao
) : LifecycleObserver {
    private val tag = RemyndDetailsPresenter::class.java.simpleName
    private val reducer = RemyndReducer()
    private val parentJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + parentJob)
    private val form = BroadcastChannel<RemyndForm>(Channel.CONFLATED)
    private val actions = BroadcastChannel<RemyndFormAction>(1)

    fun bind(id: Long?, state: RemyndForm?) {
        Log.d(tag, "bind")
        scope.launch(Dispatchers.IO) {
            val init = initForm(id, state)
            scope.launch(Dispatchers.Main) {
                actions.asFlow()
                    .distinctUntilChanged()
                    .scan(init) { prev, action ->
                        Log.d(tag, "${Thread.currentThread().name} reducing: $action")
                        reducer.reduce(prev, action)
                    }
                    .collect {
                        Log.d(tag, "${Thread.currentThread().name} send form: $it")
                        form.send(it)
                    }
            }
        }
        scope.launch(Dispatchers.Main) {
            form.asFlow()
                .distinctUntilChanged()
                .collect {
                    Log.d(tag, "${Thread.currentThread().name} render: $it")
                    view.render(it)
                }
        }
    }

    fun setDate(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        Log.d(tag, "Date picked: $year, $monthOfYear, $dayOfMonth")
        scope.launch(Dispatchers.Main) {
            actions.send(RemyndFormAction.UpdateDate(year, monthOfYear, dayOfMonth))
        }
    }

    fun setContent(content: String) {
        Log.d(tag, "Set content: $content")
        scope.launch(Dispatchers.Main) {
            actions.send(RemyndFormAction.UpdateContent(content))
        }
    }

    fun setVibrate(checked: Boolean) {
        Log.d(tag, "Set vibrate: $checked")
        scope.launch(Dispatchers.Main) {
            actions.send(RemyndFormAction.UpdateVibrate(checked))
        }
    }

    fun setTime(hourOfDay: Int, minute: Int) {
        Log.d(tag, "Time picked: $hourOfDay, $minute")
        scope.launch(Dispatchers.Main) {
            actions.send(RemyndFormAction.UpdateTime(hourOfDay, minute))
        }
    }

    fun currentForm(): RemyndForm {
        return runBlocking { form.asFlow().first() }
    }

    private suspend fun initForm(id: Long?, state: RemyndForm?): RemyndForm {
        Log.d(tag, "${Thread.currentThread().name} initForm: $id, $state")
        if (state != null) {
            return state
        }

        if (id != null) {
            return restoreForm(id) ?: defaultForm()
        }

        return defaultForm()
    }

    private suspend fun restoreForm(id: Long): RemyndForm? {
        val entity = remyndDao.get(id)
        if (entity != null) {
            val time = Calendar.getInstance()
            time.timeInMillis = entity.triggerAt
            return RemyndForm(
                content = entity.content,
                dateConfig = DateConfig.SingleDate(time),
                enabled = entity.active,
                vibrate = false,
                interval = entity.interval
            )
        }
        return null
    }

    private fun defaultForm(): RemyndForm {
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DATE, 1)
            set(Calendar.HOUR_OF_DAY, 19)
            set(Calendar.MINUTE, 30)
        }
        return RemyndForm(
            content = "",
            dateConfig = DateConfig.SingleDate(tomorrow),
            enabled = true,
            vibrate = false,
            interval = null
        )
    }

    fun unbind() {
        Log.d(tag, "unbind")
        parentJob.cancel()
    }
}
