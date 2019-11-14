package com.rain.remynd.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.rain.remynd.ui.details.RemyndDetailsFragment
import com.rain.remynd.ui.list.RemyndListFragment

class RemyndFragmentFactory(private val dependency: RemyndComponent) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (loadFragmentClass(classLoader, className)) {
            RemyndDetailsFragment::class.java -> RemyndDetailsFragment(dependency)
            RemyndListFragment::class.java -> RemyndListFragment(dependency)
            else -> super.instantiate(classLoader, className)
        }
    }
}
