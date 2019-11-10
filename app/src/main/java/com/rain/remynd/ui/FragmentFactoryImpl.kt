package com.rain.remynd.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.rain.remynd.ui.list.RemyndListDependency
import com.rain.remynd.ui.list.RemyndListFragment

class FragmentFactoryImpl(
    private val dependency: RemyndListDependency
) : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        val clazz = loadFragmentClass(classLoader, className)
        return if (clazz == RemyndListFragment::class.java) {
            RemyndListFragment(dependency)
        } else {
            super.instantiate(classLoader, className)
        }
    }
}
