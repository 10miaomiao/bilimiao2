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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.pages.time.components.*
import com.a10miaomiao.bilimiao.comm.store.TimeSettingStore
import com.a10miaomiao.bilimiao.comm.store.model.DateModel
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance
import java.util.*

@Serializable
class TimeSettingPage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel = diViewModel<TimeSettingViewMode>()
        TimeSettingPageContent(viewModel)
    }

}

@Composable
private fun TimeSettingPageContent(
    viewModel: TimeSettingViewMode,
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
                    fontSize = 20.sp,
                    lineHeight = 20.sp
                )
            }
        }

    }

}


