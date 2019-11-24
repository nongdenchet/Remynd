package com.rain.remynd.view

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import mobi.upod.timedurationpicker.TimeDurationPicker
import mobi.upod.timedurationpicker.TimeDurationPickerDialog

private const val DURATION = "DURATION"

class PickerDialogFragment : DialogFragment() {
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return TimeDurationPickerDialog(
            activity,
            TimeDurationPickerDialog.OnDurationSetListener { _, duration ->
                listener?.onResult(duration)
            },
            arguments?.getLong(DURATION) ?: 0,
            TimeDurationPicker.HH_MM
        )
    }
}
