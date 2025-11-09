package cn.a10miaomiao.bilimiao.compose.components.start

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoCard
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoOutlinedCard
import com.a10miaomiao.bilimiao.comm.store.PlayListStore
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import org.kodein.di.compose.rememberInstance

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun StartPlayerCard(
    modifier: Modifier = Modifier,
    aid: String = "",
    title: String = "",
    cover: String = "",
    onClick: () -> Unit = {},
    onLyricClick: () -> Unit = {},
    onPlayListClick: () -> Unit = {},
    onCloseClick: () -> Unit = {},
) {
    val playListStore by rememberInstance<PlayListStore>()
    val playListState by playListStore.stateFlow.collectAsState()

    MiaoOutlinedCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            GlideImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .matchParentSize()
                    .alpha(0.1f),
                model = UrlUtil.autoHttps(cover),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
            GlideImage(
                modifier = Modifier
                    .size(80.dp)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(5.dp)),
                model = UrlUtil.autoHttps(cover),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 80.dp,
                        end = 8.dp,
                        top = 8.dp,
                    ),
                verticalArrangement = Arrangement.Center,
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        maxLines = 2,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f),
                    )
                    IconButton(
                        onClick = onCloseClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f), // 使用主题错误色
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭播放",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AssistChip(
                        onClick = onLyricClick,
                        label = {
                            Text(
                                text = "歌词",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                    )
                    if (!playListState.isEmpty()) {
                        AssistChip(
                            onClick = onPlayListClick,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = "play list",
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            label = {
                                Text(
                                    text = playListState.name ?: "播放列表",
                                    maxLines = 1,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            trailingIcon = {
                                Text(
                                    text = "${playListState.indexOfAid(aid) + 1}/${playListState.items.size}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}