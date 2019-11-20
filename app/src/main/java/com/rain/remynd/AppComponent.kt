package com.rain.remynd

import android.content.Context
import com.rain.remynd.alarm.AlarmIntentService
import com.rain.remynd.alarm.AlarmScheduler
import com.rain.remynd.alarm.AlarmSchedulerImpl
import com.rain.remynd.data.StorageModule
import com.rain.remynd.support.ResourcesProvider
import com.rain.remynd.support.ResourcesProviderImpl
import com.rain.remynd.support.VibrateUtils
import com.rain.remynd.support.VibrateUtilsImpl
import com.rain.remynd.ui.RemyndDependency
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AppScope

@Module
object AppModule {

    @Provides
    @JvmStatic
    @AppScope
    fun provideContext(app: RemyndApp): Context = app

    @Provides
    @JvmStatic
    @AppScope
    fun provideAlarmScheduler(context: Context): AlarmScheduler = AlarmSchedulerImpl(context)

    @Provides
    @JvmStatic
    @AppScope
    fun provideVibrateUtils(context: Context): VibrateUtils = VibrateUtilsImpl(context)

    @Provides
    @JvmStatic
    @AppScope
    fun provideResourcesProvider(context: Context): ResourcesProvider {
        return ResourcesProviderImpl(context.resources)
    }
}

@Component(modules = [AppModule::class, StorageModule::class])
@AppScope
interface AppComponent : RemyndDependency {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: RemyndApp): AppComponent
    }

    fun inject(alarmIntentService: AlarmIntentService)
}
