package com.rain.remynd.di

import android.app.Activity
import android.content.Context
import com.rain.remynd.RemyndActivity
import com.rain.remynd.RemyndApp
import com.rain.remynd.alarm.AlarmComponent
import com.rain.remynd.alarm.AlarmModule
import com.rain.remynd.common.AppScope
import com.rain.remynd.common.ResourcesProvider
import com.rain.remynd.common.ResourcesProviderImpl
import com.rain.remynd.common.VibrateUtils
import com.rain.remynd.common.VibrateUtilsImpl
import com.rain.remynd.data.StorageModule
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlin.reflect.KClass

@Module
object AppModule {

    @Provides
    @AppScope
    fun provideContext(app: RemyndApp): Context = app

    @Provides
    @AppScope
    fun provideTargetActivity(): KClass<out Activity> = RemyndActivity::class

    @Provides
    @AppScope
    fun provideVibrateUtils(context: Context): VibrateUtils = VibrateUtilsImpl(context)

    @Provides
    @AppScope
    fun provideResourcesProvider(context: Context): ResourcesProvider {
        return ResourcesProviderImpl(context.resources)
    }
}

@Component(modules = [AppModule::class, StorageModule::class, AlarmModule::class])
@AppScope
interface AppComponent : RemyndDependency, AlarmComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: RemyndApp): AppComponent
    }
}
