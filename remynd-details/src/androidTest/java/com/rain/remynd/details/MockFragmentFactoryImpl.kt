package com.rain.remynd.details

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory

class MockFragmentFactoryImpl(
    private val dependency: RemyndDetailsDependency
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (loadFragmentClass(classLoader, className)) {
            RemyndDetailsFragment::class.java -> RemyndDetailsFragment(dependency)
            else -> super.instantiate(classLoader, className)
        }
    }
}
