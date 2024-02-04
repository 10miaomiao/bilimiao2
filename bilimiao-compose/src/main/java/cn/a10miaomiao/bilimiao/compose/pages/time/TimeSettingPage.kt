package cn.a10miaomiao.bilimiao.compose.pages.time

import android.widget.Toast
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.pages.time.commponents.*
import com.a10miaomiao.bilimiao.comm.store.TimeSettingStore
import com.a10miaomiao.bilimiao.comm.store.model.DateModel
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance
import java.util.*

class TimeSettingPage : ComposePage() {
    override val route: String
        get() = "time/setting"

    @Composable
    override fun AnimatedContentScope.Content(navEntry: NavBackStackEntry) {
        val viewModel = diViewModel<TimeSettingPageViewMode>()
        TimeSettingPageContent(viewModel)
    }

}

internal class TimeSettingPageViewMode(
    override val di: DI,
) : ViewModel(), DIAware {

    private val timeSettingStore by instance<TimeSettingStore>()

    private val fragment by instance<Fragment>()

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
                Toast.makeText(fragment.requireActivity(), "时间间隔不能大于30天", Toast.LENGTH_LONG)
                    .show()
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
            Toast.makeText(fragment.requireActivity(), "请选择时间范围", Toast.LENGTH_SHORT)
                .show()
            return
        }
        timeSettingStore.setTime(
            cardIndex.value,
            timeInfo.timeFrom.copy(),
            timeInfo.timeTo.copy(),
        )
        timeSettingStore.save()
        fragment.findNavController().popBackStack()
    }

    class TimeInfo(
        val timeFrom: DateModel = DateModel(),
        val timeTo: DateModel = DateModel(),
    )
}

@Composable
internal fun TimeSettingPageContent(
    viewModel: TimeSettingPageViewMode,
) {
    PageConfig(title = "时光姬-时间线设置")

    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val cardIndex = viewModel.cardIndex.collectAsState().value

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(windowInsets.topDp.dp))

            TimeCard(
                title = "当前时间线",
                active = cardIndex == TimeSettingStore.TIME_TYPE_CURRENT,
                onActiveChange = viewModel::setCurrentCardAsCurrent,
            ) {
                CurrentTime(viewModel)
            }
            TimeCard(
                title = "按月份选择",
                active = cardIndex == TimeSettingStore.TIME_TYPE_MONTH,
                onActiveChange = viewModel::setCurrentCardAsMonth,
            ) {
                MonthTime(viewModel)
            }
            TimeCard(
                title = "自定义范围",
                active = cardIndex == TimeSettingStore.TIME_TYPE_CUSTOM,
                onActiveChange = viewModel::setCurrentCardAsCustom,
            ) {
                CustomTime(viewModel)
            }

            Spacer(modifier = Modifier.height(windowInsets.bottomDp.dp + 50.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
                .padding(bottom = windowInsets.bottomDp.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = viewModel::save,
            ) {
                Text(
                    text = "确定",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

    }

}


