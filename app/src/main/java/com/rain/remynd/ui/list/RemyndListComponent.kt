package com.rain.remynd.ui.list

import com.rain.remynd.data.RemyndDao
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
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
        remyndDao: RemyndDao
    ) = RemyndListPresenter(
        fragment,
        remyndDao
    )
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
}
