package com.rain.remynd.ui.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.rain.remynd.databinding.FragmentRemyndListBinding
import com.rain.remynd.support.dependency

class RemyndListFragment : Fragment() {
    private lateinit var component: RemyndListComponent
    private lateinit var binding: FragmentRemyndListBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        component = DaggerRemyndListComponent.factory()
            .create(this, context.dependency())
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
    }
}
