package com.rain.remynd.ui.list

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.rain.remynd.data.RemyndDao
import com.rain.remynd.data.RemyndEntity
import com.rain.remynd.support.formatTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Date

@Suppress("EXPERIMENTAL_API_USAGE")
class RemyndListPresenter(
    private val view: RemyndListView,
    private val remyndDao: RemyndDao
) : LifecycleObserver {
    private val tag = RemyndListPresenter::class.java.simpleName
    private val parentJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + parentJob)

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        Log.d(tag, "onCreate")
        observeItems()
        observeItemEvents()
    }

    private fun observeItemEvents() {
        scope.launch(Dispatchers.IO) {
            Log.d(tag, Thread.currentThread().name + ": observe switch events")
            view.itemEvents()
                .distinctUntilChanged()
                .debounce(300)
                .collect {
                    when (it) {
                        is ItemEvent.ClickEvent -> openItem(it.id)
                        is ItemEvent.SwitchEvent -> updateItem(it.id, it.active)
                    }
                }
        }
    }

    private fun openItem(id: Long) {
        Log.d(tag, Thread.currentThread().name + ": open $id")
        scope.launch(Dispatchers.Main) {
            // TODO: implement this
        }
    }

    private suspend fun updateItem(id: Long, active: Boolean) {
        Log.d(tag, Thread.currentThread().name + ": switching $id, $active")
        remyndDao.update(id, active)
    }

    private fun observeItems() {
        scope.launch(Dispatchers.IO) {
            Log.d(tag, Thread.currentThread().name + ": observe")
            remyndDao.observe()
                .map { toViewModels(it) }
                .collect {
                    Log.d(tag, Thread.currentThread().name + ": collect data")
                    scope.launch(Dispatchers.Main) {
                        Log.d(tag, Thread.currentThread().name + ": render data")
                        view.render(it)
                    }
                }
        }
    }

    private fun toViewModels(entities: List<RemyndEntity>): List<RemyndItemViewModel> {
        Log.d(tag, Thread.currentThread().name + ": toViewModels")
        return entities.map {
            val date = Date(it.triggerAt)
            RemyndItemViewModel(
                id = it.id,
                content = it.content,
                date = formatTime("EEE, dd MMM", date),
                clock = formatTime("a", date),
                time = formatTime("hh:mm", date),
                active = it.active
            )
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        Log.d(tag, "onDestroy")
        parentJob.cancel()
    }
}
