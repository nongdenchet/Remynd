package com.rain.remynd.ui

import androidx.fragment.app.FragmentFactory
import com.rain.remynd.data.RemyndDao
import com.rain.remynd.alarm.AlarmScheduler
import com.rain.remynd.support.ResourcesProvider
import com.rain.remynd.ui.details.RemyndDetailsDependency
import com.rain.remynd.ui.list.RemyndListDependency
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class RemyndScope

@Module
object RemyndModule {

    @RemyndScope
    @Provides
    @JvmStatic
    fun provideFragmentFactory(component: RemyndComponent): FragmentFactory =
        RemyndFragmentFactory(component)

    @RemyndScope
    @Provides
    @JvmStatic
    fun provideRemyndNavigator(
        activity: RemyndActivity,
        fragmentFactory: FragmentFactory
    ): RemyndNavigator = RemyndNavigatorImpl(
        activity,
        fragmentFactory
    )
}

@Component(modules = [RemyndModule::class], dependencies = [RemyndDependency::class])
@RemyndScope
interface RemyndComponent : RemyndListDependency, RemyndDetailsDependency {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance activity: RemyndActivity,
            dependency: RemyndDependency
        ): RemyndComponent
    }

    fun inject(remyndActivity: RemyndActivity)
}

interface RemyndDependency {
    fun remyndDao(): RemyndDao
    fun alarmScheduler(): AlarmScheduler
    fun resourceProvider(): ResourcesProvider
}
