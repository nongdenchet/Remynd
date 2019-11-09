package com.rain.remynd.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RemyndDao {
    @Query("SELECT * FROM remynd_table")
    fun observe(): Flow<List<RemyndEntity>>

    @Query("SELECT COUNT(id) FROM remynd_table")
    suspend fun count(): Int

    @Query("SELECT * FROM remynd_table WHERE id = :id")
    suspend fun getByID(id: Long): RemyndEntity

    @Insert
    suspend fun insert(data: RemyndEntity)

    @Update
    suspend fun update(data: RemyndEntity)

    @Query("DELETE FROM remynd_table WHERE id = :id")
    suspend fun delete(id: Long)
}
