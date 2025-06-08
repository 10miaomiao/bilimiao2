package cn.a10miaomiao.bilimiao.compose.pages.home.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bilimiao.compose.BilimiaoPageRoute
import cn.a10miaomiao.bilimiao.compose.R
import cn.a10miaomiao.bilimiao.compose.base.BottomSheetState
import cn.a10miaomiao.bilimiao.compose.common.constant.PageTabIds
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.emitter.EmitterAction
import cn.a10miaomiao.bilimiao.compose.common.flow.stateMap
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.localEmitter
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.pages.home.HomePageState
import cn.a10miaomiao.bilimiao.compose.pages.home.components.HomeTimeMachineRegionCard
import cn.a10miaomiao.bilimiao.compose.pages.home.components.HomeTimeMachineTimeCard
import cn.a10miaomiao.bilimiao.compose.pages.time.TimeRegionDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.time.TimeSettingPage
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.comm.store.RegionStore
import com.a10miaomiao.bilimiao.comm.store.TimeSettingStore
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.store.WindowStore
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

private class HomeTimeMachineContentViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val pageNavigation: PageNavigation by instance()
    private val bottomSheetState by instance<BottomSheetState>()

    val timeSettingStore: TimeSettingStore by instance()
    val regionStore: RegionStore by instance()
    val userStore: UserStore by instance()

    val regionList = regionStore.stateFlow.stateMap { it.regions }

    val timeText = timeSettingStore.stateFlow.stateMap {
        it.timeFrom.getValue("-") + " 至 " + it.timeTo.getValue("-")
    }

    val timeSeason = timeSettingStore.stateFlow.stateMap {
        val month = it.timeFrom.month
        if (month < 3) 0
        else if (month < 6) 1
        else if (month < 9) 2
        else if (month < 12) 3
        else 0
    }

    fun toRegionDetailPage(
        region: RegionInfo,
        initialIndex: Int,
    ) {
        val children = region.children ?: emptyList()
        pageNavigation.navigate(TimeRegionDetailPage(
            tid = region.tid,
            name = region.name,
            childIds = children.map { it.tid },
            childNames = children.map { it.name },
            initialIndex = initialIndex,
        ))
    }

    fun openTimeSetting() {
        bottomSheetState.open(TimeSettingPage())
    }

}

@Composable
internal fun HomeTimeMachineContent(
    pageState: HomePageState
) {
    val viewModel: HomeTimeMachineContentViewModel = diViewModel()
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val regionList by viewModel.regionList.collectAsState()
    val timeText by viewModel.timeText.collectAsState()
    val timeSeason by viewModel.timeSeason.collectAsState()

    val listState = rememberLazyStaggeredGridState()
    val emitter = localEmitter()
    LaunchedEffect(Unit) {
        emitter.collectAction<EmitterAction.DoubleClickTab> {
            if (it.tab == PageTabIds.HomeTimeMachine) {
                if (listState.firstVisibleItemIndex != 0) {
                    listState.animateScrollToItem(0)
                }
            }
        }
    }
    LazyVerticalStaggeredGrid(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        columns = StaggeredGridCells.Adaptive(300.dp),
        contentPadding = windowInsets.toPaddingValues(
            top = 0.dp
        )
    ) {
        item() {
            HomeTimeMachineTimeCard(
                timeText,
                timeSeason,
                onClick = viewModel::openTimeSetting
            )
        }
        item() {
            val adInfo = pageState.adInfo.value
            if (adInfo != null && adInfo.isShow) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(5.dp)
                    ) {
                        Text(
                            adInfo.title,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    TextButton(
                        onClick = pageState::openLinkUrl
                    ) {
                        Text(adInfo.link.text)
                    }
                }
            }
        }
        items(regionList, { it.tid }, ) { region ->
            HomeTimeMachineRegionCard(
                region,
                onClick = viewModel::toRegionDetailPage
            )
        }
    }

}
