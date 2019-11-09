package com.rain.remynd.ui.list

import com.rain.remynd.data.RemyndDao
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class RemyndListScope

@Module
object RemyndListModule

@Component(modules = [RemyndListModule::class], dependencies = [RemyndListDependency::class])
@RemyndListScope
interface RemyndListComponent {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance fragment: RemyndListFragment,
            dependency: RemyndListDependency
        ): RemyndListComponent
    }
}

interface RemyndListDependency {
    fun remyndDao(): RemyndDao
}
