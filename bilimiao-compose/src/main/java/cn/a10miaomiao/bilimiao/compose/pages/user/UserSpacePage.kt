package cn.a10miaomiao.bilimiao.compose.pages.user


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.emitter.EmitterAction
import cn.a10miaomiao.bilimiao.compose.common.foundation.combinedTabDoubleClick
import cn.a10miaomiao.bilimiao.compose.common.foundation.pagerTabIndicatorOffset
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.localEmitter
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageListener
import cn.a10miaomiao.bilimiao.compose.common.mypage.rememberMyMenu
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.layout.chain_scrollable.ChainScrollableLayout
import cn.a10miaomiao.bilimiao.compose.components.layout.chain_scrollable.rememberChainScrollableLayoutState
import cn.a10miaomiao.bilimiao.compose.components.status.BiliFailBox
import cn.a10miaomiao.bilimiao.compose.components.status.BiliLoadingBox
import cn.a10miaomiao.bilimiao.compose.pages.user.components.UserSpaceHeader
import com.a10miaomiao.bilimiao.comm.entity.user.SpaceInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuActions
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.SearchConfigInfo
import com.a10miaomiao.bilimiao.comm.mypage.myMenu
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.store.WindowStore.Insets
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.compose.rememberInstance
import kotlin.math.roundToInt

@Serializable
data class UserSpacePage(
    val id: String,
) : ComposePage() {

    @Composable
    override fun Content() {
        val archiveViewModel = diViewModel(key = "archive$id") {
            UserArchiveViewModel(it, id)
        }
        val viewModel = diViewModel() {
            UserSpaceViewModel(it, id, archiveViewModel)
        }
//        AnimatedContent()
        UserSpacePageContent(viewModel, archiveViewModel)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun UserSpacePageContent(
    viewModel: UserSpaceViewModel,
    archiveViewModel: UserArchiveViewModel,
) {
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val detailData = viewModel.detailData.collectAsState().value
//    val slideDistance = LocalDensity.current.run {
//        100.dp.toPx()
//    }
    AnimatedContent(
        modifier = Modifier.fillMaxSize(),
        targetState = detailData == null,
        label = "UserSpacePageContent",
        transitionSpec = {
            // Follow M3 Clean fades
            val fadeIn = fadeIn(
                tween(),
            )
            val fadeOut = fadeOut()
            fadeIn.togetherWith(fadeOut)
        }
    ) {
        if (it || detailData == null) {
            UserSpacePageLoadingContent(
                loading = viewModel.loading.collectAsState().value,
                fail = viewModel.fail.collectAsState().value,
                innerPadding = windowInsets.toPaddingValues()
            )
        } else {
            UserSpacePageDetailContent(
                viewModel = viewModel,
                archiveViewModel = archiveViewModel,
                windowInsets = windowInsets,
                detailData = detailData,
            )
        }
    }
}

@Composable
private fun UserSpacePageLoadingContent(
    loading: Boolean,
    fail: Any?,
    innerPadding: PaddingValues,
) {
    PageConfig(
        title = "个人中心"
    )
    if (loading) {
        BiliLoadingBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    } else if (fail != null) {
        BiliFailBox(
            e = fail,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
private fun UserSpacePageDetailContent(
    viewModel: UserSpaceViewModel,
    archiveViewModel: UserArchiveViewModel,
    windowInsets: Insets,
    detailData: SpaceInfo,
) {
    val isFollow = viewModel.isFollow.collectAsState().value
    val rankOrder = archiveViewModel.rankOrder.collectAsState().value
    val pageConfigId = PageConfig(
        title = detailData.card?.name ?: "个人中心",
        menu = rememberMyMenu(isFollow, viewModel.isFiltered, viewModel.currentPage, rankOrder) {
            myItem {
                key = MenuKeys.more
                iconFileName = "ic_more_vert_grey_24dp"
                title = "更多"
                childMenu = myMenu {
                    if (!viewModel.isSelf) {
                        if (viewModel.isFiltered) {
                            myItem {
                                key = 1
                                title = "取消屏蔽该UP主"
                            }
                        } else {
                            myItem {
                                key = 2
                                title = "屏蔽该UP主"
                            }
                        }
                    }
                    myItem {
                        key = 3
                        title = "用浏览器打开"
                    }
                    myItem {
                        key = 4
                        title = "复制链接"
                    }
                    myItem {
                        key = 5
                        title = "分享"
                    }
                }
            }
            if (viewModel.currentPage == 2) {
                myItem {
                    key = MenuKeys.filter
                    title = when(rankOrder) {
                        "pubdate" -> "最新发布"
                        "click" -> "最多播放"
                        else -> "排序"
                    }
                    iconFileName = "ic_baseline_filter_list_grey_24"
                    childMenu = myMenu {
                        checkable = true
                        checkedKey = when(rankOrder) {
                            "pubdate" -> 11
                            "click" -> 12
                            else -> 11
                        }
                        myItem {
                            key = 11
                            action = "pubdate"
                            title = "最新发布"
                        }
                        myItem {
                            key = 12
                            action = "click"
                            title = "最多播放"
                        }
                    }
                }
            }
            myItem {
                key = MenuKeys.search
                title = "搜索"
                iconFileName = "ic_search_gray"
                action = MenuActions.search
            }
            if (!viewModel.isSelf) {
                myItem {
                    key = MenuKeys.follow
                    if (isFollow) {
                        iconFileName = "ic_baseline_favorite_24"
                        title = "已关注"
                    } else {
                        iconFileName = "ic_outline_favorite_border_24"
                        title = "关注"
                    }
                }
            }
        },
        search = SearchConfigInfo(
            name = "搜索投稿列表",
            keyword = "",
        )
    )
    PageListener(
        pageConfigId,
        onMenuItemClick = viewModel::menuItemClick,
        onSearchSelfPage = viewModel::searchSelfPage
    )

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

    val scope = rememberCoroutineScope()
    val emitter = localEmitter()

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
                archiveViewModel = archiveViewModel,
            )
        }
        val combinedTabClick = combinedTabDoubleClick(
            pagerState = viewModel.pagerState,
            onDoubleClick = {
                scope.launch {
                    emitter.emit(
                        EmitterAction.DoubleClickTab(
                            tab = viewModel.tabs[it].id
                        ))
                }
            }
        )
        Column(
            modifier = Modifier
                .offset {
                    IntOffset(
                        0,
                        (state.maxPx + state.getOffsetYValue()).roundToInt()
                    )
                },
        ) {
            TabRow(
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
                indicator = { positions ->
                    TabRowDefaults.PrimaryIndicator(
                        Modifier.pagerTabIndicatorOffset(viewModel.pagerState, positions),
                    )
                },
            ) {
                viewModel.tabs.forEachIndexed { index, tab ->
                    Tab(
                        text = {
                            Text(
                                text = tab.name,
                                color = if (index == viewModel.currentPage) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onBackground
                                }
                            )
                        },
                        selected = viewModel.currentPage == index,
                        onClick = { combinedTabClick(index) },
                    )
                }
            }
            val saveableStateHolder = rememberSaveableStateHolder()
            HorizontalPager(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(bottom = state.minScrollPosition),
                state = viewModel.pagerState,
            ) { index ->
                saveableStateHolder.SaveableStateProvider(index) {
                    viewModel.tabs[index].PageContent()
                }
            }
        }
    }
}