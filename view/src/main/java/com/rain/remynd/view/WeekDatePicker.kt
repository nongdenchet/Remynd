package com.rain.remynd.view

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.core.content.ContextCompat
import com.rain.remynd.common.indexToDateSymbol
import java.util.Calendar

data class DateItem(val dateInWeek: Int, val checked: Boolean) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(dateInWeek)
        parcel.writeByte(if (checked) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Creator<DateItem> {
        override fun createFromParcel(parcel: Parcel): DateItem = DateItem(parcel)
        override fun newArray(size: Int): Array<DateItem?> = arrayOfNulls(size)
    }
}

private class SavedState : View.BaseSavedState {
    val items: List<DateItem>

    constructor(source: Parcel) : super(source) {
        items = mutableListOf<DateItem>().apply {
            source.readList(this as List<*>, DateItem::class.java.classLoader)
        }
    }

    constructor(superState: Parcelable?, items: List<DateItem>) : super(superState) {
        this.items = items
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeList(items)
    }

    companion object CREATOR : Creator<SavedState> {
        override fun createFromParcel(parcel: Parcel): SavedState = SavedState(parcel)
        override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
    }
}

private val defaultItems: List<DateItem> = Array(7) {
    DateItem(Calendar.SUNDAY + it, false)
}.toList()

class WeekDatePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyle, defStyleRes) {
    private var listener: ((items: List<DateItem>) -> Unit)? = null
    private var items: List<DateItem> = defaultItems

    init {
        initialize()
    }

    private fun initialize() {
        orientation = HORIZONTAL
        isClickable = true
        updateDates(items)
    }

    @MainThread
    fun updateDates(items: List<DateItem>) {
        this.items = items
        items.forEachIndexed { index, item ->
            getOrCreateViewHolder(index).run {
                tvTime.text = indexToDateSymbol[item.dateInWeek].toString()
                setChecked(tvTime, item.checked)
            }
        }
    }

    private fun setChecked(tvTime: TextView, checked: Boolean) {
        tvTime.setTextColor(
            ContextCompat.getColor(
                context,
                if (checked) R.color.colorSecondary
                else R.color.white
            )
        )
        tvTime.setBackgroundResource(
            if (checked) R.drawable.round_background_selected
            else R.drawable.round_background_normal
        )
    }

    fun setOnDataChangeListener(listener: ((items: List<DateItem>) -> Unit)?) {
        this.listener = listener
    }

    private fun getOrCreateViewHolder(index: Int): ViewHolder {
        if (index < childCount) {
            return getChildAt(index).getTag(R.id.view_tag) as ViewHolder
        }

        val root = LayoutInflater.from(context).inflate(R.layout.item_remynd_date, this, false)
        val viewHolder = ViewHolder(root)
        root.setTag(R.id.view_tag, viewHolder)
        this.addView(root, LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply { weight = 1F })
        viewHolder.tvTime.setOnClickListener {
            updateItem(viewHolder.tvTime, index)
        }
        return viewHolder
    }

    private fun updateItem(tvTime: TextView, index: Int) {
        this.items = items.mapIndexed { i, dateItem ->
            if (i == index) dateItem.copy(checked = !dateItem.checked)
            else dateItem
        }
        setChecked(tvTime, items[index].checked)
        listener?.invoke(items)
    }

    override fun onSaveInstanceState(): Parcelable? {
        return SavedState(super.onSaveInstanceState(), items)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        items = state.items
        updateDates(items)
        listener?.invoke(items)
    }

    private class ViewHolder(root: View) {
        val tvTime: TextView = root.findViewById(R.id.tvTime)
    }
}
