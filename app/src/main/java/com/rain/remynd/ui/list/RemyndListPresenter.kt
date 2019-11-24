package com.rain.remynd.ui.list

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.rain.remynd.R
import com.rain.remynd.alarm.bridge.AlarmScheduler
import com.rain.remynd.data.RemyndDao
import com.rain.remynd.data.RemyndEntity
import com.rain.remynd.common.RemindFormatUtils
import com.rain.remynd.common.ResourcesProvider
import com.rain.remynd.common.VibrateUtils
import com.rain.remynd.common.formatTime
import com.rain.remynd.common.indexToDate
import com.rain.remynd.alarm.toAlarm
import com.rain.remynd.navigator.Navigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import java.util.Date

private data class EditMode(val ids: Set<Long>)

@Suppress("EXPERIMENTAL_API_USAGE")
class RemyndListPresenter(
    private val view: RemyndListView,
    private val remyndDao: RemyndDao,
    private val navigator: Navigator,
    private val alarmScheduler: AlarmScheduler,
    private val resourcesProvider: ResourcesProvider,
    private val remindFormatUtils: RemindFormatUtils,
    private val vibrateUtils: VibrateUtils
) : LifecycleObserver {
    private val tag = RemyndListPresenter::class.java.simpleName
    private val parentJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + parentJob)
    private val editMode = BroadcastChannel<EditMode?>(CONFLATED)

    fun bind() {
        Log.d(tag, "bind")
        observeItems()
        observeClicks()
        observeItemEvents()
    }

    private fun observeClicks() {
        scope.launch(Dispatchers.Main) {
            view.addClicks()
                .debounce(300)
                .collect { navigator.showRemindForm() }
        }
        scope.launch(Dispatchers.Main) {
            view.introClicks()
                .debounce(300)
                .collect { navigator.showRemindForm() }
        }
        scope.launch(Dispatchers.Main) {
            view.removeClicks()
                .debounce(300)
                .collect {
                    Log.d(tag, Thread.currentThread().name + ": remove clicks")
                    val currentMode = editMode.asFlow().first() ?: return@collect
                    removeIDs(currentMode.ids)
                }
        }
    }

    private fun removeIDs(ids: Set<Long>) {
        scope.launch(Dispatchers.IO) {
            remyndDao.delete(ids.toList())
            editMode.send(null)
            alarmScheduler.cancel(ids)
        }
    }

    private fun observeItemEvents() {
        scope.launch(Dispatchers.Main) {
            view.itemEvents()
                .debounce(300)
                .collect {
                    Log.d(tag, Thread.currentThread().name + ": observe switch events")
                    when (it) {
                        is ItemEvent.ClickEvent -> handleClick(it.id)
                        is ItemEvent.SwitchEvent -> updateItem(it.id, it.active, it.position)
                        is ItemEvent.LongClickEvent -> handleLongClick(it.id)
                        is ItemEvent.CheckEvent -> handleCheckChange(it.id, it.value)
                    }
                }
        }
    }

    private fun handleCheckChange(id: Long, value: Boolean) {
        scope.launch(Dispatchers.Main) {
            val currentMode = editMode.asFlow().first() ?: return@launch
            val ids = if (value) {
                currentMode.ids + id
            } else {
                currentMode.ids - id
            }
            editMode.send(currentMode.copy(ids = ids))
        }
    }

    private fun handleLongClick(id: Long) {
        scope.launch(Dispatchers.Main) {
            Log.d(tag, Thread.currentThread().name + ": long click $id")
            val currentMode = editMode.asFlow().first()
            val newMode = currentMode ?: EditMode(setOf())
            editMode.send(newMode.copy(ids = newMode.ids + id))
            if (currentMode == null) {
                vibrateUtils.execute()
            }
        }
    }

    private fun handleClick(id: Long) {
        scope.launch(Dispatchers.Main) {
            Log.d(tag, Thread.currentThread().name + ": handleClick $id")
            val currentMode = editMode.asFlow().first()
            if (currentMode == null) {
                navigator.showRemindDetails(id)
            } else {
                val ids = if (currentMode.ids.contains(id)) {
                    currentMode.ids - id
                } else {
                    currentMode.ids + id
                }
                editMode.send(currentMode.copy(ids = ids))
            }
        }
    }

    private fun updateItem(id: Long, active: Boolean, position: Int) {
        scope.launch(Dispatchers.IO) {
            Log.d(tag, Thread.currentThread().name + ": switching $id, $active")
            var entity = remyndDao.get(id) ?: return@launch
            if (entity.active == active) {
                return@launch
            }

            if (!active) {
                alarmScheduler.cancel(entity.toAlarm())
                remyndDao.update(entity.copy(active = active))
                return@launch
            }

            val calendar = Calendar.getInstance().apply { timeInMillis = entity.triggerAt }
            val today = Calendar.getInstance()
            val daysOfWeek = entity.daysOfWeek

            // Find the next available date to schedule
            if (!daysOfWeek.isNullOrEmpty()) {
                val daySet = daysOfWeek.split(";")
                    .mapNotNull { it.toIntOrNull() }
                    .toSet()
                while (today >= calendar || !daySet.contains(calendar.get(Calendar.DAY_OF_WEEK))) {
                    calendar.add(Calendar.DATE, 1)
                }
                calendar.timeInMillis
            }

            if (today >= calendar) {
                scope.launch(Dispatchers.Main) {
                    view.showError(resourcesProvider.getString(R.string.time_past_error), position)
                }
                return@launch
            }

            entity = entity.copy(active = active, triggerAt = calendar.timeInMillis)
            alarmScheduler.schedule(entity.toAlarm())
            remyndDao.update(entity)
            scope.launch(Dispatchers.Main) {
                view.showMessage(remindFormatUtils.execute(entity.triggerAt))
            }
        }
    }

    private fun observeItems() {
        scope.launch(Dispatchers.Main) {
            editMode.send(null)
        }
        scope.launch(Dispatchers.IO) {
            Log.d(tag, Thread.currentThread().name + ": observe")
            remyndDao.observe()
                .combine(editMode.asFlow().distinctUntilChanged()) { entities, editMode ->
                    toViewModels(entities, editMode)
                }
                .collect { (viewModels, editMode) ->
                    Log.d(tag, Thread.currentThread().name + ": collect data")
                    val activeCount = formatActiveCount(viewModels)
                    scope.launch(Dispatchers.Main) {
                        Log.d(tag, Thread.currentThread().name + ": render data")
                        view.render(viewModels)
                        view.renderIntro(viewModels.isEmpty())
                        view.renderEditMode(editMode != null)
                        view.renderActiveCount(activeCount)
                    }
                }
        }
    }

    private fun formatActiveCount(items: List<RemyndItemViewModel>): String {
        val activeCount = items.count { it.active }

        return resourcesProvider.getString(R.string.active_count, activeCount)
    }

    private fun toViewModels(
        entities: List<RemyndEntity>,
        editMode: EditMode?
    ): Pair<List<RemyndItemViewModel>, EditMode?> {
        Log.d(tag, Thread.currentThread().name + ": toViewModels")
        val viewModels = entities.map {
            val date = Date(it.triggerAt)
            RemyndItemViewModel(
                id = it.id,
                content = it.content,
                date = formatDate(it.daysOfWeek, date),
                clock = formatTime("a", date),
                time = formatTime("hh:mm", date),
                active = it.active,
                isEditable = editMode != null,
                isChecked = editMode?.ids?.contains(it.id) == true
            )
        }
        return viewModels to editMode
    }

    private fun formatDate(daysOfWeek: String?, date: Date): String {
        return if (daysOfWeek.isNullOrEmpty()) {
            formatTime("EEE, dd MMM", date)
        } else {
            val days = daysOfWeek.split(";")
                .mapNotNull { it.toIntOrNull() }

            return if (days.size == 7) {
                resourcesProvider.getString(R.string.everyday)
            } else {
                days.map { indexToDate[it] }
                    .joinToString(", ")
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun unbind() {
        Log.d(tag, "unbind")
        parentJob.cancel()
    }

    fun onBackPressed(): Boolean {
        runBlocking { editMode.asFlow().first() } ?: return false

        return scope.launch(Dispatchers.Main) {
            editMode.send(null)
        }.let { true }
    }
}
