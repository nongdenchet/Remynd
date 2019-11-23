package com.rain.remynd.common

import android.content.Context
import kotlin.reflect.KClass

interface DependencyProvider {
    fun <T : Any> provide(clazz: KClass<T>): T
}

fun <T : Any> Context.dependency(clazz: KClass<T>): T {
    return (this as DependencyProvider).provide(clazz)
}
