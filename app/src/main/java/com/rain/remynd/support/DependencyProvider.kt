package com.rain.remynd.support

import android.content.Context

interface DependencyProvider<out T> {
    fun provide(): T
}

@Suppress("UNCHECKED_CAST")
fun <T> Context.dependency(): T {
    return (this as DependencyProvider<T>).provide()
}
