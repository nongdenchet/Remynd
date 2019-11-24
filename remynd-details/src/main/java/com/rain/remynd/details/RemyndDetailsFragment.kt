package com.rain.remynd.details

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.rain.remynd.common.text
import com.rain.remynd.details.databinding.FragmentRemyndDetailsBinding
import com.rain.remynd.view.PickerDialogFragment
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

const val REMYND_ID = "REMYND_ID"
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.sVibrate.setOnCheckedChangeListener { _, isChecked ->
            presenter.setVibrate(isChecked)
        }
        binding.sEnabled.setOnCheckedChangeListener { _, isChecked ->
            presenter.setEnabled(isChecked)
        }
        binding.llDates.setOnDataChangeListener { presenter.setDateItems(it) }
        binding.tvCancel.setOnClickListener { goBack() }
        binding.tvSave.setOnClickListener { presenter.save() }
    }

    override fun goBack() {
        fragmentManager?.popBackStack()
    }

    private fun updateTimePicker(timeInfo: TimeInfo) {
        val tag = TimePickerDialog::class.java.simpleName
        val listener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute, _ ->
            presenter.setTime(hourOfDay, minute)
        }

        fragmentManager?.run {
            val tpd = findFragmentByTag(tag) as TimePickerDialog?
            tpd?.run { onTimeSetListener = listener }
        }
        binding.tvClock.text = timeInfo.clock
        binding.tvSelectedTime.text = timeInfo.displayTime
        binding.flTime.setOnClickListener {
            val tpd = TimePickerDialog.newInstance(
                listener,
                timeInfo.hour,
                timeInfo.minute,
                false
            )
            fragmentManager?.run { tpd.show(this, tag) }
        }
    }

    private fun updateDatePicker(dateInfo: DateInfo) {
        val tag = DatePickerDialog::class.java.simpleName
        val listener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            presenter.setDate(year, monthOfYear, dayOfMonth)
        }

        binding.tvDateInfo.text = dateInfo.displayDate
        fragmentManager?.run {
            val dpd = findFragmentByTag(tag) as DatePickerDialog?
            dpd?.run { onDateSetListener = listener }
        }
        binding.ivCalendar.setOnClickListener {
            val dpd = DatePickerDialog.newInstance(
                listener,
                dateInfo.year,
                dateInfo.month,
                dateInfo.day
            )
            fragmentManager?.run { dpd.show(this, tag) }
        }
    }

    private fun updateInterval(intervalInfo: IntervalInfo) {
        val tag = PickerDialogFragment::class.java.simpleName
        val listener = object : PickerDialogFragment.Listener {
            override fun onResult(duration: Long) = presenter.setInterval(duration)
        }

        binding.tvRemindValue.text = intervalInfo.display
        fragmentManager?.run {
            val pdf = findFragmentByTag(tag) as PickerDialogFragment?
            pdf?.run { setListener(listener) }
        }
        binding.tvRemindValue.setOnClickListener {
            val pdf = PickerDialogFragment.newInstance(intervalInfo.interval, listener)
            fragmentManager?.run { pdf.show(this, tag) }
        }
    }

    override fun render(vm: RemyndDetailsViewModel) {
        updateDatePicker(vm.dateInfo)
        updateTimePicker(vm.timeInfo)
        updateInterval(vm.intervalInfo)
        binding.sVibrate.isChecked = vm.vibrate
        binding.sEnabled.isChecked = vm.enabled
        binding.llDates.updateDates(vm.dateItems)
        // Two way binding
        vm.content.let {
            if (!it.userInput) {
                binding.edtTitle.setText(it.content)
                binding.edtTitle.setSelection(it.content.length)
            }
        }
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

    override fun contentChanges(): Flow<String> = binding.edtTitle.text()

    override fun showMessage(content: String) {
        context?.run {
            Toast.makeText(this, content, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        presenter.unbind()
        super.onDestroy()
    }
}
