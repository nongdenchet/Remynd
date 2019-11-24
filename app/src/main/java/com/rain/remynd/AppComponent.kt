package com.rain.remynd

import android.content.Context
import com.rain.remynd.alarm.AlarmIntentService
import com.rain.remynd.alarm.AlarmScheduler
import com.rain.remynd.alarm.AlarmSchedulerImpl
import com.rain.remynd.alarm.BootIntentService
import com.rain.remynd.common.AppScope
import com.rain.remynd.common.ResourcesProvider
import com.rain.remynd.common.ResourcesProviderImpl
import com.rain.remynd.common.VibrateUtils
import com.rain.remynd.common.VibrateUtilsImpl
import com.rain.remynd.data.StorageModule
import com.rain.remynd.ui.RemyndDependency
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

@Module
object AppModule {

    @Provides
    @AppScope
    fun provideContext(app: RemyndApp): Context = app

    @Provides
    @AppScope
    fun provideAlarmScheduler(context: Context): AlarmScheduler = AlarmSchedulerImpl(context)

    @Provides
    @AppScope
    fun provideVibrateUtils(context: Context): VibrateUtils = VibrateUtilsImpl(context)

    @Provides
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

    fun inject(service: AlarmIntentService)
    fun inject(service: BootIntentService)
}
