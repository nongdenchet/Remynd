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
    suspend fun get(id: Long): RemyndEntity?

    @Insert
    suspend fun insert(data: RemyndEntity): Long

    @Update
    suspend fun update(data: RemyndEntity): Int

    @Query("DELETE FROM remynd_table WHERE id = :id")
    suspend fun delete(id: Long): Int

    @Query("DELETE FROM remynd_table WHERE id in (:ids)")
    suspend fun delete(ids: List<Long>): Int
}
