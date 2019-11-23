package com.rain.remynd.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentFactory
import com.rain.remynd.R
import com.rain.remynd.navigator.Navigator
import com.rain.remynd.ui.details.REMYND_ID
import com.rain.remynd.ui.details.RemyndDetailsFragment
import com.rain.remynd.ui.list.RemyndListFragment

class NavigatorImpl(
    private val activity: AppCompatActivity,
    private val fragmentFactory: FragmentFactory
) : Navigator {
    override fun showRemindList() {
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

    override fun showRemindForm() {
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

    override fun showRemindDetails(id: Long) {
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
