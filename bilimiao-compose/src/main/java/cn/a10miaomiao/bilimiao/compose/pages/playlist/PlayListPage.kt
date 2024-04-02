package cn.a10miaomiao.bilimiao.compose.pages.playlist

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.VideoPlayerSource
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListItemInfo
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.a10miaomiao.bilimiao.store.WindowStore
import com.skydoves.landscapist.glide.GlideImage
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
        LazyColumn(
//            columns = GridCells.Adaptive(400.dp),
            modifier = Modifier.padding(
                start = windowInsets.leftDp.dp,
                end = windowInsets.rightDp.dp,
            )
        ) {

            item(
//                span = {
//                    GridItemSpan(maxLineSpan)
//                }
            ) {
                Spacer(modifier = Modifier.height(windowInsets.topDp.dp))
            }
            val playListItems = playListInfo.items
            items(playListItems.size, { playListItems[it].cid }) { index ->
                val item = playListItems[index]
                Box(
                    modifier = Modifier.padding(5.dp),
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable {
                                    viewModel.toVideoInfoPage(item)
                                }
                                .padding(10.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(130.dp, 80.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                            ) {
                                GlideImage(
                                    imageModel = UrlUtil.autoHttps(item.cover) + "@672w_378h_1c_",
                                    modifier = Modifier.fillMaxSize(),
                                )
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(bottomEnd = 10.dp)
                                        )
                                        .padding(
                                            vertical = 2.dp,
                                            horizontal = 4.dp,
                                        ),
                                    ) {
                                    Text(
                                        color = Color.White,
                                        text = "${index + 1}",
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 10.dp),
                            ) {
                                Text(
                                    text = item.title,
                                    maxLines = 2,
                                    modifier = Modifier.padding(bottom = 5.dp),
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "UP:" + item.ownerName,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.outline,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Box(
                                modifier = Modifier.fillMaxHeight(),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (playerState.cid == item.cid) {
                                    Box(
                                        modifier = Modifier
                                            .sizeIn(
                                                minWidth = 40.dp,
                                                minHeight = 30.dp
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            color = MaterialTheme.colorScheme.primary,
                                            text = "播放中",
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.playVideo(item) },
                                        shape = MaterialTheme.shapes.small,
                                        contentPadding = PaddingValues(
                                            vertical = 4.dp,
                                            horizontal = 16.dp,
                                        ),
                                        modifier = Modifier
                                            .sizeIn(
                                                minWidth = 40.dp,
                                                minHeight = 30.dp
                                            )
                                            .padding(0.dp)
                                    ) {
                                        Text(text = "播放")
                                    }
                                }

                            }
                        }
                    }
                }
            }

            item(
//                span = {
//                    GridItemSpan(maxLineSpan)
//                }
            ) {
                Spacer(modifier = Modifier.height(windowInsets.bottomDp.dp))
            }
        }
    }
}