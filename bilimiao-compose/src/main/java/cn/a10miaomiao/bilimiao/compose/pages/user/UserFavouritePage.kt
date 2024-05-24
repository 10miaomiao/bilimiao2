package cn.a10miaomiao.bilimiao.compose.pages.user

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.window.layout.DisplayFeature
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.base.stringPageArg
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.commponents.layout.AutoTwoPaneLayout
import cn.a10miaomiao.bilimiao.compose.pages.user.commponents.FavouriteEditDialog
import cn.a10miaomiao.bilimiao.compose.pages.user.content.UserFavouriteDetailContent
import cn.a10miaomiao.bilimiao.compose.pages.user.content.UserFavouriteListContent
import cn.a10miaomiao.bilimiao.compose.pages.user.content.UserSeasonDetailContent
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.google.accompanist.adaptive.HorizontalTwoPaneStrategy
import com.google.accompanist.adaptive.SplitResult
import com.google.accompanist.adaptive.TwoPane
import com.google.accompanist.adaptive.TwoPaneStrategy
import com.google.accompanist.adaptive.calculateDisplayFeatures
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import org.kodein.di.compose.rememberInstance
import org.kodein.di.compose.subDI
import org.kodein.di.instance

class UserFavouritePage : ComposePage() {

    val id = stringPageArg("mid")

    override val route: String
        get() = "user/${id}/favourite"

    @Composable
    override fun AnimatedContentScope.Content(navEntry: NavBackStackEntry) {
        val viewModel: UserFavouriteViewModel = diViewModel()
        val mid = navEntry.arguments?.get(id) ?: ""
        LaunchedEffect(mid) {
            viewModel.mid = mid
        }
        subDI(
            diBuilder = {
                bindSingleton { viewModel }
            }
        ) {
            UserFavouritePageContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UserFavouritePageContent() {
    val viewModel: UserFavouriteViewModel by rememberInstance()
    val userStore: UserStore by rememberInstance()
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())
    val saveableStateHolder = rememberSaveableStateHolder()
    val listData = viewModel.createdList.data.collectAsState().value
    val openMediaDetail = viewModel.openedMedia.collectAsState().value

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val callName = remember(viewModel.mid) {
        if (userStore.isSelf(viewModel.mid)) {
            "我"
        } else {
            "Ta"
        }
    }

    PageConfig(
        title = "${callName}的收藏"
    )

    BackHandler(
        enabled = openMediaDetail != null,
        onBack = viewModel::closeMediaDetail
    )

    AutoTwoPaneLayout(
        modifier = Modifier.padding(
            start = windowInsets.leftDp.dp,
            end = windowInsets.rightDp.dp
        ),
        first = {
            Column {
                PrimaryTabRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(
                            top = windowInsets.topDp.dp,
                        ),
                    selectedTabIndex = pagerState.currentPage,
                ) {
                    Tab(
                        text = {
                            Text(
                                text = "${callName}创建的",
                                color = if (0 == pagerState.currentPage) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onBackground
                                }
                            )
                        },
                        selected = pagerState.currentPage == 0,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        },
                    )

                    Tab(
                        text = {
                            Text(
                                text = "${callName}订阅的",
                                color = if (1 == pagerState.currentPage) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onBackground
                                }
                            )
                        },
                        selected = pagerState.currentPage == 1,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        },
                    )
                }
                HorizontalPager(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    state = pagerState,
                ) { index ->
                    when(index) {
                        0 -> {
                            // 创建的
                            UserFavouriteListContent(
                                showTowPane = it.showTowPane,
                                folderType = UserFavouriteFolderType.Created,
                            )
                        }
                        1 -> {
                            // 订阅的
                            UserFavouriteListContent(
                                showTowPane = it.showTowPane,
                                folderType = UserFavouriteFolderType.Collected,
                            )
                        }
                    }
                }
            }
        },
        second = {
            val media = openMediaDetail ?: if (it.visible) {
                listData.firstOrNull()
            } else null
            if (media != null) {
                saveableStateHolder.SaveableStateProvider(media.id) {
                    if (media.type == 21) {
                        // 合集详情
                        UserSeasonDetailContent(
                            media.id,
                            media.title
                        )
                    } else {
                        // 收藏详情
                        UserFavouriteDetailContent(
                            media.id,
                            media.title
                        )
                    }
                }
            }
        },
        twoPaneMinWidth = 500.dp,
        openedSecond = openMediaDetail != null,
        firstPaneMaxWidth = 400.dp
    )

    FavouriteEditDialog()

}