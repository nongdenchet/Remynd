package com.rain.remynd.ui.list

import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.rain.remynd.R
import com.rain.remynd.databinding.FragmentRemyndListBinding
import com.rain.remynd.support.clicks
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.math.abs

class RemyndListFragment(
    private val dependency: RemyndListDependency
) : Fragment(), RemyndListView {
    private lateinit var binding: FragmentRemyndListBinding

    @Inject
    internal lateinit var remyndListAdapter: RemyndListAdapter
    @Inject
    internal lateinit var presenter: RemyndListPresenter
    @Inject
    internal lateinit var lifecycleObservers: Set<@JvmSuppressWildcards LifecycleObserver>

    companion object {
        val tag: String = RemyndListFragment::class.java.simpleName
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerRemyndListComponent.factory()
            .create(this, dependency)
            .inject(this)
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
        setUpRecyclerView()
        setUpAppBar()
        setUpClockView()
    }

    private fun setUpClockView() {
        binding.headerContent.tvCurrentTime.run {
            val hourSpan = SpannableString("HH").apply {
                setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorSecondary)),
                    0,
                    length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            val minuteSpan = SpannableString(":mm").apply {
                setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, R.color.white)),
                    0,
                    length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            format12Hour = TextUtils.concat(hourSpan, minuteSpan)
        }
    }

    private fun setUpRecyclerView() {
        binding.rvReminds.run {
            layoutManager = LinearLayoutManager(context)
            adapter = remyndListAdapter
            (itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
        }
    }

    private fun setUpAppBar() {
        binding.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { layout, offset ->
            val percentage = abs(offset).toFloat() / layout.totalScrollRange
            binding.toolbarContent.tvToolbar.alpha = percentage
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleObservers.forEach { lifecycle.removeObserver(it) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presenter.bind()
    }

    override fun addRemyndClicks(): Flow<Unit> = binding.toolbarContent.tvAdd.clicks()

    override fun render(items: List<RemyndItemViewModel>) = remyndListAdapter.submitList(items)

    override fun renderActiveCount(value: String) {
        binding.headerContent.tvTotal.text = value
    }

    override fun itemEvents(): Flow<ItemEvent> = remyndListAdapter.itemEvents()

    override fun showError(content: String, position: Int) {
        remyndListAdapter.notifyItemChanged(position)
        context?.run {
            Toast.makeText(this, content, Toast.LENGTH_LONG).show()
        }
    }
}
