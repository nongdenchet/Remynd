package com.rain.remynd.alarm

import android.app.Activity
import android.content.Context
import com.rain.remynd.alarm.bridge.AlarmScheduler
import com.rain.remynd.common.AppScope
import dagger.Module
import dagger.Provides
import kotlin.reflect.KClass

interface AlarmComponent {
    fun alarmScheduler(): AlarmScheduler
    fun targetActivity(): KClass<out Activity>
    fun inject(service: AlarmIntentService)
    fun inject(service: BootIntentService)
}

@Module
object AlarmModule {

    @Provides
    @AppScope
    fun provideAlarmScheduler(context: Context): AlarmScheduler = AlarmSchedulerImpl(context)
}
