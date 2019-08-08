package com.a10miaomiao.bilimiao.ui.time

import android.arch.lifecycle.ViewModel
import android.content.Context
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.model.DateModel
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.bilimiao.utils.SettingUtil
import com.a10miaomiao.miaoandriod.MiaoLiveData
import java.util.*

class TimeSettingViewModel(
        val context: Context
) : ViewModel() {

    var timeSettingStore = MainActivity.of(context).timeSettingStore
    var spinnerSelected = MiaoLiveData(0)
    var timeFrom = MiaoLiveData(timeSettingStore.timeFrom())
    var timeTo = MiaoLiveData(timeSettingStore.timeTo())
    var isLink = MiaoLiveData(false)
    var gapCount = MiaoLiveData(0)


    init {
        isLink set SettingUtil.getBoolean(context, ConstantUtil.TIME_IS_LINK, false)
        spinnerSelected set SettingUtil.getInt(context, ConstantUtil.TIME_TYPE, 0)
    }

    fun saveTime() {
        timeSettingStore.timeFrom set timeFrom()
        timeSettingStore.timeTo set timeTo()
        timeSettingStore.save(-spinnerSelected, -isLink)
    }

    fun changedSpinnerItem(position: Int) {
        spinnerSelected set position
        when (position) {
            0 -> {
                val dateModel = DateModel()
                val now = Date()
                dateModel.setDate(now)
                timeFrom set dateModel.getTimeByGapCount(-7) //最近7天
                timeTo set dateModel
            }
            1 -> {
                timeFrom().date = 1
                timeTo set timeFrom().getTimeToByMonth()
            }
        }
        gapCount set timeFrom().getGapCount(timeTo())
    }

    fun changedMonthPicker(dateModel: DateModel) {
        timeFrom set dateModel.getTimeFromByMonth()
        timeTo set dateModel.getTimeToByMonth()
    }

    fun changedTimeFromPicker(dateModel: DateModel) {
        timeFrom set dateModel
        val timeFromV = -timeFrom
        if (isLink()) {
            timeTo set timeFromV.getTimeByGapCount(-gapCount)
        } else {
            gapCount set timeFromV.getGapCount(timeTo())
        }
    }

    fun changedTimeToPicker(dateModel: DateModel) {
        timeTo set dateModel
        val timeToV = -timeTo
        if (isLink()) {
            timeFrom set timeToV.getTimeByGapCount(-gapCount)
        } else {
            gapCount set timeFrom().getGapCount(timeToV)
        }
    }


}