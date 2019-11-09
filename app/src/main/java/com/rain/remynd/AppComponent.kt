package com.rain.remynd

import dagger.BindsInstance
import dagger.Component
import dagger.Module
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AppScope

@Module
object AppModule

@Component(modules = [AppModule::class])
@AppScope
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: RemyndApp): AppComponent
    }
}
