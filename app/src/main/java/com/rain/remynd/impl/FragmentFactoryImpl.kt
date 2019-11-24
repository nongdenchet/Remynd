package com.rain.remynd.impl

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.rain.remynd.di.RemyndComponent
import com.rain.remynd.ui.details.RemyndDetailsFragment
import com.rain.remynd.ui.list.RemyndListFragment

class FragmentFactoryImpl(private val dependency: RemyndComponent) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (loadFragmentClass(classLoader, className)) {
            RemyndDetailsFragment::class.java -> RemyndDetailsFragment(dependency)
            RemyndListFragment::class.java -> RemyndListFragment(dependency)
            else -> super.instantiate(classLoader, className)
        }
    }
}
