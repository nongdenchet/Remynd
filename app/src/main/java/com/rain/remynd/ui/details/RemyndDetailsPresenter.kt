package com.rain.remynd.ui.details

import androidx.lifecycle.LifecycleObserver
import com.rain.remynd.data.RemyndDao
import com.rain.remynd.support.ResourcesProvider

class RemyndDetailsPresenter(
    view: RemyndDetailsView,
    remyndDao: RemyndDao,
    resourcesProvider: ResourcesProvider
) : LifecycleObserver {
    fun generateForm(): RemyndDetailsForm {
        TODO("Implement this")
    }

    fun bind(form: RemyndDetailsForm?) {
        TODO("Implement this")
    }

    fun unbind() {
        TODO("Implement this")
    }
}
