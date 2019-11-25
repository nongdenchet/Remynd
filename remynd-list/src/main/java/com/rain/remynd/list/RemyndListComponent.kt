package com.rain.remynd.list

import androidx.lifecycle.LifecycleObserver
import com.rain.remynd.alarm.scheduler.AlarmScheduler
import com.rain.remynd.common.RemindFormatUtils
import com.rain.remynd.common.RemindFormatUtilsImpl
import com.rain.remynd.common.ResourcesProvider
import com.rain.remynd.common.VibrateUtils
import com.rain.remynd.data.RemyndDao
import com.rain.remynd.navigator.Navigator
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
internal annotation class RemyndListScope

@Module
internal object RemyndListModule {

    @Provides
    @RemyndListScope
    fun provideAdapter() = RemyndListAdapter()

    @Provides
    @RemyndListScope
    fun providePresenter(
        fragment: RemyndListFragment,
        remyndDao: RemyndDao,
        navigator: Navigator,
        alarmScheduler: AlarmScheduler,
        resourcesProvider: ResourcesProvider,
        remindFormatUtils: RemindFormatUtils,
        vibrateUtils: VibrateUtils
    ) = RemyndListPresenter(
        fragment,
        remyndDao,
        navigator,
        alarmScheduler,
        resourcesProvider,
        remindFormatUtils,
        vibrateUtils
    )

    @Provides
    @RemyndListScope
    fun provideRemindFormatUtils(resourcesProvider: ResourcesProvider): RemindFormatUtils {
        return RemindFormatUtilsImpl(resourcesProvider)
    }

    @Provides
    @IntoSet
    @RemyndListScope
    fun providePresenterObserver(presenter: RemyndListPresenter): LifecycleObserver = presenter

    @Provides
    @IntoSet
    @RemyndListScope
    fun provideAdapterObserver(adapter: RemyndListAdapter): LifecycleObserver = adapter
}

@RemyndListScope
@Component(modules = [RemyndListModule::class], dependencies = [RemyndListDependency::class])
internal interface RemyndListComponent {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance fragment: RemyndListFragment,
            dependency: RemyndListDependency
        ): RemyndListComponent
    }

    fun inject(fragment: RemyndListFragment)
}

interface RemyndListDependency {
    fun remyndDao(): RemyndDao
    fun remyndNavigator(): Navigator
    fun resourceProvider(): ResourcesProvider
    fun alarmScheduler(): AlarmScheduler
    fun vibrateUtils(): VibrateUtils
}
