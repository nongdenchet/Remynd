package com.rain.remynd.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentFactory
import com.rain.remynd.R
import com.rain.remynd.ui.details.REMYND_ID
import com.rain.remynd.ui.details.RemyndDetailsFragment
import com.rain.remynd.ui.list.RemyndListFragment

interface RemyndNavigator {
    fun showRemyndList()
    fun showRemyndForm()
    fun showRemyndDetails(id: Long)
}

@Suppress("MemberVisibilityCanBePrivate")
class MockRemyndNavigator : RemyndNavigator {
    var showRemyndListCount = 0
    var showRemyndFormCount = 0
    var showRemyndDetailsCount = 0

    override fun showRemyndList() {
        showRemyndListCount++
    }

    override fun showRemyndForm() {
        showRemyndFormCount++
    }

    override fun showRemyndDetails(id: Long) {
        showRemyndDetailsCount++
    }

    fun reset() {
        showRemyndListCount = 0
        showRemyndFormCount = 0
        showRemyndDetailsCount = 0
    }
}

class RemyndNavigatorImpl(
    private val activity: AppCompatActivity,
    private val fragmentFactory: FragmentFactory
) : RemyndNavigator {
    override fun showRemyndList() {
        activity.supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.main_container,
                fragmentFactory.instantiate(
                    activity.classLoader,
                    RemyndListFragment::class.java.name
                ),
                RemyndListFragment.tag
            )
            .commit()
    }

    override fun showRemyndForm() {
        activity.supportFragmentManager
            .beginTransaction()
            .add(
                R.id.main_container,
                fragmentFactory.instantiate(
                    activity.classLoader,
                    RemyndDetailsFragment::class.java.name
                ),
                RemyndDetailsFragment.tag
            )
            .addToBackStack(null)
            .commit()
    }

    override fun showRemyndDetails(id: Long) {
        val fragment = fragmentFactory.instantiate(
            activity.classLoader,
            RemyndDetailsFragment::class.java.name
        )
        fragment.arguments = Bundle().apply {
            this.putLong(REMYND_ID, id)
        }

        activity.supportFragmentManager
            .beginTransaction()
            .add(
                R.id.main_container,
                fragment,
                RemyndDetailsFragment.tag
            )
            .addToBackStack(null)
            .commit()
    }
}
