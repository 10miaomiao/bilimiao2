package cn.a10miaomiao.bilimiao.compose.pages.bangumi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.a10miaomiao.bilimiao.comm.entity.bangumi.EpisodeInfo
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun BangumiEpisodeItem(
    modifier: Modifier = Modifier,
    item: EpisodeInfo,
    desc: String? = null,
    playerState: PlayerStore.State,
    onClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    var expandedMoreMenu by remember{ mutableStateOf(false) }
    val isPlaying = playerState.type == PlayerStore.BANGUMI && playerState.epid == item.id
    Row(
        modifier = modifier
            .clickable(
                enabled = !isPlaying,
                onClick = onClick,
            )
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(width = 120.dp, height = 80.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            GlideImage(
                model = UrlUtil.autoHttps(item.cover) + "@672w_378h_1c_",
                contentScale = ContentScale.Crop,
                contentDescription = null,
                modifier = Modifier
                    .size(width = 120.dp, height = 80.dp)
                    .clip(RoundedCornerShape(5.dp))
            )
            if (item.badge.isNotBlank()) {
                Box(
                    modifier = Modifier.padding(5.dp),
                ) {
                    Text(
                        text = item.badge,
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .background(
                                color = Color(item.badge_info.bg_color.toColorInt()),
                                shape = RoundedCornerShape(5.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .height(80.dp)
                .padding(start = 10.dp),
        ) {
            Text(
                text = item.title + if (item.long_title.isBlank()) "" else "_" + item.long_title,
                maxLines = 2,
                modifier = Modifier.weight(1f),
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                ) {
                    if (isPlaying) {
                        Text(
                            text = "正在播放",
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (!desc.isNullOrBlank()) {
                        Text(
                            text = desc,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Box() {
                    IconButton(
                        onClick = { expandedMoreMenu = true },
                    ) {
                        Icon(
                            Icons.Filled.MoreVert,
                            null,
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    DropdownMenu(
                        expanded = expandedMoreMenu,
                        onDismissRequest = { expandedMoreMenu = false },
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                expandedMoreMenu = false
                                onCommentClick()
                            },
                            text = {
                                Text(text = "查看评论")
                            }
                        )
                        DropdownMenuItem(
                            onClick = {
                                expandedMoreMenu = false
                                onShareClick()
                            },
                            text = {
                                Text(text = "分享剧集")
                            }
                        )
                    }
                }
            }
        }
    }
}