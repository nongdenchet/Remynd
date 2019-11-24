package com.rain.remynd.list

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rain.remynd.list.databinding.ItemRemyndListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch

private val diffCallback = object : DiffUtil.ItemCallback<RemyndItemViewModel>() {
    override fun areItemsTheSame(
        oldItem: RemyndItemViewModel,
        newItem: RemyndItemViewModel
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: RemyndItemViewModel,
        newItem: RemyndItemViewModel
    ): Boolean {
        return oldItem == newItem
    }
}

sealed class ItemEvent {
    data class ClickEvent(val id: Long) : ItemEvent()
    data class LongClickEvent(val id: Long) : ItemEvent()
    data class CheckEvent(val id: Long, val value: Boolean) : ItemEvent()
    data class SwitchEvent(val id: Long, val active: Boolean, val position: Int) : ItemEvent()
}

@Suppress("EXPERIMENTAL_API_USAGE")
internal class RemyndListAdapter :
    ListAdapter<RemyndItemViewModel, RemyndListAdapter.ViewHolder>(diffCallback),
    LifecycleObserver {
    private val tag = RemyndListAdapter::class.java.simpleName
    private val parentJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + parentJob)
    private val eventChannel = BroadcastChannel<ItemEvent>(Channel.CONFLATED)

    fun itemEvents(): Flow<ItemEvent> = eventChannel.asFlow()

    private fun safeGetItem(position: Int): RemyndItemViewModel? {
        if (position < 0 || position >= itemCount) {
            return null
        }

        return getItem(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return ViewHolder(ItemRemyndListBinding.inflate(inflater, parent, false)).apply {
            binding.sEnabled.setOnCheckedChangeListener { _, isChecked ->
                val pos = adapterPosition
                safeGetItem(pos)?.run {
                    scope.launch(Dispatchers.Main) {
                        eventChannel.send(ItemEvent.SwitchEvent(id, isChecked, pos))
                    }
                }
            }
            binding.cbCheck.setOnCheckedChangeListener { _, isChecked ->
                val pos = adapterPosition
                safeGetItem(pos)?.run {
                    scope.launch(Dispatchers.Main) {
                        eventChannel.send(ItemEvent.CheckEvent(id, isChecked))
                    }
                }
            }
            binding.root.setOnLongClickListener {
                val pos = adapterPosition
                safeGetItem(pos)?.run {
                    scope.launch(Dispatchers.Main) {
                        eventChannel.send(ItemEvent.LongClickEvent(id))
                    }
                }.let { true }
            }
            binding.root.setOnClickListener {
                val pos = adapterPosition
                safeGetItem(pos)?.run {
                    scope.launch(Dispatchers.Main) {
                        eventChannel.send(ItemEvent.ClickEvent(id))
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        Log.d(tag, "onDestroy")
        parentJob.cancel()
    }

    inner class ViewHolder(val binding: ItemRemyndListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RemyndItemViewModel) {
            binding.sEnabled.isChecked = item.active
            binding.tvTime.text = item.time
            binding.tvContent.text = item.content
            binding.tvClock.text = item.clock
            binding.tvDate.text = item.date
            binding.cbCheck.isChecked = item.isChecked
            binding.cbCheck.visibility = if (item.isEditable) View.VISIBLE else View.GONE
            binding.sEnabled.visibility = if (item.isEditable) View.GONE else View.VISIBLE
        }
    }
}
