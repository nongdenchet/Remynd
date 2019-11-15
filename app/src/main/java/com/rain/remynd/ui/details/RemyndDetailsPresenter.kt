package com.rain.remynd.ui.details

import android.util.Log
import androidx.lifecycle.LifecycleObserver
import com.rain.remynd.data.RemyndDao
import com.rain.remynd.support.ResourcesProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

@Suppress("EXPERIMENTAL_API_USAGE")
class RemyndDetailsPresenter(
    private val view: RemyndDetailsView,
    private val remyndDao: RemyndDao,
    private val resourcesProvider: ResourcesProvider
) : LifecycleObserver {
    private val tag = RemyndDetailsPresenter::class.java.simpleName
    private val parentJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + parentJob)

    fun bind(id: Long?) {
        Log.d(tag, "bind")
    }
    fun unbind() {
        Log.d(tag, "unbind")
        parentJob.cancel()
    }
}
