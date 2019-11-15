package com.rain.remynd.ui.details

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rain.remynd.databinding.FragmentRemyndDetailsBinding
import com.rain.remynd.support.observe
import com.rain.remynd.view.DateItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal const val REMYND_ID = "REMYND_ID"

class RemyndDetailsFragment(private val dependency: RemyndDetailsDependency) : Fragment(),
    RemyndDetailsView {
    private lateinit var binding: FragmentRemyndDetailsBinding

    @Inject
    internal lateinit var presenter: RemyndDetailsPresenter

    companion object {
        val tag: String = RemyndDetailsFragment::class.java.simpleName
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerRemyndDetailsComponent.factory()
            .create(this, dependency)
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRemyndDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presenter.bind(arguments?.getLong(REMYND_ID))
    }

    override fun onDestroy() {
        presenter.unbind()
        super.onDestroy()
    }

    override fun observeRepeat(): Flow<Boolean> = binding.sRepeat.observe()

    override fun observeDates(): Flow<List<DateItem>> = binding.llDates.observe()
}
