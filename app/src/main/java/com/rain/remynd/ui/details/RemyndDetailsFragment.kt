package com.rain.remynd.ui.details

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.rain.remynd.databinding.FragmentRemyndDetailsBinding
import com.rain.remynd.support.text
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import kotlinx.coroutines.flow.Flow
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
        binding.tvClock.text = timeInfo.clock
        binding.tvSelectedTime.text = timeInfo.displayTime
        binding.tvSelectedTime.setOnClickListener {
            val dpd = TimePickerDialog.newInstance(
                { _, hourOfDay, minute, _ ->
                    presenter.setTime(hourOfDay, minute)
                },
                timeInfo.hour,
                timeInfo.minute,
                false
            )
            fragmentManager?.run {
                dpd.show(this, TimePickerDialog::class.java.simpleName)
            }
        }
    }

    private fun updateDatePicker(dateInfo: DateInfo) {
        binding.tvDateInfo.text = dateInfo.displayDate
        binding.ivCalendar.setOnClickListener {
            val dpd = DatePickerDialog.newInstance(
                { _, year, monthOfYear, dayOfMonth ->
                    presenter.setDate(year, monthOfYear, dayOfMonth)
                },
                dateInfo.year,
                dateInfo.month,
                dateInfo.day
            )
            fragmentManager?.run {
                dpd.show(this, DatePickerDialog::class.java.simpleName)
            }
        }
    }

    override fun render(vm: RemyndDetailsViewModel) {
        updateDatePicker(vm.dateInfo)
        updateTimePicker(vm.timeInfo)
        binding.sVibrate.isChecked = vm.vibrate
        binding.sEnabled.isChecked = vm.enabled
        binding.edtTitle.setText(vm.content)
        binding.edtTitle.setSelection(vm.content.length)
        binding.llDates.updateDates(vm.dateItems)
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

    override fun showError(content: String) {
        context?.run {
            Toast.makeText(this, content, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        presenter.unbind()
        super.onDestroy()
    }
}
