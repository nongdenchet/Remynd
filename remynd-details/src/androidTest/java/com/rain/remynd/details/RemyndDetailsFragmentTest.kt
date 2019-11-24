package com.rain.remynd.details

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.rain.remynd.alarm.bridge.Alarm
import com.rain.remynd.alarm.bridge.AlarmScheduler
import com.rain.remynd.common.ResourcesProviderImpl
import com.rain.remynd.data.RemyndDB
import com.rain.remynd.data.RemyndDao
import com.rain.remynd.data.RemyndEntity
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
class RemyndDetailsFragmentTest {
    private lateinit var factory: FragmentFactory
    private lateinit var db: RemyndDB
    private lateinit var remyndDao: RemyndDao

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
        factory = MockFragmentFactoryImpl(object : RemyndDetailsDependency {
            override fun remyndDao() = remyndDao
            override fun alarmScheduler() = scheduler
            override fun resourceProvider() = ResourcesProviderImpl(context.resources)
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
                    daysOfWeek = "1;3;5",
                    active = false,
                    vibrate = true,
                    interval = 5 * 60 * 1000
                )
            )
        }
    }

    @Test
    fun testUpdateExistingRemind() {
        mockData()
        launchFragmentInContainer<RemyndDetailsFragment>(
            fragmentArgs = Bundle().apply {
                putLong(REMYND_ID, 1)
            },
            factory = factory,
            themeResId = R.style.AppTheme
        )

        onView(withId(R.id.edtTitle)).check(matches(withText("Drink Water")))
        onView(withId(R.id.edtTitle)).perform(
            click(),
            replaceText("Hello World"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.tvSave)).perform(click())

        runBlocking {
            assertEquals("Hello World", remyndDao.get(1)!!.content)
        }
    }

    @Test
    fun testOpenExistingRemind() {
        mockData()
        launchFragmentInContainer<RemyndDetailsFragment>(
            fragmentArgs = Bundle().apply {
                putLong(REMYND_ID, 1)
            },
            factory = factory,
            themeResId = R.style.AppTheme
        )

        onView(withId(R.id.edtTitle)).check(matches(withText("Drink Water")))
        onView(withId(R.id.tvRemindValue)).check(matches(withText("5m")))
        onView(withId(R.id.sEnabled)).check(matches(not(isChecked())))
    }

    @Test
    fun testCreateNewRemind() {
        launchFragmentInContainer<RemyndDetailsFragment>(
            factory = factory,
            themeResId = R.style.AppTheme
        )

        onView(withId(R.id.edtTitle)).perform(
            typeText("Go to school"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.sVibrate)).perform(click())
        onView(withId(R.id.tvSave)).perform(click())

        runBlocking {
            val entity = remyndDao.get(1)!!
            assertEquals("Go to school", entity.content)
            assertTrue(entity.active)
            assertTrue(entity.vibrate)
            verify(scheduler).schedule(
                Alarm(
                    id = 1,
                    content = "Go to school",
                    triggerAt = entity.triggerAt,
                    vibrate = true,
                    interval = null
                )
            )
        }
    }

    @After
    fun tearDown() {
        db.close()
    }
}
