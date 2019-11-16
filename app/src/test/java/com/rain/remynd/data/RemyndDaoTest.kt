package com.rain.remynd.data

import android.content.Context
import android.os.Build
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class RemyndDaoTest {
    private lateinit var dao: RemyndDao
    private lateinit var db: RemyndDB

    private val data = RemyndEntity(
        content = "Drink Water",
        triggerAt = 1000000,
        interval = 60 * 1000,
        active = true,
        vibrate = false
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
            assertEquals(1, dao.insert(data))
            assertEquals(1, dao.count())
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
                            active = true,
                            vibrate = false
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
                active = true,
                vibrate = false
            )
            assertEquals(1, dao.insert(data))

            // Delete
            assertEquals(1, dao.delete(1))
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
            assertEquals(1, dao.insert(data))

            // Update
            val data = RemyndEntity(
                id = 1,
                content = "Test",
                triggerAt = 1000000,
                interval = 60 * 1000,
                active = true,
                vibrate = false
            )
            assertEquals(1, dao.update(data))
            dao.observe()
                .take(1)
                .collect {
                    assertEquals(1, it.size)
                    assertEquals(data, it[0])
                }
        }
    }

    @Test
    fun updateActive() {
        runBlocking {
            // Insert
            assertEquals(1, dao.insert(data))

            // Update
            assertEquals(1, dao.update(1, false))
            dao.observe()
                .take(1)
                .collect {
                    assertEquals(
                        RemyndEntity(
                            id = 1,
                            content = "Drink Water",
                            triggerAt = 1000000,
                            interval = 60 * 1000,
                            active = false,
                            vibrate = false
                        ),
                        it[0]
                    )
                }
        }
    }

    @Test
    fun updateActiveNonExistRecord() {
        runBlocking {
            assertEquals(0, dao.update(1, true))
        }
    }

    @Test
    fun updateNonExistRecord() {
        runBlocking {
            assertEquals(0, dao.update(data))
        }
    }

    @Test
    fun deleteNonExistRecord() {
        runBlocking {
            assertEquals(0, dao.delete(1))
        }
    }

    @After
    fun tearDown() = db.close()
}
