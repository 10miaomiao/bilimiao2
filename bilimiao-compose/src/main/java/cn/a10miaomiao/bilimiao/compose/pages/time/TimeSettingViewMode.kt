package cn.a10miaomiao.bilimiao.compose.pages.time

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.pages.time.components.getMonthDayNum
import com.a10miaomiao.bilimiao.comm.store.TimeSettingStore
import com.a10miaomiao.bilimiao.comm.store.model.DateModel
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.util.Date

class TimeSettingViewMode(
    override val di: DI,
) : ViewModel(), DIAware {

    private val timeSettingStore by instance<TimeSettingStore>()
    private val pageNavigation by instance<PageNavigation>()

//    private val calendar = Calendar.getInstance()

    val cardIndex = MutableStateFlow(timeSettingStore.state.timeType)

    val minDate = DateModel().also {
        it.year = 2009
        it.month = 1
        it.date = 1
    }

    val maxDate = DateModel().also {
        it.setDate(Date())
//        it.year = Calendar.getInstance().get(Calendar.YEAR)
//        it.month = Calendar.getInstance().get(Calendar.MONTH + 1)
//        it.date = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    }

    val yearCount = maxDate.year - minDate.year + 1

    val currentTime = MutableStateFlow(TimeInfo().apply {
        val now = Date()
        timeTo.setDate(now)
        timeFrom.set(timeTo.getTimeByGapCount(-7)) //最近7天
    })

    val monthTime = MutableStateFlow(TimeInfo().apply {
        val dateModel = timeSettingStore.state.timeFrom.copy()
        dateModel.date = 1 // 当月第一天
        timeFrom.set(dateModel)
        dateModel.date = getMonthDayNum(dateModel.year, dateModel.month) // 当月最后一天
        timeTo.set(dateModel)
    })

    val customTime = MutableStateFlow(TimeInfo().apply {
        timeFrom.set(timeSettingStore.state.timeFrom)
        timeTo.set(timeSettingStore.state.timeTo)
    })

    fun setMonthTime(year: Int, month: Int) {
        monthTime.value = TimeInfo().apply {
            val dateModel = DateModel()
            dateModel.year = year
            dateModel.month = month
            dateModel.date = 1
            timeFrom.set(dateModel)
            dateModel.date = getMonthDayNum(dateModel.year, dateModel.month)
            timeTo.set(dateModel)
        }
    }

    fun setCustomTime(start: DateModel?, end: DateModel?) {
        customTime.value = TimeInfo().apply {
            if (start != null && end != null) {
                timeFrom.set(start)
                timeTo.set(end)
            } else if (start != null) {
                PopTip.show("时间间隔不能大于30天")
            } else {
                timeFrom.year = -1
            }
        }
    }

    fun setCurrentCardAsCurrent(avtive: Boolean) {
        cardIndex.value = TimeSettingStore.TIME_TYPE_CURRENT
    }

    fun setCurrentCardAsMonth(avtive: Boolean) {
        cardIndex.value = TimeSettingStore.TIME_TYPE_MONTH
    }

    fun setCurrentCardAsCustom(avtive: Boolean) {
        cardIndex.value = TimeSettingStore.TIME_TYPE_CUSTOM
    }

    fun save() {
        val timeInfo = (when (cardIndex.value) {
            TimeSettingStore.TIME_TYPE_CURRENT -> currentTime.value
            TimeSettingStore.TIME_TYPE_MONTH -> monthTime.value
            TimeSettingStore.TIME_TYPE_CUSTOM -> customTime.value
            else -> currentTime.value
        })
        if (timeInfo.timeFrom.year == -1) {
            PopTip.show("请选择时间范围")
            return
        }
        timeSettingStore.setTime(
            cardIndex.value,
            timeInfo.timeFrom.copy(),
            timeInfo.timeTo.copy(),
        )
        timeSettingStore.save()
        pageNavigation.popBackStack()
    }

    class TimeInfo(
        val timeFrom: DateModel = DateModel(),
        val timeTo: DateModel = DateModel(),
    )
}