package com.rain.remynd.ui.list

import android.content.Context
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.rain.remynd.R
import com.rain.remynd.data.RemyndDB
import com.rain.remynd.data.RemyndDao
import com.rain.remynd.data.RemyndEntity
import com.rain.remynd.ui.FragmentFactoryImpl
import com.rain.remynd.ui.execute
import com.rain.remynd.ui.withRecyclerView
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.util.Date

@LargeTest
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RemyndListFragmentTest {
    private lateinit var factory: FragmentFactory
    private lateinit var db: RemyndDB
    private lateinit var remyndDao: RemyndDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            RemyndDB::class.java
        ).fallbackToDestructiveMigration()
            .build()
        remyndDao = db.dao()
        factory = FragmentFactoryImpl(object : RemyndListDependency {
            override fun remyndDao(): RemyndDao = remyndDao
        })
    }

    private fun mockData() {
        runBlocking {
            remyndDao.insert(
                RemyndEntity(
                    content = "Drink Water",
                    triggerAt = Date().time,
                    active = false
                )
            )
            remyndDao.insert(
                RemyndEntity(
                    content = "Test Code",
                    triggerAt = Date().time,
                    active = true
                )
            )
        }
    }

    @Test
    fun testShowItemsFromDatabase() {
        mockData()
        launchFragmentInContainer<RemyndListFragment>(
            factory = factory,
            themeResId = R.style.AppTheme
        )

        // First item
        execute {
            onView(withRecyclerView(R.id.rvReminds).atPositionOnView(0, R.id.tvContent))
                .check(matches(withText("Drink Water")))
        }
        execute {
            onView(withRecyclerView(R.id.rvReminds).atPositionOnView(0, R.id.sEnabled))
                .check(matches(isNotChecked()))
        }

        // Second item
        execute {
            onView(withRecyclerView(R.id.rvReminds).atPositionOnView(1, R.id.tvContent))
                .check(matches(withText("Test Code")))
        }
        execute {
            onView(withRecyclerView(R.id.rvReminds).atPositionOnView(1, R.id.sEnabled))
                .check(matches(isChecked()))
        }
    }

    @Test
    fun testSwitchOffItem() {
        mockData()
        launchFragmentInContainer<RemyndListFragment>(
            factory = factory,
            themeResId = R.style.AppTheme
        )

        execute {
            onView(withRecyclerView(R.id.rvReminds).atPositionOnView(0, R.id.sEnabled))
                .check(matches(isNotChecked()))
        }
        execute {
            onView(withRecyclerView(R.id.rvReminds).atPositionOnView(0, R.id.sEnabled))
                .perform(click())
        }
        execute {
            onView(withRecyclerView(R.id.rvReminds).atPositionOnView(0, R.id.sEnabled))
                .check(matches(isChecked()))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }
}
