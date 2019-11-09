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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
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
        val items = remyndDao.observe()
        scope.launch(Dispatchers.IO) {
            Log.d(tag, Thread.currentThread().name + ": observe")
            items.map { toViewModels(it) }
                .collect {
                    Log.d(tag, Thread.currentThread().name + ": collect data")
                    scope.launch(Dispatchers.Main) {
                        Log.d(tag, Thread.currentThread().name + ": render data")
                        view.render(it)
                    }
                }
        }
        scope.launch(Dispatchers.IO) {
            Log.d(tag, Thread.currentThread().name + ": observe switch events")
            view.switchEvents()
                .distinctUntilChanged()
                .debounce(300)
                .flatMapLatest { pair ->
                    val (pos, active) = pair
                    Log.d(tag, Thread.currentThread().name + ": flatMapLatest")
                    items.take(1)
                        .map { it.getOrNull(pos) }
                        .map { it?.copy(active = active) }
                }
                .collect {
                    if (it != null) {
                        Log.d(tag, Thread.currentThread().name + ": switching")
                        remyndDao.update(it)
                    } else {
                        Log.d(tag, Thread.currentThread().name + ": cannot switch")
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
