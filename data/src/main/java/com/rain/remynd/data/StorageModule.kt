package com.rain.remynd.data

import android.content.Context
import androidx.room.Room
import com.rain.remynd.common.AppScope
import dagger.Module
import dagger.Provides

@Module
object StorageModule {

    @Provides
    @JvmStatic
    @AppScope
    fun provideRoom(context: Context): RemyndDB {
        return Room.databaseBuilder(
            context,
            RemyndDB::class.java,
            "remynd-database"
        ).build()
    }

    @Provides
    @JvmStatic
    @AppScope
    fun provideRemyndDao(db: RemyndDB): RemyndDao = db.dao()
}
