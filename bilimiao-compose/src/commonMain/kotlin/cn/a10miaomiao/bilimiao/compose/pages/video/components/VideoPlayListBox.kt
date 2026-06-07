package cn.a10miaomiao.bilimiao.compose.pages.video.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.pages.video.VideoDetailViewModel
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListFrom
import com.a10miaomiao.bilimiao.comm.store.PlayListStore
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import okhttp3.internal.toLongOrDefault
import org.kodein.di.compose.rememberInstance

@Composable
fun VideoPlayListBox(
    modifier: Modifier = Modifier,
    viewModel: VideoDetailViewModel,
    arc: bilibili.app.archive.v1.Arc,
    ugcSeason: bilibili.app.view.v1.UgcSeason?,
    playListState: PlayListStore.State,
    isExpand: Boolean = false,
    onChangeExpand: (Boolean) -> Unit = {},
) {
    val playerStore by rememberInstance<PlayerStore>()
    val playerState by playerStore.stateFlow.collectAsState()
    val position by remember {
        derivedStateOf {
            playListState.indexOfAid(arc.aid.toString())
        }
    }
    val listState = rememberLazyGridState()

    Box(modifier) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            if (!isExpand && playListState.inListForAid(arc.aid.toString())) {
                                onChangeExpand(true)
                            } else {
                                viewModel.toPlayListPage()
                            }
                        }
                        .padding(5.dp),
                ) {
                    Text(
                        text = "当前播放列表",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (position == -1) {
                            "${playListState.items.size}个视频>"
                        } else {
                            "在列表中${position + 1}/${playListState.items.size}>"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (ugcSeason != null && viewModel.isAutoPlaySeason) {
                    val playListFromId = (playListState.from as? PlayListFrom.Season)?.seasonId
                        ?: (playListState.from as? PlayListFrom.Section)?.seasonId
                    if (playListFromId != ugcSeason.id.toString() ||
                        !playListState.inListForAid(arc.aid.toString())) {
                        Text(
                            modifier = Modifier
                                .padding(5.dp),
                            text = "将自动替换列表",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            Box(
                modifier = Modifier.animateContentSize()
            ) {
                if (isExpand) {
                    LazyHorizontalGrid(
                        rows = GridCells.Fixed(4),
                        modifier = Modifier
                            .height(200.dp),
                        state = listState,
                    ) {
                        val currentAid = arc.aid.toString()
                        val playAid = playerState.aid
                        items(
                            playListState.items.size,
                            key = { playListState.items[it].aid },
                        ) { index ->
                            val item = playListState.items[index]
                            VideoEpisodeBox(
                                modifier = Modifier.width(240.dp),
                                title = item.title,
                                cover = item.cover,
                                desc = item.ownerName,
                                isCurrent = currentAid == item.aid,
                                isPlaying = playAid == item.aid,
                                onClick = {
                                    viewModel.changeVideo(item.aid)
                                },
                            )
                        }
                    }
                    LaunchedEffect(arc.aid) {
                        val index = playListState.indexOfAid(arc.aid.toString())
                        if (index > 0) {
                            listState.scrollToItem(index)
                        }
                    }
                }
            }
        }
    }
}