package cn.a10miaomiao.bilimiao.compose.pages.video

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Badge
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.archive.v1.Arc
import bilibili.app.dynamic.v2.DynamicItem
import bilibili.app.view.v1.ViewGRPC
import bilibili.app.view.v1.ViewPage
import bilibili.app.view.v1.ViewReply
import bilibili.app.view.v1.ViewReq
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.foundation.pagerTabIndicatorOffset
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.layout.DataDrivenNavigator
import cn.a10miaomiao.bilimiao.compose.components.layout.DoubleColumnAutofitLayout
import cn.a10miaomiao.bilimiao.compose.components.layout.chain_scrollable.rememberChainScrollableLayoutState
import cn.a10miaomiao.bilimiao.compose.components.status.BiliFailBox
import cn.a10miaomiao.bilimiao.compose.components.status.BiliLoadingBox
import cn.a10miaomiao.bilimiao.compose.components.video.VideoItemBox
import cn.a10miaomiao.bilimiao.compose.pages.community.MainReplyViewModel
import cn.a10miaomiao.bilimiao.compose.pages.community.content.ReplyDetailContent
import cn.a10miaomiao.bilimiao.compose.pages.video.components.VideoAddFavoriteDialog
import cn.a10miaomiao.bilimiao.compose.pages.video.components.VideoCoinDialog
import cn.a10miaomiao.bilimiao.compose.pages.video.components.VideoCoverBox
import cn.a10miaomiao.bilimiao.compose.pages.video.components.VideoDownloadDialog
import cn.a10miaomiao.bilimiao.compose.pages.video.components.VideoReplyTitleBar
import cn.a10miaomiao.bilimiao.compose.pages.video.content.VideoDetailContent
import cn.a10miaomiao.bilimiao.compose.pages.video.content.VideoReplyContent
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.VideoPlayerSource
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListFrom
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.store.FilterStore
import com.a10miaomiao.bilimiao.comm.store.PlayListStore
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.utils.MiaoLogger
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.store.WindowStore.Insets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

@Serializable
class VideoDetailPage(
    val id: String,
) : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: VideoDetailViewModel = diViewModel(key = id) {
            VideoDetailViewModel(it, id)
        }
        val windowStore: WindowStore by rememberInstance()
        val windowState = windowStore.stateFlow.collectAsState().value
        val windowInsets = windowState.getContentInsets(localContainerView())

        val detailData = viewModel.detailData.collectAsState().value

        BackHandler(
            onBack = viewModel::onBackPressed
        )
        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            targetState = detailData == null,
            label = "VideoDetailPage",
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
                VideoDetailPageLoading(
                    loading = viewModel.loading.collectAsState().value,
                    fail = viewModel.fail.collectAsState().value,
                    innerPadding = windowInsets.toPaddingValues()
                )
            } else {
                val arcData = detailData.arc ?: detailData.activitySeason?.arc
                if (arcData != null) {
                    VideoDetailPageContent(
                        viewModel = viewModel,
                        windowInsets = windowInsets,
                        detailData = detailData,
                        arcData = arcData,
                    )
                } else {
                    Text("arc为空")
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun VideoDetailPageContent(
    viewModel: VideoDetailViewModel,
    windowInsets: Insets,
    detailData: ViewReply,
    arcData: Arc,
) {
    val playerStore by rememberInstance<PlayerStore>()
    val playerState by playerStore.stateFlow.collectAsState()

    val mainReplyViewModel = diViewModel(
        key = "main-reply-${arcData.aid}"
    ) {
        MainReplyViewModel(
            it,
            type = 1,
            oid = arcData.aid.toString(),
        )
    }

    val videoPages = detailData.pages

    val isShowCover = playerState.aid != arcData.aid.toString()
    val headerCoverHeight = 200.dp
    val headerHeight = remember(isShowCover) {
        if (isShowCover) {
            windowInsets.topDp.dp + headerCoverHeight
        } else {
            windowInsets.topDp.dp
        }
    }

    val scope = rememberCoroutineScope()
    val chainScrollableLayoutState = rememberChainScrollableLayoutState(
        maxScrollPosition = headerHeight,
        minScrollPosition = windowInsets.topDp.dp,
    )
//    val saveableStateHolder = rememberSaveableStateHolder()

    val replyListState = rememberLazyListState()
    val currentReply by mainReplyViewModel.currentReply.collectAsState()
    BackHandler(
        enabled = currentReply != null
    ) {
        mainReplyViewModel.clearCurrentReply()
    }
    DoubleColumnAutofitLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        innerPadding = windowInsets.toPaddingValues(),
        chainScrollableLayoutState = chainScrollableLayoutState,
        leftMaxWidth = 600.dp,
        leftMaxHeight = headerHeight,
        leftContent = { orientation, innerPadding ->
            if (orientation == Orientation.Vertical) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Spacer(
                        Modifier.height(windowInsets.topDp.dp)
                    )
                    val videoHistory = detailData.history
                    AnimatedVisibility(isShowCover) {
                        VideoCoverBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(headerCoverHeight)
                                .padding(8.dp),
                            aid = arcData.aid,
                            title = arcData.title,
                            pic = arcData.pic,
                            duration = arcData.duration,
                            progress = videoHistory?.progress ?: 0L,
                            progressTitle = videoHistory?.cid?.let { cid ->
                                videoPages
                                    .mapNotNull { it.page }
                                    .find { it.cid == cid }
                                    ?.part
                            } ?: "",
                            onClick = viewModel::playVideo,
                            onLongClick = viewModel::openCoverActivity
                        )
                    }
                }
            } else {
                VideoDetailContent(
                    viewModel = viewModel,
                    innerPadding = innerPadding,
                    showCover = isShowCover,
                    detailData = detailData,
                    arcData = arcData,
                    isActive = true,
                )
            }
        }
    ) { orientation, innerPadding ->
        val tabs = remember(orientation) {
            if (orientation == Orientation.Vertical) {
                listOf(
                    "detail" to "详情",
                    "reply" to "评论"
                )
            } else {
                listOf(
                    "reply" to "评论"
                )
            }
        }
        val pagerState = rememberPagerState(pageCount = { tabs.size })
        DataDrivenNavigator(
            modifier = Modifier.fillMaxSize(),
            data = currentReply,
            dataKey = { it.id },
            dataContent = { data ->
                ReplyDetailContent(
                    reply = data,
                    innerPadding = innerPadding,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onCloseClick = {
                        mainReplyViewModel.clearCurrentReply()
                    },
                    onLikeReply = mainReplyViewModel::likeReply,
                    onDeletedReply = mainReplyViewModel::removeReplyItem,
                    usePageConfig = orientation == Orientation.Vertical
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                val replyCount = arcData.stat?.reply
                if (tabs.size > 1) {
                    TabRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = innerPadding.calculateTopPadding(),
                                start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                                end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                            )
                            .background(MaterialTheme.colorScheme.surface)
                            .nestedScroll(chainScrollableLayoutState.nestedScroll)
                            .scrollable(rememberScrollState(), Orientation.Vertical),
                        selectedTabIndex = pagerState.currentPage,
                        indicator = { positions ->
                            TabRowDefaults.PrimaryIndicator(
                                Modifier.pagerTabIndicatorOffset(pagerState, positions),
                            )
                        },
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            val selected = tabs[pagerState.currentPage].first == tab.first
                            Tab(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                    ) {
                                        Text(
                                            tab.second,
                                            color = if (selected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onBackground
                                            }
                                        )
                                        if (tab.first == "reply" && replyCount != null) {
                                            Text(
                                                text = "($replyCount)",
                                                fontSize = 10.sp,
                                                color = if (selected) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.onBackground
                                                }
                                            )
                                        }
                                    }
                                },
                                selected = selected,
                                onClick = {
                                    scope.launch {
                                        pagerState.scrollToPage(index)
                                    }
                                },
                            )
                        }
                    }
                } else {
                    VideoReplyTitleBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = innerPadding.calculateTopPadding(),
                                start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                                end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                            )
                            .background(MaterialTheme.colorScheme.surface),
                        viewModel = mainReplyViewModel,
                        count = replyCount ?: -1,
                    )
                }
                BackHandler(
                    enabled = pagerState.currentPage > 0
                ) {
                    scope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                }
                HorizontalPager(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    state = pagerState,
                    key = { index -> tabs[index].first },
                ) { index ->
                    val tab = tabs[index]
                    when (tab.first) {
                        "detail" -> {
                            VideoDetailContent(
                                viewModel = viewModel,
                                innerPadding = PaddingValues(
                                    bottom = innerPadding.calculateBottomPadding(),
                                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                                ),
                                showCover = false,
                                detailData = detailData,
                                arcData = arcData,
                                isActive = index == pagerState.currentPage,
                            )
                        }

                        "reply" -> {
                            VideoReplyContent(
                                viewModel = mainReplyViewModel,
                                listState = replyListState,
                                innerPadding = PaddingValues(
                                    bottom = innerPadding.calculateBottomPadding(),
                                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                                ),
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                                detailData = detailData,
                                arcData = arcData,
                                isActive = index == pagerState.currentPage,
                                usePageConfig = orientation == Orientation.Vertical,
                            )
                        }
                    }
                }
            }
        }
    }

    VideoCoinDialog(state = viewModel.coinDialogState)
    VideoAddFavoriteDialog(viewModel.addFavoriteDialogState)
    VideoDownloadDialog(state = viewModel.downloadDialogState)
}

@Composable
private fun VideoDetailPageLoading(
    loading: Boolean,
    fail: Any?,
    innerPadding: PaddingValues,
) {
    PageConfig(
        title = "视频详情"
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