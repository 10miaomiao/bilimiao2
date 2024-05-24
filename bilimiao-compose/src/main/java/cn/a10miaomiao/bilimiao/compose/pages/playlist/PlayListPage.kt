package cn.a10miaomiao.bilimiao.compose.pages.playlist

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.comm.defaultNavOptions
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.pages.playlist.commponents.PlayListItemCard
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListItemInfo
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.store.WindowStore
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

class PlayListPage : ComposePage() {
    override val route: String
        get() = "playlist"

    @Composable
    override fun AnimatedContentScope.Content(navEntry: NavBackStackEntry) {
        val viewModel: PlayListPageViewModel = diViewModel()
        PlayListPageContent(viewModel)
    }
}

private class PlayListPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()

    private val playerDelegate by instance<BasePlayerDelegate>()

    fun toVideoInfoPage(item: PlayListItemInfo) {
        val nav = fragment.findNavController()
        val id = item.aid
        nav.navigate(
            "bilimiao://video/$id".toUri(),
            defaultNavOptions
        )
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
    PageConfig(
        title = "播放列表"
    )
    val playerStore: PlayerStore by rememberInstance()
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())
    val playerState = playerStore.stateFlow.collectAsState().value
    val playListInfo = playerState.playList

    if (playListInfo != null) {
        val currentPosition = remember {
            playerState.getPlayListCurrentPosition()
        }
        val lazyListState = rememberLazyListState(
            initialFirstVisibleItemIndex = currentPosition
        )
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.padding(
                start = windowInsets.leftDp.dp,
                end = windowInsets.rightDp.dp,
            )
        ) {
            item {
                Spacer(modifier = Modifier.height(windowInsets.topDp.dp))
            }
            val playListItems = playListInfo.items
            items(playListItems.size, { playListItems[it].cid }) { index ->
                val item = playListItems[index]
                Box(
                    modifier = Modifier.padding(5.dp),
                ) {
                    PlayListItemCard(
                        index = index,
                        item = item,
                        currentPlayCid = playerState.cid,
                        onPlayClick = {
                            viewModel.playVideo(item)
                        },
                        onClick = {
                            viewModel.toVideoInfoPage(item)
                        }
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(windowInsets.bottomDp.dp))
            }
        }
    } else {

    }
}