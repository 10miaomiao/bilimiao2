package cn.a10miaomiao.bilimiao.compose.pages.playlist

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.a10miaomiao.bilimiao.comm.store.PlayListStore
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.store.WindowStore
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance
import kotlin.math.max

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
    val playListStore: PlayListStore by rememberInstance()
    val windowStore: WindowStore by rememberInstance()
    val windowState by windowStore.stateFlow.collectAsState()
    val windowInsets = windowState.getContentInsets(localContainerView())
    val playListState by playListStore.stateFlow.collectAsState()
    val playerState by playerStore.stateFlow.collectAsState()

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
            val playListItems = playListState.items
            items(playListItems.size, {
                playListItems[it].aid + "-" + playListItems[it].cid
            }) { index ->
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