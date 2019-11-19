package com.rain.remynd.view

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import mobi.upod.timedurationpicker.TimeDurationPicker
import mobi.upod.timedurationpicker.TimeDurationPickerDialog

internal const val DURATION = "DURATION"

class PickerDialogFragment : DialogFragment(), TimeDurationPickerDialog.OnDurationSetListener {
    companion object {
        fun newInstance(duration: Long, listener: Listener): PickerDialogFragment {
            return PickerDialogFragment().apply {
                setListener(listener)
                arguments = Bundle().apply {
                    putLong(DURATION, duration)
                }
            }
        }
    }

    private var listener: Listener? = null

    interface Listener {
        fun onResult(duration: Long)
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    override fun onDurationSet(view: TimeDurationPicker, duration: Long) {
        listener?.onResult(duration)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return TimeDurationPickerDialog(activity, this, getInitialDuration(), setTimeUnits())
    }

    private fun setTimeUnits(): Int {
        return TimeDurationPicker.HH_MM
    }

    private fun getInitialDuration(): Long {
        return arguments?.getLong(DURATION) ?: 0
    }
}
