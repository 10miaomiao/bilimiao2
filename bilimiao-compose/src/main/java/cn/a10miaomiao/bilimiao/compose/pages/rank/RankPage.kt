package cn.a10miaomiao.bilimiao.compose.pages.rank

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.foundation.pagerTabIndicatorOffset
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.pages.rank.content.RankListContent
import com.a10miaomiao.bilimiao.comm.store.RegionStore
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance


@Serializable
class RankPage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: RankPageViewModel = diViewModel()
        RankPageContent(viewModel)
    }
}

private class RankPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()
    private val regionStore: RegionStore by instance()

    val regionNames = mutableStateListOf<String>()
    val regionIds = mutableStateListOf<Int>()

    init {
        viewModelScope.launch {
            regionStore.stateFlow.collect {
                regionNames.clear()
                regionIds.clear()
                regionNames.add("全站")
                regionIds.add(0)
                it.regions.forEach { region ->
                    regionNames.add(region.name)
                    regionIds.add(region.tid)
                }
            }
        }
    }

}


@Composable
private fun RankPageContent(
    viewModel: RankPageViewModel
) {
    PageConfig(
        title = "排行榜"
    )

    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val pagerState = rememberPagerState { viewModel.regionIds.size }
    val currentPage = pagerState.currentPage
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            ScrollableTabRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(
                        top = windowInsets.topDp.dp,
                        start = windowInsets.leftDp.dp,
                        end = windowInsets.rightDp.dp,
                    ),
                edgePadding = 0.dp,
                selectedTabIndex = currentPage,
                indicator = { positions ->
                    if (currentPage > -1 && currentPage < positions.size) {
                        TabRowDefaults.PrimaryIndicator(
                            Modifier.pagerTabIndicatorOffset(pagerState, positions),
                        )
                    }
                },
                divider = {}
            ) {
                viewModel.regionIds.forEachIndexed { index, region ->
                    Tab(
                        text = {
                            Text(
                                text = viewModel.regionNames[index],
                                color = if (index == pagerState.currentPage) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onBackground
                                }
                            )
                        },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                    )
                }
            }
            HorizontalDivider(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
            )
        }

        val saveableStateHolder = rememberSaveableStateHolder()
        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = pagerState,
        ) { index ->
            val id = viewModel.regionIds[index]
            saveableStateHolder.SaveableStateProvider(id.toString()) {
                RankListContent(
                    regionId = id,
                )
            }
        }

    }
}