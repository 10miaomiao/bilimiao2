package cn.a10miaomiao.bilimiao.compose.pages.user

import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.foundation.pagerTabIndicatorOffset
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageListener
import cn.a10miaomiao.bilimiao.compose.common.mypage.rememberMyMenu
import cn.a10miaomiao.bilimiao.compose.components.layout.AutoTwoPaneLayout
import cn.a10miaomiao.bilimiao.compose.pages.user.components.FavouriteEditForm
import cn.a10miaomiao.bilimiao.compose.pages.user.components.FavouriteEditFormState
import cn.a10miaomiao.bilimiao.compose.pages.user.content.UserFavouriteDetailContent
import cn.a10miaomiao.bilimiao.compose.pages.user.content.UserFavouriteListContent
import cn.a10miaomiao.bilimiao.compose.pages.user.content.UserSeasonDetailContent
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.myMenu
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.compose.rememberInstance

@Serializable
data class UserFavouritePage(
    private val mid: String,
) : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: UserFavouriteViewModel = diViewModel {
            UserFavouriteViewModel(it, mid)
        }
        UserFavouritePageContent(viewModel)
    }
}

@Composable
private fun UserFavouritePageContent(
    viewModel: UserFavouriteViewModel,
) {
    val userStore: UserStore by rememberInstance()
    val windowStore: WindowStore by rememberInstance()
    val playerStore: PlayerStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())
    val saveableStateHolder = rememberSaveableStateHolder()
    val listData = viewModel.createdList.data.collectAsState().value
    val openMediaDetail = viewModel.openedMedia.collectAsState().value

    var showAddDialog by remember {
        mutableStateOf(false)
    }
    var hideFirstPane by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val callName = remember(viewModel.mid) {
        if (userStore.isSelf(viewModel.mid)) {
            "我"
        } else {
            "Ta"
        }
    }

    fun menuItemClick(view: View, item: MenuItemPropInfo) {
        if (item.key == MenuKeys.add) {
            showAddDialog = true
        }
    }

    val pageConfigId = PageConfig(
        title = "${callName}的收藏",
        menu = rememberMyMenu {
            myItem {
                key = MenuKeys.more
                iconFileName = "ic_more_vert_grey_24dp"
                title = "更多"
                childMenu = myMenu {
                    myItem {
                        key = MenuKeys.add
                        title = "新建收藏夹"
                        iconFileName = "ic_add_white_24dp"
                    }
                }
            }
        },
    )
    PageListener(
        pageConfigId,
        onMenuItemClick = ::menuItemClick,
    )

    BackHandler(
        enabled = openMediaDetail != null,
        onBack = viewModel::closeMediaDetail
    )
    BackHandler(
        enabled = hideFirstPane,
        onBack = {
            hideFirstPane = false
        }
    )

    AutoTwoPaneLayout(
        modifier = Modifier.padding(
            start = windowInsets.leftDp.dp,
            end = windowInsets.rightDp.dp
        ),
        first = {
            Column {
                TabRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(
                            top = windowInsets.topDp.dp,
                            start = windowInsets.leftDp.dp,
                            end = if (it.showTowPane) 0.dp else windowInsets.rightDp.dp
                        ),
                    selectedTabIndex = pagerState.currentPage,
                    indicator = { positions ->
                        TabRowDefaults.PrimaryIndicator(
                            Modifier.pagerTabIndicatorOffset(pagerState, positions),
                        )
                    },
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
                    when (index) {
                        0 -> {
                            saveableStateHolder.SaveableStateProvider(
                                UserFavouriteFolderType.Created
                            ) {
                                // 创建的
                                UserFavouriteListContent(
                                    viewModel = viewModel,
                                    showTowPane = it.showTowPane,
                                    folderType = UserFavouriteFolderType.Created,
                                )
                            }
                        }

                        1 -> {
                            saveableStateHolder.SaveableStateProvider(
                                UserFavouriteFolderType.Collected
                            ) {
                                // 订阅的
                                UserFavouriteListContent(
                                    viewModel = viewModel,
                                    showTowPane = it.showTowPane,
                                    folderType = UserFavouriteFolderType.Collected,
                                )
                            }
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
                            media.title,
                            showTowPane = it.showTowPane,
                            hideFirstPane = hideFirstPane,
                            onChangeHideFirstPane = { hidden ->
                                hideFirstPane = hidden
                            },
                        )
                    } else {
                        // 收藏详情
                        UserFavouriteDetailContent(
                            media.id,
                            media.title,
                            showTowPane = it.showTowPane,
                            hideFirstPane = hideFirstPane,
                            onChangeHideFirstPane = { hidden ->
                                hideFirstPane = hidden
                            },
                            onRefresh = {
                                viewModel.refresh(UserFavouriteFolderType.Created)
                            },
                            onClose = {
                                viewModel.closeMediaDetail()
                            }
                        )
                    }
                }
            }
        },
        twoPaneMinWidth = 500.dp,
        openedSecond = openMediaDetail != null,
        firstPaneMaxWidth = 400.dp,
        hideFirstPane = hideFirstPane,
    )

    if (showAddDialog) {
        val formState = remember {
            FavouriteEditFormState(
                initialTitle = "",
                initialIntro = "",
                initialPrivacy = 0,
            )
        }
        var loading by remember {
            mutableStateOf(false)
        }

        fun handleSubmit() {
            scope.launch(Dispatchers.IO) {
                runCatching {
                    viewModel.addFolder(
                        cover = "",
                        title = formState.title,
                        intro = formState.intro,
                        privacy = formState.privacy,
                    )
                }.onSuccess {
                    PopTip.show("创建成功")
                    showAddDialog = false
                }.onFailure {
                    PopTip.show(it.message ?: it.toString())
                }
            }
        }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
            },
            title = {
                Text(
                    text = "新建收藏夹",
                    fontWeight = FontWeight.W700,
                    style = MaterialTheme.typography.titleSmall
                )
            },
            text = {
                FavouriteEditForm(formState)
            },
            confirmButton = {
                TextButton(
                    enabled = !loading,
                    onClick = ::handleSubmit,
                ) {
                    Text(text = "添加")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                    },
                ) {
                    Text(text = "取消")
                }
            }
        )
    }

}