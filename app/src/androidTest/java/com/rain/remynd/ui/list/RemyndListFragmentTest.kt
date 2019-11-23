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
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.rain.remynd.R
import com.rain.remynd.alarm.AlarmScheduler
import com.rain.remynd.data.RemyndDB
import com.rain.remynd.data.RemyndDao
import com.rain.remynd.data.RemyndEntity
import com.rain.remynd.common.ResourcesProvider
import com.rain.remynd.common.ResourcesProviderImpl
import com.rain.remynd.common.VibrateUtils
import com.rain.remynd.common.VibrateUtilsImpl
import com.rain.remynd.navigator.Navigator
import com.rain.remynd.ui.execute
import com.rain.remynd.ui.recyclerViewCount
import com.rain.remynd.ui.withRecyclerView
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import java.util.Calendar

@LargeTest
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RemyndListFragmentTest {
    private lateinit var factory: FragmentFactory
    private lateinit var db: RemyndDB
    private lateinit var remyndDao: RemyndDao

    @Mock
    private lateinit var navigator: Navigator
    @Mock
    private lateinit var scheduler: AlarmScheduler

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RemyndDB::class.java)
            .fallbackToDestructiveMigration()
            .build()
        remyndDao = db.dao()
        factory = MockFragmentFactoryImpl(object : RemyndListDependency {
            override fun remyndDao(): RemyndDao = remyndDao
            override fun remyndNavigator(): Navigator = navigator
            override fun alarmScheduler(): AlarmScheduler = scheduler
            override fun vibrateUtils(): VibrateUtils = VibrateUtilsImpl(context)
            override fun resourceProvider(): ResourcesProvider =
                ResourcesProviderImpl(context.resources)
        })
    }

    private fun mockData() {
        runBlocking {
            remyndDao.insert(
                RemyndEntity(
                    content = "Drink Water",
                    triggerAt = Calendar.getInstance().apply {
                        add(Calendar.DATE, 1)
                    }.timeInMillis,
                    active = false,
                    vibrate = false
                )
            )
            remyndDao.insert(
                RemyndEntity(
                    content = "Test Code",
                    triggerAt = Calendar.getInstance().apply {
                        add(Calendar.DATE, 1)
                    }.timeInMillis,
                    active = true,
                    vibrate = false
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

        // Alarm count
        execute {
            onView(withId(R.id.tvTotal)).check(matches(withText("Total 1 alarms active")))
        }

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
    fun testRecreateFragment() {
        mockData()
        launchFragmentInContainer<RemyndListFragment>(
            factory = factory,
            themeResId = R.style.AppTheme
        ).recreate()

        execute {
            onView(withId(R.id.rvReminds)).check(recyclerViewCount(2))
        }
    }

    @Test
    fun testClickingOnAdd() {
        mockData()
        launchFragmentInContainer<RemyndListFragment>(
            factory = factory,
            themeResId = R.style.AppTheme
        )

        onView(withId(R.id.ivAdd)).perform(click())
        execute {
            verify(navigator).showRemindForm()
        }
    }

    @Test
    fun testClickingOnItem() {
        mockData()
        launchFragmentInContainer<RemyndListFragment>(
            factory = factory,
            themeResId = R.style.AppTheme
        )

        onView(withRecyclerView(R.id.rvReminds).atPosition(0)).perform(click())
        execute {
            verify(navigator).showRemindDetails(1)
        }
    }

    @Test
    fun testSwitchItem() {
        mockData()
        launchFragmentInContainer<RemyndListFragment>(
            factory = factory,
            themeResId = R.style.AppTheme
        )

        // Before switch
        execute {
            onView(withRecyclerView(R.id.rvReminds).atPositionOnView(0, R.id.sEnabled))
                .check(matches(isNotChecked()))
        }
        execute {
            onView(withId(R.id.tvTotal)).check(matches(withText("Total 1 alarms active")))
        }

        // After switch
        execute {
            onView(withRecyclerView(R.id.rvReminds).atPositionOnView(0, R.id.sEnabled))
                .perform(click())
        }
        execute {
            onView(withRecyclerView(R.id.rvReminds).atPositionOnView(0, R.id.sEnabled))
                .check(matches(isChecked()))
        }
        execute {
            onView(withId(R.id.tvTotal)).check(matches(withText("Total 2 alarms active")))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }
}
