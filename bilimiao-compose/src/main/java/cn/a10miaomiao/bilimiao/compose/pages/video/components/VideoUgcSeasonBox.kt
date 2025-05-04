package cn.a10miaomiao.bilimiao.compose.pages.video.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.pages.video.VideoDetailViewModel
import com.a10miaomiao.bilimiao.comm.store.PlayListStore
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import okhttp3.internal.toLongOrDefault
import org.kodein.di.compose.rememberInstance

@Composable
fun VideoUgcSeasonBox(
    modifier: Modifier = Modifier,
    viewModel: VideoDetailViewModel,
    arc: bilibili.app.archive.v1.Arc,
    ugcSeason: bilibili.app.view.v1.UgcSeason,
    isExpand: Boolean = false,
    onChangeExpand: (Boolean) -> Unit = {},
) {
    val playerStore by rememberInstance<PlayerStore>()
    val playerState by playerStore.stateFlow.collectAsState()
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
                            if (isExpand) {
                                viewModel.toUgcSeasonPage(
                                    seasonId = ugcSeason.id.toString(),
                                    seasonTitle = ugcSeason.title,
                                )
                            } else {
                                onChangeExpand(true)
                            }
                        }
                        .padding(5.dp),
                ) {
                    Text(
                        text = ugcSeason.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${ugcSeason.epCount}个视频>",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "自动连播",
                    style = MaterialTheme.typography.labelMedium,
                )
                Switch(
                    modifier = Modifier.scale(0.75f),
                    checked = viewModel.isAutoPlaySeason,
                    onCheckedChange = viewModel::updateIsAutoPlaySeason,
                )
            }

//            if (ugcSeason.sections.size > 1) {
//                LazyRow(
//                    horizontalArrangement = Arrangement.spacedBy(5.dp),
//                    contentPadding = PaddingValues(horizontal = 5.dp),
//                ) {
//                    items(ugcSeason.sections.size) {
//                        val s = ugcSeason.sections[it]
//                        FilterChip(
//                            selected = false,
//                            onClick = {
//
//                            },
//                            label = {
//                                Text(text = s.title)
//                            }
//                        )
//                    }
//                }
//            }

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
                        val currentAid = arc.aid
                        val playAid = playerState.aid.toLongOrDefault(0L)
                        if (ugcSeason.sections.size > 1) {
                            for (s in ugcSeason.sections) {
                                item(
                                    span = { GridItemSpan(maxLineSpan) },
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .width(80.dp)
                                            .padding(5.dp),
                                        horizontalAlignment = Alignment.End,
                                    ) {
                                        Text(
                                            text = s.title + ":",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.End,
                                        )
                                        Text(
                                            text = "${s.episodes.size}个视频",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.End,
                                        )
                                    }
                                }
                                items(
                                    s.episodes.size,
                                    key = { s.episodes[it].id }
                                ) { index ->
                                    val episode = s.episodes[index]
                                    VideoEpisodeBox(
                                        modifier = Modifier.width(240.dp),
                                        title = episode.title,
                                        cover = episode.cover,
                                        desc = episode.coverRightText,
                                        isCurrent = currentAid == episode.aid,
                                        isPlaying = playAid == episode.aid,
                                        onClick = {
                                            viewModel.changeVideo(episode.aid.toString())
                                        },
                                    )
                                }
                            }
                        } else if (ugcSeason.sections.isNotEmpty()) {
                            val s = ugcSeason.sections[0]
                            items(
                                s.episodes.size,
                                key = { s.episodes[it].id },
                            ) { index ->
                                val episode = s.episodes[index]
                                VideoEpisodeBox(
                                    modifier = Modifier.width(240.dp),
                                    title = episode.title,
                                    cover = episode.cover,
                                    desc = episode.coverRightText,
                                    isCurrent = currentAid == episode.aid,
                                    isPlaying = playAid == episode.aid,
                                    onClick = {
                                        viewModel.changeVideo(episode.aid.toString())
                                    },
                                )
                            }
                        }
                    }

                    LaunchedEffect(arc.aid) {
                        var offset = if (ugcSeason.sections.size > 1) 1 else 0
                        var eIndex = 0
                        val has = ugcSeason.sections.any { s ->
                            eIndex = s.episodes.indexOfFirst { it.aid == arc.aid }
                            if (eIndex == -1) {
                                offset += s.episodes.size + 1
                                false
                            } else {
                                true
                            }
                        }
                        val index = offset + eIndex
                        if (has && index > 0) {
                            listState.scrollToItem(index)
                        }
                    }
                }
            }
        }
    }
}