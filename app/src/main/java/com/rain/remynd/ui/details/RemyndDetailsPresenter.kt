package com.rain.remynd.ui.details

import android.util.Log
import androidx.lifecycle.LifecycleObserver
import com.rain.remynd.R
import com.rain.remynd.data.RemyndDao
import com.rain.remynd.alarm.AlarmScheduler
import com.rain.remynd.support.ResourcesProvider
import com.rain.remynd.support.toAlarm
import com.rain.remynd.view.DateItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Calendar

@Suppress("EXPERIMENTAL_API_USAGE")
class RemyndDetailsPresenter(
    private val view: RemyndDetailsView,
    private val remyndDao: RemyndDao,
    private val alarmScheduler: AlarmScheduler,
    private val resourcesProvider: ResourcesProvider
) : LifecycleObserver {
    private val reducer = RemyndReducer()
    private val mapper = RemyndDetailsViewModelMapper()
    private val parentJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + parentJob)
    private val form = BroadcastChannel<RemyndForm>(Channel.CONFLATED)
    private val actions = BroadcastChannel<RemyndFormAction>(1)

    companion object {
        private val tag = RemyndDetailsPresenter::class.java.simpleName
    }

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
                .map { mapper.toViewModel(it) }
                .collect {
                    Log.d(tag, "${Thread.currentThread().name} render: $it")
                    view.render(it)
                }
        }
        scope.launch(Dispatchers.Main) {
            view.contentChanges()
                .debounce(300)
                .collect { setContent(it) }
        }
    }

    fun setDate(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        Log.d(tag, "Date picked: $year, $monthOfYear, $dayOfMonth")
        scope.launch(Dispatchers.Main) {
            actions.send(RemyndFormAction.UpdateDate(year, monthOfYear, dayOfMonth))
        }
    }

    private fun setContent(content: String) {
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

    fun setEnabled(checked: Boolean) {
        Log.d(tag, "Set enabled: $checked")
        scope.launch(Dispatchers.Main) {
            actions.send(RemyndFormAction.UpdateEnabled(checked))
        }
    }

    fun setTime(hourOfDay: Int, minute: Int) {
        Log.d(tag, "Time picked: $hourOfDay, $minute")
        scope.launch(Dispatchers.Main) {
            actions.send(RemyndFormAction.UpdateTime(hourOfDay, minute))
        }
    }

    fun setDateItems(items: List<DateItem>) {
        Log.d(tag, "Date items picked: $items")
        scope.launch(Dispatchers.Main) {
            actions.send(RemyndFormAction.UpdateItems(items))
        }
    }

    fun setInterval(duration: Long?) {
        Log.d(tag, "Interval picked: $duration")
        scope.launch(Dispatchers.Main) {
            actions.send(RemyndFormAction.UpdateInterval(duration))
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

            val dateConfig = if (!entity.daysOfWeek.isNullOrEmpty()) {
                val daysOfWeek = entity.daysOfWeek
                    .split(";")
                    .mapNotNull { it.toIntOrNull() }

                DateConfig.RepeatDate(
                    hour = time.get(Calendar.HOUR_OF_DAY),
                    minute = time.get(Calendar.MINUTE),
                    daysOfWeek = daysOfWeek
                )
            } else {
                DateConfig.SingleDate(time)
            }

            return RemyndForm(
                id = entity.id,
                contentInfo = ContentInfo(false, entity.content),
                dateConfig = dateConfig,
                enabled = entity.active,
                vibrate = entity.vibrate,
                interval = entity.interval
            )
        }
        return null
    }

    private fun defaultForm(): RemyndForm {
        val time = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 19)
            set(Calendar.MINUTE, 30)
            if (this < Calendar.getInstance()) {
                add(Calendar.DATE, 1)
            }
        }
        return RemyndForm(
            id = null,
            contentInfo = ContentInfo(false, ""),
            dateConfig = DateConfig.SingleDate(time),
            enabled = true,
            vibrate = false,
            interval = null
        )
    }

    fun unbind() {
        Log.d(tag, "unbind")
        parentJob.cancel()
    }

    fun save() {
        Log.d(tag, "save")
        scope.launch(Dispatchers.IO) {
            val form = form.asFlow().first()
            val entity = mapper.toEntity(form)

            if (entity.triggerAt < Calendar.getInstance().timeInMillis) {
                scope.launch(Dispatchers.Main) {
                    view.showError(resourcesProvider.getString(R.string.time_past_error))
                }
                return@launch
            }

            if (entity.content.isBlank()) {
                scope.launch(Dispatchers.Main) {
                    view.showError(resourcesProvider.getString(R.string.content_empty))
                }
                return@launch
            }

            // Update DB
            if (entity.id != 0L) remyndDao.update(entity)
            else remyndDao.insert(entity)

            // Schedule alarm
            if (entity.active) alarmScheduler.schedule(entity.toAlarm())
            else alarmScheduler.cancel(entity.toAlarm())

            // Exit Fragment
            scope.launch(Dispatchers.Main) { view.goBack() }
        }
    }
}
