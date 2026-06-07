package cn.a10miaomiao.bilimiao.compose.pages.setting

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.foundation.pagerTabIndicatorOffset
import cn.a10miaomiao.bilimiao.compose.common.localContentInsets
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.pages.setting.content.DanmakuDisplaySettingContent
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.mapPreferences
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware

@Serializable
data class DanmakuDisplaySettingPage(
    val name: String = "default"
) : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: DanmakuDisplaySettingPageViewModel = diViewModel { DanmakuDisplaySettingPageViewModel(it) }
        val initialMode = name
        DanmakuDisplaySettingPageContent(
            viewModel,
            initialMode,
        )
    }
}

private class DanmakuDisplaySettingPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val modeList = listOf(
        Pair(
            SettingPreferences.DanmakuDefault,
            "默认",
        ),
        Pair(
            SettingPreferences.DanmakuSmallMode,
            "小屏",
        ),
        Pair(
            SettingPreferences.DanmakuFullMode,
            "全屏",
        ),
        Pair(
            SettingPreferences.DanmakuPipMode,
            "画中画",
        )
    )
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun DanmakuDisplaySettingPageContent(
    viewModel: DanmakuDisplaySettingPageViewModel,
    initialMode: String,
) {
    PageConfig(
        title = "弹幕显示设置"
    )
    val windowInsets = localContentInsets()

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        pageCount = {
            viewModel.modeList.size
        }
    )
    LaunchedEffect(initialMode) {
        val page = viewModel.modeList.indexOfFirst {
            it.first.name == initialMode
        }
        if (page != -1) {
            val mode = viewModel.modeList[page]
            val enable = SettingPreferences.mapPreferences {
                it[mode.first.enable] ?: false
            }
            if (enable) {
                pagerState.scrollToPage(page)
            }
        }
    }

    Column(
        modifier = Modifier.padding(
            start = windowInsets.leftDp.dp,
            end = windowInsets.rightDp.dp,
        )
    ) {
        TabRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(windowInsets.toPaddingValues(bottom = 0.dp)),
            selectedTabIndex = pagerState.currentPage,
            indicator = { positions ->
                TabRowDefaults.PrimaryIndicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, positions),
                )
            },
        ) {
            viewModel.modeList.forEachIndexed { index, item ->
                val selected = index == pagerState.currentPage
                Tab(
                    text = {
                        Text(
                            text = item.second,
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onBackground
                            }
                        )
                    },
                    selected = selected,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                )
            }
        }
        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = pagerState,
        ) { index ->
            val item = viewModel.modeList[index]
            DanmakuDisplaySettingContent(
                danmakuPreferences = item.first
            )
        }
    }
}
