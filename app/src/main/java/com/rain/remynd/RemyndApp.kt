package com.rain.remynd

import android.app.Application
import com.rain.remynd.alarm.AlarmComponent
import com.rain.remynd.common.DependencyProvider
import com.rain.remynd.di.AppComponent
import com.rain.remynd.di.DaggerAppComponent
import com.rain.remynd.di.RemyndDependency
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
            AlarmComponent::class -> component as T
            else -> throw IllegalStateException("RemyndApp cannot satisfy dependency: $clazz")
        }
    }
}
