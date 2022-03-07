package com.a10miaomiao.bilimiao.page.time

import android.content.Context
import androidx.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.Bilimiao
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.store.Store
import com.a10miaomiao.bilimiao.store.TimeSettingStore
import com.a10miaomiao.bilimiao.widget.picker.DateModel
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.util.*

class TimeSettingViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val timeSettingStore: TimeSettingStore by instance()


    var spinnerSelected = 0
    var timeFrom = timeSettingStore.state.timeFrom.copy()
    var timeTo = timeSettingStore.state.timeTo.copy()
    var isLink = false
    var gapCount = 0

    init {
        val sp = context.getSharedPreferences(Bilimiao.APP_NAME, Context.MODE_PRIVATE)
        isLink = sp.getBoolean(TimeSettingStore.TIME_IS_LINK, false)
        spinnerSelected = sp.getInt(TimeSettingStore.TIME_TYPE, 0)
    }

    fun saveTime() {
        timeSettingStore.setTime(timeFrom.copy(), timeTo.copy())
        timeSettingStore.save(spinnerSelected, isLink)
    }

    fun changedSpinnerItem(position: Int) {
        ui.setState {
            spinnerSelected = position
            when (position) {
                0 -> {
                    val dateModel = DateModel()
                    val now = Date()
                    dateModel.setDate(now)
                    timeFrom = dateModel.getTimeByGapCount(-7) //最近7天
                    timeTo = dateModel
                }
                1 -> {
                    timeFrom.date = 1
                    timeTo = timeFrom.getTimeToByMonth()
                }
            }
            gapCount = timeFrom.getGapCount(timeTo)
        }
    }

    fun changedMonthPicker(dateModel: DateModel) {
        ui.setState {
            timeFrom = dateModel.getTimeFromByMonth()
            timeTo = dateModel.getTimeToByMonth()
        }
    }

    fun changedTimeFromPicker(dateModel: DateModel) {
        ui.setState {
            timeFrom = dateModel
            if (isLink) {
                timeTo = timeFrom.getTimeByGapCount(gapCount)
            } else {
                gapCount = timeFrom.getGapCount(timeTo)
            }
        }
    }

    fun changedTimeToPicker(dateModel: DateModel) {
        ui.setState {
            timeTo = dateModel
            if (isLink) {
                timeFrom = timeTo.getTimeByGapCount(gapCount)
            } else {
                gapCount = timeFrom.getGapCount(timeTo)
            }
        }
    }
}