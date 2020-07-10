package com.a10miaomiao.bilimiao.store

import android.arch.lifecycle.ViewModel
import android.content.Context
import com.a10miaomiao.bilimiao.ui.commponents.model.DateModel
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.bilimiao.utils.SettingUtil
import com.a10miaomiao.miaoandriod.MiaoLiveData
import com.a10miaomiao.miaoandriod.mergeMiaoObserver
import java.util.*

class TimeSettingStore(
        val context: Context
): ViewModel() {
    val timeFrom = MiaoLiveData(read(ConstantUtil.TIME_FROM))
    val timeTo = MiaoLiveData(read(ConstantUtil.TIME_TO))

    fun read(type: String): DateModel {
        val dateModel = DateModel()
        val timeType = SettingUtil.getInt(context, ConstantUtil.TIME_TYPE, 0)
        if (timeType == 0) {
            val now = Date()
            dateModel.setDate(now)
            if (type == ConstantUtil.TIME_FROM) {
                return dateModel.getTimeByGapCount(-7)
            }
            return dateModel
        }
        val timeStr = SettingUtil.getString(context, type, "20180909")
        dateModel.setValue(timeStr)
        return dateModel
    }

    fun save(timeType: Int, isLink: Boolean) {
        SettingUtil.putString(context, ConstantUtil.TIME_FROM, timeFromValue)
        SettingUtil.putString(context, ConstantUtil.TIME_TO, timeToValue)
        SettingUtil.putBoolean(context, ConstantUtil.TIME_IS_LINK, isLink)
        SettingUtil.putInt(context, ConstantUtil.TIME_TYPE, timeType)
    }

    fun observe() = mergeMiaoObserver(+timeFrom, +timeTo)

    val value get() = timeFrom().getValue("-") + " 至 " + timeTo().getValue("-")
    val timeFromValue get() = timeFrom().getValue()
    val timeToValue get() = timeTo().getValue()

}