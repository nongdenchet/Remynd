package com.rain.remynd.ui.details

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rain.remynd.databinding.FragmentRemyndDetailsBinding
import javax.inject.Inject

private const val REMYND_DETAILS_FORM = "REMYND_DETAILS_FORM"

class RemyndDetailsFragment(private val dependency: RemyndDetailsDependency) : Fragment(),
    RemyndDetailsView {
    private lateinit var binding: FragmentRemyndDetailsBinding

    @Inject
    internal lateinit var presenter: RemyndDetailsPresenter

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        TODO("Implement this")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presenter.bind(savedInstanceState?.getParcelable(REMYND_DETAILS_FORM))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(REMYND_DETAILS_FORM, presenter.generateForm())
    }

    override fun onDestroy() {
        presenter.unbind()
        super.onDestroy()
    }
}
