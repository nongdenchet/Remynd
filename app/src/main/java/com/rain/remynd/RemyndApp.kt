package com.rain.remynd

import android.app.Application
import com.rain.remynd.support.DependencyProvider
import com.rain.remynd.ui.RemyndDependency
import kotlin.reflect.KClass

class RemyndApp : Application(), DependencyProvider {
    private lateinit var component: AppComponent

    override fun onCreate() {
        super.onCreate()
        component = DaggerAppComponent.factory()
            .create(this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> provide(clazz: KClass<T>): T {
        return when (clazz) {
            RemyndDependency::class -> component as T
            else -> throw IllegalStateException("RemyndApp cannot satisfy dependency: $clazz")
        }
    }
}
