package com.rain.remynd.details

import com.rain.remynd.alarm.scheduler.AlarmScheduler
import com.rain.remynd.common.RemindFormatUtils
import com.rain.remynd.common.RemindFormatUtilsImpl
import com.rain.remynd.common.ResourcesProvider
import com.rain.remynd.data.RemyndDao
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
internal annotation class RemyndDetailsScope

@Module
internal object RemyndDetailsModule {

    @Provides
    @RemyndDetailsScope
    fun provideRemindFormatUtils(resourcesProvider: ResourcesProvider): RemindFormatUtils {
        return RemindFormatUtilsImpl(resourcesProvider)
    }

    @Provides
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
internal interface RemyndDetailsComponent {

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
