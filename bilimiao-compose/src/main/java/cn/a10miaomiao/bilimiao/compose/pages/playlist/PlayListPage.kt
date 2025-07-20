package cn.a10miaomiao.bilimiao.compose.pages.playlist

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.defaultNavOptions
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageListener
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.pages.playlist.components.PlayListItemCard
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListItemInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.myMenu
import com.a10miaomiao.bilimiao.comm.store.PlayListStore
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.math.max

@Serializable
class PlayListPage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: PlayListPageViewModel = diViewModel()
        PlayListPageContent(viewModel)
    }
}

private class PlayListPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val pageNavigation by instance<PageNavigation>()

    private val playerDelegate by instance<BasePlayerDelegate>()

    fun toVideoInfoPage(item: PlayListItemInfo) {
        pageNavigation.navigateToVideoInfo(item.aid)
    }

    fun playVideo(item: PlayListItemInfo) {
        playerDelegate.openPlayer(
            item.toVideoPlayerSource()
        )
    }
}


@Composable
private fun PlayListPageContent(
    viewModel: PlayListPageViewModel
) {
    val playerStore: PlayerStore by rememberInstance()
    val playListStore: PlayListStore by rememberInstance()
    val windowStore: WindowStore by rememberInstance()
    val windowState by windowStore.stateFlow.collectAsState()
    val windowInsets = windowState.getContentInsets(localContainerView())
    val playListState by playListStore.stateFlow.collectAsState()
    val playerState by playerStore.stateFlow.collectAsState()

    val showClearTipsDialog = remember {
        mutableStateOf(false)
    }
    val enableEditMode = remember {
        mutableStateOf(false)
    }
    val selectedItemsMap = remember {
        mutableStateMapOf<String, Int>()
    }

    fun clearPlayList() {
        showClearTipsDialog.value = false
        playListStore.clearPlayList()
    }

    fun menuItemClick (view: View, menuItem: MenuItemPropInfo) {
        when(menuItem.key) {
            MenuKeys.clear -> {
                showClearTipsDialog.value = true
            }
            MenuKeys.edit -> {
                selectedItemsMap.clear()
                enableEditMode.value = true
            }
            MenuKeys.delete -> {
                val selectedKeys = selectedItemsMap.keys
                selectedKeys.remove(playerState.cid) // 移除当前播放的视频
                if (selectedKeys.isEmpty()) {
                    PopTip.show("未选中任何视频")
                } else {
                    playListStore.removeItems(selectedKeys)
                }
            }
            MenuKeys.complete -> {
                enableEditMode.value = false
            }
        }
    }

    val pageConfigId = PageConfig(
        title = "播放列表",
        menu = remember(enableEditMode.value) {
            myMenu {
                if (enableEditMode.value) {
                    myItem {
                        key = MenuKeys.complete
                        title = "完成编辑"
                        iconFileName = "ic_baseline_check_24"
                    }
                    myItem {
                        key = MenuKeys.delete
                        title = "移除选中"
                        iconFileName = "ic_baseline_delete_outline_24"
                    }
                } else {
                    myItem {
                        key = MenuKeys.edit
                        title = "编辑列表"
                        iconFileName = "ic_baseline_edit_note_24"
                    }
                }
                myItem {
                    key = MenuKeys.clear
                    title = "清空列表"
                    iconFileName = "ic_baseline_clear_all_24"
                }
            }
        }
    )
    PageListener(
        configId = pageConfigId,
        onMenuItemClick = ::menuItemClick,
    )
    BackHandler(
        enabled = enableEditMode.value,
        onBack = {
            enableEditMode.value = false
        }
    )

    if(playListState.loading) {
        Row (
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(top = windowInsets.topDp.dp)
                .fillMaxWidth(),
        ){
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 3.dp,
            )
            Text(
                "加载中",
                modifier = Modifier.padding(start = 5.dp),
                color = MaterialTheme.colorScheme.outline,
                fontSize = 14.sp,
            )
        }
    } else if (!playListState.isEmpty()) {
        val currentPosition = remember {
            playerStore.getPlayListCurrentPosition()
        }
        val lazyListState = rememberLazyListState(
            initialFirstVisibleItemIndex = max(0, currentPosition)
        )
        val view = LocalView.current
        val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
            playListStore.moveItem(from.index, to.index)
            view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK)
        }
        LazyColumn(
            state = lazyListState,
            contentPadding = windowInsets.toPaddingValues()
        ) {
            val playListItems = playListState.items
            val currentPlayAid = playerState.aid
            val currentPlayCid = playerState.cid
            items(playListItems.size, {
                playListItems[it].aid
            }) { index ->
                val item = playListItems[index]
                ReorderableItem(
                    reorderableLazyListState,
                    key = item.aid,
                    modifier = Modifier.padding(5.dp),
                ) { isDragging ->
                    PlayListItemCard(
                        modifier = Modifier.fillMaxWidth()
                            .longPressDraggableHandle(
                                onDragStarted = {
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                },
                                onDragStopped = {
                                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                                },
                            ),
                        index = index,
                        item = item,
                        onClick = {
                           if (enableEditMode.value) {
                               if (selectedItemsMap.contains(item.cid)) {
                                   selectedItemsMap.remove(item.cid)
                               } else {
                                   selectedItemsMap[item.cid] = index
                               }
                           } else {
                               viewModel.toVideoInfoPage(item)
                           }
                        },
                        action = {
                           if (currentPlayAid == item.aid) {
                               Column(
                                   modifier = Modifier
                                       .widthIn(min = 50.dp),
                                   verticalArrangement = Arrangement.Center,
                                   horizontalAlignment = Alignment.CenterHorizontally
                               ) {
                                   Text(
                                       color = MaterialTheme.colorScheme.primary,
                                       text = "播放中",
                                       fontSize = 12.sp
                                   )
                                   if (item.videoPages.size > 1) {
                                       val i = item.videoPages.indexOfFirst {
                                           it.cid == currentPlayCid
                                       }
                                       Text(
                                           color = MaterialTheme.colorScheme.primary,
                                           text = "P${i + 1}",
                                           fontSize = 12.sp
                                       )
                                   }
                               }
                           } else if (enableEditMode.value) {
                               Checkbox(
                                   checked = selectedItemsMap.contains(item.cid),
                                   onCheckedChange = {
                                       if (it) {
                                           selectedItemsMap[item.cid] = index
                                       } else {
                                           selectedItemsMap.remove(item.cid)
                                       }
                                   }
                               )
                           } else {
                               Button(
                                   onClick = {
                                       viewModel.playVideo(item)
                                   },
                                   shape = MaterialTheme.shapes.small,
                                   contentPadding = PaddingValues(
                                       vertical = 4.dp,
                                       horizontal = 12.dp,
                                   ),
                                   modifier = Modifier
                                       .sizeIn(
                                           minWidth = 40.dp,
                                           minHeight = 30.dp
                                       )
                                       .padding(0.dp)
                               ) {
                                   Text(
                                       text = "播放",
                                       fontSize = 12.sp
                                   )
                               }
                           }
                       }
                    )
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = windowInsets.topDp.dp,
                    bottom = windowInsets.bottomDp.dp,
                    start = windowInsets.leftDp.dp,
                    end = windowInsets.rightDp.dp,
                )
        ) {
            Text(
                text = "当前播放列表为空",
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.outline,
                fontSize = 20.sp,
            )
        }
    }

    if (showClearTipsDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showClearTipsDialog.value = false
            },
            title = {
                Text(text = "提示")
            },
            text = {
                Text(text = "确认清空播放列表(⊙ˍ⊙)？")
            },
            confirmButton = {
               TextButton(onClick = ::clearPlayList) {
                   Text(text = "确认")
               }
            },
            dismissButton = {
                TextButton(onClick = {
                    showClearTipsDialog.value = false
                }) {
                    Text(text = "取消")
                }
            }
        )
    }
}