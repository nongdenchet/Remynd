package com.rain.remynd.ui.details

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rain.remynd.databinding.FragmentRemyndDetailsBinding
import com.rain.remynd.support.formatTime
import com.rain.remynd.support.observe
import com.rain.remynd.view.DateItem
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject

internal const val REMYND_ID = "REMYND_ID"
internal const val REMYND_FORM = "REMYND_FORM"

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

    private fun updateTimePicker(calendar: Calendar) {
        binding.tvClock.text = formatTime("a", calendar.time)
        binding.tvSelectedTime.text = formatTime("hh:mm", calendar.time)
        binding.tvSelectedTime.setOnClickListener {
            val dpd = TimePickerDialog.newInstance(
                { _, hourOfDay, minute, _ ->
                    presenter.setTime(hourOfDay, minute)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            )
            dpd.show(fragmentManager!!, TimePickerDialog::class.java.name)
        }
    }

    private fun updateDatePicker(calendar: Calendar) {
        binding.tvDateInfo.text = formatTime("EEE, dd MMM", calendar.time)
        binding.ivCalendar.setOnClickListener {
            val dpd = DatePickerDialog.newInstance(
                { _, year, monthOfYear, dayOfMonth ->
                    presenter.setDate(year, monthOfYear, dayOfMonth)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dpd.show(fragmentManager!!, DatePickerDialog::class.java.simpleName)
        }
    }

    override fun render(form: RemyndForm) {
        val date = (form.dateConfig as DateConfig.SingleDate).date
        updateDatePicker(date)
        updateTimePicker(date)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presenter.bind(
            arguments?.getLong(REMYND_ID),
            savedInstanceState?.getParcelable(REMYND_FORM)
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(REMYND_FORM, presenter.currentForm())
    }

    override fun onDestroy() {
        presenter.unbind()
        super.onDestroy()
    }

    override fun observeDates(): Flow<List<DateItem>> = binding.llDates.observe()
}
