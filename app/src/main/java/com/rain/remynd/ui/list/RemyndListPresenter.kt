package com.rain.remynd.ui.list

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.rain.remynd.R
import com.rain.remynd.data.RemyndDao
import com.rain.remynd.data.RemyndEntity
import com.rain.remynd.support.ResourcesProvider
import com.rain.remynd.support.formatTime
import com.rain.remynd.ui.RemyndNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Date

@Suppress("EXPERIMENTAL_API_USAGE")
class RemyndListPresenter(
    private val view: RemyndListView,
    private val remyndDao: RemyndDao,
    private val navigator: RemyndNavigator,
    private val resourcesProvider: ResourcesProvider
) : LifecycleObserver {
    private val tag = RemyndListPresenter::class.java.simpleName
    private val parentJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + parentJob)

    fun bind() {
        Log.d(tag, "bind")
        observeItems()
        observeAddClicks()
        observeItemEvents()
    }

    private fun observeAddClicks() {
        scope.launch(Dispatchers.Main) {
            view.addRemyndClicks()
                .debounce(300)
                .collect {
                    Log.d(tag, Thread.currentThread().name + ": showing form")
                    navigator.showRemyndForm()
                }
        }
    }

    private fun observeItemEvents() {
        scope.launch(Dispatchers.Main) {
            view.itemEvents()
                .debounce(300)
                .collect {
                    Log.d(tag, Thread.currentThread().name + ": observe switch events")
                    when (it) {
                        is ItemEvent.ClickEvent -> openItem(it.id)
                        is ItemEvent.SwitchEvent -> updateItem(it.id, it.active)
                    }
                }
        }
    }

    private fun openItem(id: Long) {
        Log.d(tag, Thread.currentThread().name + ": open $id")
        navigator.showRemyndDetails(id)
    }

    private fun updateItem(id: Long, active: Boolean) {
        scope.launch(Dispatchers.IO) {
            Log.d(tag, Thread.currentThread().name + ": switching $id, $active")
            remyndDao.update(id, active)
        }
    }

    private fun observeItems() {
        scope.launch(Dispatchers.IO) {
            Log.d(tag, Thread.currentThread().name + ": observe")
            remyndDao.observe()
                .map { toViewModels(it) }
                .collect {
                    Log.d(tag, Thread.currentThread().name + ": collect data")
                    val activeCount = formatActiveCount(it)
                    scope.launch(Dispatchers.Main) {
                        Log.d(tag, Thread.currentThread().name + ": render data")
                        view.render(it)
                        view.renderActiveCount(activeCount)
                    }
                }
        }
    }

    private fun formatActiveCount(items: List<RemyndItemViewModel>): String {
        val activeCount = items.count { it.active }

        return resourcesProvider.getString(R.string.active_count, activeCount)
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
    fun unbind() {
        Log.d(tag, "unbind")
        parentJob.cancel()
    }
}
