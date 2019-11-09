package com.rain.remynd.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RemyndDaoTest {
    private lateinit var dao: RemyndDao
    private lateinit var db: RemyndDB

    private val data = RemyndEntity(
        content = "Drink Water",
        triggerAt = 1000000,
        interval = 60 * 1000,
        active = true
    )

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            RemyndDB::class.java
        ).build()
        dao = db.dao()
    }

    @Test
    fun insert() {
        runBlocking {
            dao.insert(data)
            dao.observe()
                .take(1)
                .collect {
                    assertEquals(1, it.size)
                    assertEquals(
                        RemyndEntity(
                            id = 1,
                            content = "Drink Water",
                            triggerAt = 1000000,
                            interval = 60 * 1000,
                            active = true
                        ),
                        it[0]
                    )
                }
        }
    }

    @Test
    fun delete() {
        runBlocking {
            // Insert
            val data = RemyndEntity(
                content = "Drink Water",
                triggerAt = 1000000,
                interval = 60 * 1000,
                active = true
            )
            dao.insert(data)
            dao.observe()
                .take(1)
                .collect {
                    assertEquals(1, it.size)
                    assertEquals(
                        RemyndEntity(
                            id = 1,
                            content = "Drink Water",
                            triggerAt = 1000000,
                            interval = 60 * 1000,
                            active = true
                        ),
                        it[0]
                    )
                }

            // Delete
            dao.delete(1)
            dao.observe()
                .take(1)
                .collect {
                    assertEquals(0, it.size)
                }
        }
    }

    @Test
    fun update() {
        runBlocking {
            // Insert
            dao.insert(data)
            dao.observe()
                .take(1)
                .collect {
                    assertEquals(1, it.size)
                    assertEquals(
                        RemyndEntity(
                            id = 1,
                            content = "Drink Water",
                            triggerAt = 1000000,
                            interval = 60 * 1000,
                            active = true
                        ),
                        it[0]
                    )
                }

            // Update
            val data = RemyndEntity(
                id = 1,
                content = "Test",
                triggerAt = 1000000,
                interval = 60 * 1000,
                active = true
            )
            dao.update(data)
            dao.observe()
                .take(1)
                .collect {
                    assertEquals(1, it.size)
                    assertEquals(data, it[0])
                }
        }
    }

    @After
    fun tearDown() = db.close()
}
