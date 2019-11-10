package com.rain.remynd.ui.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.rain.remynd.databinding.FragmentRemyndListBinding
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RemyndListFragment(
    private val dependency: RemyndListDependency
) : Fragment(), RemyndListView {
    private lateinit var component: RemyndListComponent
    private lateinit var binding: FragmentRemyndListBinding

    @Inject
    lateinit var adapter: RemyndListAdapter
    @Inject
    lateinit var presenter: RemyndListPresenter
    @Inject
    lateinit var lifecycleObservers: Set<@JvmSuppressWildcards LifecycleObserver>

    companion object {
        fun tag(): String = RemyndListFragment::class.java.simpleName
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        component = DaggerRemyndListComponent.factory()
            .create(this, dependency)
        component.inject(this)
        lifecycleObservers.forEach { lifecycle.addObserver(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRemyndListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvReminds.layoutManager = LinearLayoutManager(context)
        binding.rvReminds.adapter = adapter
        (binding.rvReminds.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleObservers.forEach { lifecycle.removeObserver(it) }
    }

    override fun render(items: List<RemyndItemViewModel>) = adapter.submitList(items)

    override fun itemEvents(): Flow<ItemEvent> = adapter.itemEvents()
}
