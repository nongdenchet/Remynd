package com.rain.remynd.ui.details

import androidx.lifecycle.LifecycleObserver
import com.rain.remynd.data.RemyndDao
import com.rain.remynd.support.ResourcesProvider

class RemyndDetailsPresenter(
    view: RemyndDetailsView,
    remyndDao: RemyndDao,
    resourcesProvider: ResourcesProvider
) : LifecycleObserver {
    fun bind(id: Long?) {
        // TODO: implement this
    }

    fun unbind() {
        // TODO: implement this
    }
}
