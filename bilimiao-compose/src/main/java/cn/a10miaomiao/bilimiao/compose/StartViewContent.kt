package cn.a10miaomiao.bilimiao.compose

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.runtime.DisposableEffect
import androidx.activity.compose.BackHandler
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.PathParser as ComposePathParser
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.addPaddingValues
import cn.a10miaomiao.bilimiao.compose.common.foundation.ScaleIndication
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoCard
import cn.a10miaomiao.bilimiao.compose.components.start.StartLibraryCard
import cn.a10miaomiao.bilimiao.compose.components.start.StartPlayerCard
import cn.a10miaomiao.bilimiao.compose.components.start.StartSearchCard
import cn.a10miaomiao.bilimiao.compose.components.start.StartUserCard
import cn.a10miaomiao.bilimiao.compose.pages.auth.LoginPage
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadListPage
import cn.a10miaomiao.bilimiao.compose.pages.lyric.LyricPage
import cn.a10miaomiao.bilimiao.compose.pages.message.MessagePage
import cn.a10miaomiao.bilimiao.compose.pages.mine.HistoryPage
import cn.a10miaomiao.bilimiao.compose.pages.mine.MyBangumiPage
import cn.a10miaomiao.bilimiao.compose.pages.mine.MyFollowPage
import cn.a10miaomiao.bilimiao.compose.pages.mine.WatchLaterPage
import cn.a10miaomiao.bilimiao.compose.pages.playlist.PlayListPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.SettingPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserBangumiPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFavouritePage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSpacePage
import cn.a10miaomiao.bilimiao.compose.pages.video.VideoDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.web.WebPage
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.entity.user.UserInfo
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.store.WindowStore
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import android.app.Activity
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeGestures
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.GenericShape
import androidx.core.view.WindowCompat
import cn.a10miaomiao.bilimiao.compose.base.PageSearchMethod
import cn.a10miaomiao.bilimiao.compose.common.foundation.add
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoOutlinedCard
import cn.a10miaomiao.bilimiao.compose.components.start.SearchInputInline
import cn.a10miaomiao.bilimiao.compose.pages.search.SearchInputViewModel
import cn.a10miaomiao.bilimiao.compose.pages.search.SearchInputViewModel.SuggestInfo
import cn.a10miaomiao.bilimiao.compose.pages.search.SearchInputViewModel.SuggestType
import cn.a10miaomiao.bilimiao.compose.pages.search.SearchResultPage

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun StartViewContent(
    modifier: Modifier = Modifier,
    startTopHeight: Dp = 200.dp,
    navigateTo: (ComposePage) -> Unit,
    navigateUrl: (String) -> Unit,
    openSearch: () -> Unit,
    openScanner: (callback: (String) -> Unit) -> Boolean,
    isSearchVisible: Boolean = false,
    searchInitKeyword: String = "",
    searchInitMode: Int = 0,
    pageSearchMethod: PageSearchMethod? = null,
    searchAnimation: Boolean = true,
    onCloseSearch: () -> Unit = {},
) {
    val listState = rememberLazyListState()

    SharedTransitionLayout(
        modifier = modifier
    ) {
        AnimatedContent(
            isSearchVisible,
            transitionSpec = {
                // 从下往上进入，从上往下退出，并带有透明过渡
                if (searchAnimation) {
                    (
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> -fullWidth },
                            animationSpec = tween(durationMillis = 200)
                        ) + fadeIn(animationSpec = tween(durationMillis = 200))
                        ) togetherWith (
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth },
                            animationSpec = tween(durationMillis = 250)
                        ) + fadeOut(animationSpec = tween(durationMillis = 250))
                    )
                } else {
                    // 无动画
                    fadeIn(animationSpec = tween(0)) togetherWith
                            fadeOut(animationSpec = tween(0))
                }
            },
            label = "basic_transition"
        ) { targetState ->
            if (targetState) {
                SearchInputInline(
                    modifier = Modifier,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedContent,
                    initKeyword = searchInitKeyword,
                    initMode = searchInitMode,
                    pageSearchMethod = pageSearchMethod,
                    onDismissRequest = onCloseSearch,
                )
            } else {
                StartIndexList(
                    modifier = modifier,
                    listState = listState,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedContent,
                    startTopHeight = startTopHeight,
                    openSearch = openSearch,
                    openScanner = openScanner,
                    navigateTo = navigateTo,
                    navigateUrl = navigateUrl,
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun StartIndexList(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    startTopHeight: Dp = 200.dp,
    openSearch: () -> Unit,
    openScanner: (onResult: (String) -> Unit) -> Boolean,
    navigateTo: (ComposePage) -> Unit,
    navigateUrl: (String) -> Unit,
) {
    val userStore by rememberInstance<UserStore>()
    val userState by userStore.stateFlow.collectAsState()
    val playerStore by rememberInstance<PlayerStore>()
    val playerState by playerStore.stateFlow.collectAsState()
    val playerDelegate by rememberInstance<BasePlayerDelegate>()

    var showScannerDownloadDialog by remember { mutableStateOf(false) }
    var showScannerResultTips by remember { mutableStateOf("") }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = WindowInsets
            .safeDrawing
            .asPaddingValues()
            .add(PaddingValues(10.dp)),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .padding(top = startTopHeight)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .size(80.dp, 8.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            color = Color.White.copy(alpha = 0.8f)
                        ),
                )
            }
        }
        item {
            AnimatedVisibility(
                visible = playerState.title.isNotEmpty(),
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .fillMaxWidth()
            ) {
                StartPlayerCard(
                    aid = playerState.aid,
                    title = playerState.title,
                    cover = playerState.cover,
                    onClick = {
                        if (playerState.type === "video") {
                            navigateTo(
                                VideoDetailPage(
                                    id = playerState.aid,
                                )
                            )
                        } else if (playerState.type === "bangumi") {
                            navigateTo(
                                BangumiDetailPage(
                                    id = playerState.sid,
                                    epId = playerState.epid,
                                )
                            )
                        }
                    },
                    onLyricClick = {
                        navigateTo(LyricPage())
                    },
                    onPlayListClick = {
                        navigateTo(PlayListPage())
                    },
                    onCloseClick = playerDelegate::closePlayer,
                )
            }
        }

        item {
            StartUserCard(
                userInfo = userState.info,
                onUserClick = {
                    val userInfo = userState.info
                    if (userInfo != null) {
                        navigateTo(UserSpacePage(
                            id = userInfo.mid.toString(),
                        ))
                    } else {
                        navigateTo(LoginPage())
                    }
                },
                onUserDynamicClick = {
                    val userInfo = userState.info
                    if (userInfo != null) {
                        navigateTo(UserSpacePage(
                            id = userInfo.mid.toString(),
                        ))
                    }
                },
                onUserFollowerClick = {
                    navigateTo(WebPage(
                        url = "https://space.bilibili.com/h5/follow?type=fans",
                    ))
                },
                onUserFollowingClick = {
                    navigateTo(MyFollowPage())
                },
                onMessageClick = {
                    navigateTo(MessagePage())
                }
            )
        }
        item {
            StartSearchCard(
                modifier = Modifier
                    .run {
                        with(sharedTransitionScope) {
                            sharedElement(
                                rememberSharedContentState(key = "search"),
                                animatedVisibilityScope = animatedVisibilityScope,
                            )
                        }
                    },
                onClick = {
                    openSearch()
                },
                onScannerClick = {
                    openScanner { result ->
                        if (result.startsWith("https://")
                            || result.startsWith("http://")
                            || result.startsWith("bilimiao:")
                            || result.startsWith("bilibili://")) {
                            navigateUrl(result)
                        } else {
                            showScannerResultTips = result
                        }
                    }.let {
                        if (!it) {
                            // 打开失败
                            showScannerDownloadDialog = true
                        }
                    }
                },
            )
        }
        item {
            StartLibraryCard(
                userId = userState.info?.mid,
                navigateTo = navigateTo,
            )
        }
        item {
            StartFooterCard(
                onDownloadClick = {
                    navigateTo(DownloadListPage())
                },
                onSettingClick = {
                    navigateTo(SettingPage())
                },
            )
        }
    }

    if (showScannerDownloadDialog) {
        AlertDialog(
            onDismissRequest = { showScannerDownloadDialog = false },
            title = { Text(text = "未安装扫码插件") },
            text = {
                Text(text = "请前往Github/Gitee下载并安装bilimiao扫码器插件")
            },
            confirmButton = {
                Row() {
                    TextButton(
                        onClick = {
                            navigateUrl("https://github.com/10miaomiao/bilimiao_scanner/releases")
                            showScannerDownloadDialog = false
                        },
                    ) {
                        Text("前往GitHub下载")
                    }
                    TextButton(
                        onClick = {
                            navigateUrl("https://gitee.com/10miaomiao/bilimiao_scanner/releases")
                            showScannerDownloadDialog = false
                        },
                    ) {
                        Text("前往Gitee下载")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showScannerDownloadDialog = false
                    },
                ) {
                    Text("取消")
                }
            }
        )
    }
    if (showScannerResultTips.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showScannerResultTips = "" },
            title = { Text(text = "扫码结果") },
            text = {
                Text(text = showScannerResultTips)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        navigateTo(
                            SearchResultPage(
                                keyword = showScannerResultTips,
                            )
                        )
                        showScannerResultTips = ""
                    },
                ) {
                    Text("前往搜索")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showScannerResultTips = ""
                    },
                ) {
                    Text("取消")
                }
            }
        )
    }

}


@Composable
private fun StartFooterCard(
    onDownloadClick: () -> Unit,
    onSettingClick: () -> Unit
) {
    MiaoOutlinedCard {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onDownloadClick)
                    .padding(vertical = 10.dp),
                text = "视频下载",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            VerticalDivider(
                modifier = Modifier
                    .height(20.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
            Text(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onSettingClick)
                    .padding(vertical = 10.dp),
                text = "设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}
