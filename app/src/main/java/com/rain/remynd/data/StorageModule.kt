package com.rain.remynd.data

import androidx.room.Room
import com.rain.remynd.AppScope
import com.rain.remynd.RemyndApp
import dagger.Module
import dagger.Provides

@Module
object StorageModule {
    @Provides
    @JvmStatic
    @AppScope
    fun provideRoom(app: RemyndApp): RemyndDB {
        return Room.databaseBuilder(
            app,
            RemyndDB::class.java,
            "remynd-database"
        ).build()
    }

    @Provides
    @JvmStatic
    @AppScope
    fun provideRemyndDao(db: RemyndDB): RemyndDao = db.dao()
}
