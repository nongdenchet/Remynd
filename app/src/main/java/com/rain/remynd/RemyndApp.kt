package com.rain.remynd

import android.app.Application

class RemyndApp : Application() {
    private lateinit var component: AppComponent

    override fun onCreate() {
        super.onCreate()
    }
}
