package com.a10miaomiao.bilimiao.ui.commponents

import android.content.Context
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.ui.commponents.model.DateModel
import com.a10miaomiao.bilimiao.ui.widget.PickerView
import com.a10miaomiao.miaoandriod.MiaoView
import com.a10miaomiao.miaoandriod.bind
import kotlinx.android.synthetic.main.layout_date_picker.view.*
import java.util.*

class MonthPickerView(context: Context) : MiaoView(context) {

    lateinit var date: DateModel
    private var _onChanged: ((date: DateModel) -> Unit)? = null

    override fun layout() = R.layout.layout_month_picker

    init {
        onCreateView()
        val now = Date()
        val yearSize = now.year - 108
        mYearPicker.displayedValues = Array(yearSize, { (it + 2009).toString() })
        mYearPicker.minValue = 1
        mYearPicker.maxValue = yearSize
        mMonthPicker.minValue = 1
        mMonthPicker.maxValue = 12

        date = DateModel(binding)
        binding.bind(date::year) { if (it > 2008) mYearPicker.setValue(it - 2008) }
        binding.bind(date::month, mMonthPicker::setValue)
        mYearPicker.setOnValueChangedListener(::onValueChange)
        mMonthPicker.setOnValueChangedListener(::onValueChange)
    }

    private fun onValueChange(picker: PickerView, oldVal: Int, newVal: Int) {
        when (picker) {
            mYearPicker -> {
                date.year = newVal + 2008
            }
            mMonthPicker -> {
                date.month = newVal
            }
        }
        binding.updateView()
        _onChanged?.invoke(date)
    }

    fun onChanged(callback: ((date: DateModel) -> Unit)): MonthPickerView {
        _onChanged = callback
        return this
    }
}