package com.rain.remynd.list

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory

class MockFragmentFactoryImpl(
    private val dependency: RemyndListDependency
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (loadFragmentClass(classLoader, className)) {
            RemyndListFragment::class.java -> RemyndListFragment(dependency)
            else -> super.instantiate(classLoader, className)
        }
    }
}
