package com.rain.remynd.ui.list

import androidx.lifecycle.LifecycleObserver
import com.rain.remynd.alarm.AlarmScheduler
import com.rain.remynd.data.RemyndDao
import com.rain.remynd.support.ResourcesProvider
import com.rain.remynd.support.VibrateUtils
import com.rain.remynd.ui.RemyndNavigator
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class RemyndListScope

@Module
object RemyndListModule {

    @Provides
    @JvmStatic
    @RemyndListScope
    fun provideAdapter() = RemyndListAdapter()

    @Provides
    @JvmStatic
    @RemyndListScope
    fun providePresenter(
        fragment: RemyndListFragment,
        remyndDao: RemyndDao,
        navigator: RemyndNavigator,
        alarmScheduler: AlarmScheduler,
        resourcesProvider: ResourcesProvider,
        vibrateUtils: VibrateUtils
    ) = RemyndListPresenter(
        fragment,
        remyndDao,
        navigator,
        alarmScheduler,
        resourcesProvider,
        vibrateUtils
    )

    @Provides
    @JvmStatic
    @IntoSet
    @RemyndListScope
    fun providePresenterObserver(presenter: RemyndListPresenter): LifecycleObserver = presenter

    @Provides
    @JvmStatic
    @IntoSet
    @RemyndListScope
    fun provideAdapterObserver(adapter: RemyndListAdapter): LifecycleObserver = adapter
}

@RemyndListScope
@Component(modules = [RemyndListModule::class], dependencies = [RemyndListDependency::class])
interface RemyndListComponent {

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
    fun remyndNavigator(): RemyndNavigator
    fun resourceProvider(): ResourcesProvider
    fun alarmScheduler(): AlarmScheduler
    fun vibrateUtils(): VibrateUtils
}
