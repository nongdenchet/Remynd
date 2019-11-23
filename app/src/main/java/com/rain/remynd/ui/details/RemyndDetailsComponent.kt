package com.rain.remynd.ui.details

import com.rain.remynd.data.RemyndDao
import com.rain.remynd.alarm.AlarmScheduler
import com.rain.remynd.common.RemindFormatUtils
import com.rain.remynd.common.RemindFormatUtilsImpl
import com.rain.remynd.common.ResourcesProvider
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class RemyndDetailsScope

@Module
object RemyndDetailsModule {

    @Provides
    @JvmStatic
    @RemyndDetailsScope
    fun provideRemindFormatUtils(resourcesProvider: ResourcesProvider): RemindFormatUtils {
        return RemindFormatUtilsImpl(resourcesProvider)
    }

    @Provides
    @JvmStatic
    @RemyndDetailsScope
    fun providePresenter(
        fragment: RemyndDetailsFragment,
        remyndDao: RemyndDao,
        alarmScheduler: AlarmScheduler,
        remindFormatUtils: RemindFormatUtils,
        resourcesProvider: ResourcesProvider
    ) = RemyndDetailsPresenter(
        fragment,
        remyndDao,
        alarmScheduler,
        remindFormatUtils,
        resourcesProvider
    )
}

@RemyndDetailsScope
@Component(modules = [RemyndDetailsModule::class], dependencies = [RemyndDetailsDependency::class])
interface RemyndDetailsComponent {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance fragment: RemyndDetailsFragment,
            dependency: RemyndDetailsDependency
        ): RemyndDetailsComponent
    }

    fun inject(fragment: RemyndDetailsFragment)
}

interface RemyndDetailsDependency {
    fun remyndDao(): RemyndDao
    fun resourceProvider(): ResourcesProvider
    fun alarmScheduler(): AlarmScheduler
}
