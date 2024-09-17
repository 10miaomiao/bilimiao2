package cn.a10miaomiao.bilimiao.compose.pages.user


import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.base.stringPageArg
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.comm.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.commponents.layout.chain_scrollable.ChainScrollableLayout
import cn.a10miaomiao.bilimiao.compose.commponents.layout.chain_scrollable.rememberChainScrollableLayoutState
import cn.a10miaomiao.bilimiao.compose.pages.user.commponents.UserSpaceHeader
import cn.a10miaomiao.bilimiao.compose.pages.user.content.UserSpaceIndexContent
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.launch
import org.kodein.di.bindSingleton
import org.kodein.di.compose.rememberInstance
import org.kodein.di.compose.subDI
import kotlin.math.roundToInt

class UserSpacePage : ComposePage() {

    val id = stringPageArg("id")

    override val route: String
        get() = "user/space/${id}"

    @Composable
    override fun AnimatedContentScope.Content(navEntry: NavBackStackEntry) {
        val viewModel = diViewModel<UserSpaceViewModel>()
        val uid = navEntry.arguments?.get(id) ?: ""
        LaunchedEffect(uid) {
            viewModel.id = uid
        }
        subDI(
            diBuilder = {
                bindSingleton { viewModel }
            }
        ) {
            UserSpacePageContent()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserSpacePageContent() {
    PageConfig(
        title = "用户详情"
    )
    val viewModel: UserSpaceViewModel by rememberInstance()
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val detailData = viewModel.detailData.collectAsState().value

    if (detailData == null) {
        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column {
                Text("加载中")
            }
        }
        return
    }

    val maxHeaderSize = remember { mutableStateOf(0 to 0) }
    val density = LocalDensity.current
    val chainScrollableLayoutState = rememberChainScrollableLayoutState(
        density.run { maxHeaderSize.value.second.toDp() },
        windowInsets.topDp.dp,
    )
    val isLargeScreen = remember(maxHeaderSize.value.first) {
        density.run { maxHeaderSize.value.first.toDp() } > 600.dp
    }
    val scrollableState = rememberScrollState()


    val saveableStateHolder = rememberSaveableStateHolder()
    val scope = rememberCoroutineScope()

    ChainScrollableLayout(
        modifier = Modifier.fillMaxSize(),
        state = chainScrollableLayoutState,
    ) { state ->
        val alpha = (state.maxPx + state.getOffsetYValue()) / state.maxPx
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset {
                    IntOffset(
                        0,
                        state
                            .getOffsetYValue()
                            .roundToInt()
                    )
                }
                .background(MaterialTheme.colorScheme.background)
                .nestedScroll(state.nestedScroll)
                .scrollable(scrollableState, Orientation.Vertical),
        ) {
            UserSpaceHeader(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .fillMaxWidth()
                    .alpha(alpha)
                    .onGloballyPositioned { coordinates ->
                        val headerHeight = coordinates.size.height
                        val headerWidth = coordinates.size.width
                        if (maxHeaderSize.value.first != headerWidth ||
                            maxHeaderSize.value.second != headerHeight
                        ) {
                            maxHeaderSize.value = headerWidth to headerHeight
                        }
                    },
                isLargeScreen = isLargeScreen,
                viewModel = viewModel,
            )
        }
        Column(
            modifier = Modifier
                .offset {
                    IntOffset(
                        0,
                        (state.maxPx + state.getOffsetYValue()).roundToInt()
                    )
                },
        ) {
            PrimaryTabRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(
                        start = windowInsets.leftDp.dp,
                        end = windowInsets.rightDp.dp,
                    )
                    .nestedScroll(state.nestedScroll)
                    .scrollable(scrollableState, Orientation.Vertical),
                selectedTabIndex = viewModel.pagerState.currentPage,
            ) {
                viewModel.tabs.forEachIndexed { index, name ->
                    Tab(
                        text = {
                            Text(
                                text = name,
                                color = if (index == viewModel.currentPage) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onBackground
                                }
                            )
                        },
                        selected = viewModel.currentPage == index,
                        onClick = {
                            scope.launch {
                               viewModel.changeTab(index, true)
                            }
                        },
                    )
                }
            }
            HorizontalPager(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(bottom = state.minScrollPosition),
                state = viewModel.pagerState,
            ) { index ->
                saveableStateHolder.SaveableStateProvider(index) {
                    when (index) {
                        0 -> UserSpaceIndexContent()
                        else -> Box {}
                    }
                }
            }
        }
    }
}