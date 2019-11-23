package com.rain.remynd.common

import android.content.res.Resources
import androidx.annotation.StringRes

interface ResourcesProvider {
    fun getString(@StringRes id: Int, vararg args: Any): String
    fun getString(@StringRes id: Int): String
}

class ResourcesProviderImpl(private val resources: Resources) : ResourcesProvider {
    override fun getString(@StringRes id: Int, vararg args: Any): String =
        resources.getString(id, *args)

    override fun getString(@StringRes id: Int): String = resources.getString(id)
}
