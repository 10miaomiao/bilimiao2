package com.a10miaomiao.bilimiao.widget.picker

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.store.model.DateModel
import java.util.*

class DatePickerView(context: Context) : FrameLayout(context) {

    private val mYearPicker: PickerView by lazy {
        findViewById(R.id.mYearPicker)
    }
    private val mMonthPicker: PickerView by lazy {
        findViewById(R.id.mMonthPicker)
    }
    private val mDatePicker: PickerView by lazy {
        findViewById(R.id.mDatePicker)
    }

    var date = DateModel()
        set(value) {
            if (value.year > 2008) mYearPicker?.value = value.year - 2008
            mMonthPicker?.value = value.month
            mDatePicker?.maxValue = value.getMonthDate()
            mDatePicker?.value = value.date
            field = value
        }

    var onChanged: ((date: DateModel) -> Unit)? = null

    init {
        View.inflate(context, R.layout.layout_date_picker, this)
        val now = Date()
        val yearSize = now.year - 108
        mYearPicker.displayedValues = Array(yearSize) { (it + 2009).toString() }
        mYearPicker.minValue = 1
        mYearPicker.maxValue = yearSize
        mMonthPicker.minValue = 1
        mMonthPicker.maxValue = 12
        mDatePicker.minValue = 1
        mDatePicker.maxValue = 31

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
        onChanged?.invoke(date)
    }

}