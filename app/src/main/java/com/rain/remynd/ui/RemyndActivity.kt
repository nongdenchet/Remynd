package com.rain.remynd.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentFactory
import com.rain.remynd.R
import com.rain.remynd.data.RemyndDao
import com.rain.remynd.data.RemyndEntity
import com.rain.remynd.support.dependency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RemyndActivity : AppCompatActivity() {
    private val parentJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + parentJob)
    private val tag = RemyndActivity::class.java.simpleName

    @Inject
    internal lateinit var remyndDao: RemyndDao
    @Inject
    internal lateinit var fragmentFactory: FragmentFactory
    @Inject
    internal lateinit var navigator: RemyndNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        setUpDependency()
        supportFragmentManager.fragmentFactory = fragmentFactory
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remynd)
        if (savedInstanceState == null) {
            initialize()
        }
    }

    private fun initialize() {
        navigator.showRemyndList()
        scope.launch(Dispatchers.IO) {
            val count = remyndDao.count()
            if (count == 0) {
                Log.d(tag, Thread.currentThread().name + ": inserting")
                remyndDao.insert(
                    RemyndEntity(
                        content = "Drink Water",
                        triggerAt = Date().time,
                        active = false,
                        vibrate = false
                    )
                )
                remyndDao.insert(
                    RemyndEntity(
                        content = "Test Code",
                        triggerAt = Date().time + TimeUnit.DAYS.toMillis(1),
                        active = true,
                        vibrate = false
                    )
                )
            } else {
                Log.d(tag, Thread.currentThread().name + ": $count")
            }
        }
    }

    private fun setUpDependency() {
        DaggerRemyndComponent.factory()
            .create(this, applicationContext.dependency(RemyndDependency::class))
            .inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        parentJob.cancel()
    }
}
