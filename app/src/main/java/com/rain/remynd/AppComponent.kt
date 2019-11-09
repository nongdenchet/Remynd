package com.rain.remynd

import com.rain.remynd.data.StorageModule
import com.rain.remynd.ui.RemyndDependency
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AppScope

@Module
object AppModule

@Component(modules = [AppModule::class, StorageModule::class])
@AppScope
interface AppComponent : RemyndDependency {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: RemyndApp): AppComponent
    }
}
