package com.rain.remynd.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentFactory
import com.rain.remynd.R
import com.rain.remynd.support.BackHandler
import com.rain.remynd.support.dependency
import javax.inject.Inject

class RemyndActivity : AppCompatActivity() {
    private val tag = RemyndActivity::class.java.simpleName

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
            navigator.showRemyndList()
        }
    }

    private fun setUpDependency() {
        DaggerRemyndComponent.factory()
            .create(this, applicationContext.dependency(RemyndDependency::class))
            .inject(this)
    }

    override fun onBackPressed() {
        val handler = supportFragmentManager.findFragmentById(R.id.main_container) as? BackHandler
        if (handler?.onBackPressed() == true) {
            return
        }

        super.onBackPressed()
    }
}
