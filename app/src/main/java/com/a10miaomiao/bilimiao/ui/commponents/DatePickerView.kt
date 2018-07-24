package com.a10miaomiao.bilimiao.ui.commponents

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.renderscript.ScriptGroup
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.ui.commponents.model.DateModel
import com.a10miaomiao.bilimiao.ui.time.TimeSettingFragment
import com.a10miaomiao.bilimiao.ui.widget.PickerView
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.bilimiao.utils.TimeSettingUtil
import com.a10miaomiao.miaoandriod.MiaoAnkoContext
import com.a10miaomiao.miaoandriod.MiaoBinding

import com.a10miaomiao.miaoandriod.MiaoView
import com.a10miaomiao.miaoandriod.bind
import kotlinx.android.synthetic.main.layout_date_picker.view.*
import java.time.chrono.MinguoDate
import java.util.*

class DatePickerView(context: Context) : MiaoView(context) {

    val date: DateModel = DateModel(binding)

    override fun layout() = R.layout.layout_date_picker
    private var _onChanged: ((date: DateModel) -> Unit)? = null

    init {
        onCreateView()
        val now = Date()
        val yearSize = now.year - 108
        mYearPicker.displayedValues = Array(yearSize, { (it + 2009).toString() })
        mYearPicker.minValue = 1
        mYearPicker.maxValue = yearSize
        mMonthPicker.minValue = 1
        mMonthPicker.maxValue = 12
        mDatePicker.minValue = 1
        mDatePicker.maxValue = 31

        binding.bind(date::year) { if (it > 2008) mYearPicker.setValue(it - 2008) }
        binding.bind(date::month, mMonthPicker::setValue)
        binding.bind(date::date, mDatePicker::setValue)
        binding.bind({
            mDatePicker.maxValue = TimeSettingUtil.getMonthDate(date.year, date.month)
            mDatePicker.value = date.date
        })
        mYearPicker.setOnValueChangedListener(::onValueChange)
        mMonthPicker.setOnValueChangedListener(::onValueChange)
        mDatePicker.setOnValueChangedListener(::onValueChange)
    }


    private fun onValueChange(picker: PickerView, oldVal: Int, newVal: Int) {
        when (picker) {
            mYearPicker -> {
                date.year = newVal + 2008
            }
            mMonthPicker -> {
                date.month = newVal
            }
            mDatePicker -> {
                date.date = newVal
            }
        }
        binding.updateView()
        _onChanged?.invoke(date)
    }

    fun onChanged(callback: ((date: DateModel) -> Unit)) {
        _onChanged = callback
    }
}