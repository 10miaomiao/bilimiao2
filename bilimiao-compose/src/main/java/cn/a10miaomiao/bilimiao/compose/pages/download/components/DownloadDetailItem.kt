package cn.a10miaomiao.bilimiao.compose.pages.download.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.R
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadItemInfo
import cn.a10miaomiao.bilimiao.download.entry.CurrentDownloadInfo
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun DownloadDetailItem(
    curDownload: CurrentDownloadInfo?,
    item: DownloadItemInfo,
    onClick: () -> Unit,
    onStartClick: () -> Unit,
    onPauseClick: (taskId: Long) -> Unit,
    onDeleteClick: () -> Unit,
) {
    var expandedMoreMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.padding(5.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column() {
                Row(
                    modifier = Modifier.padding(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GlideImage(
                        model = UrlUtil.autoHttps(item.cover) + "@672w_378h_1c_",
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                        loading = placeholder(R.drawable.bili_default_placeholder_img_tv),
                        failure = placeholder(R.drawable.bili_fail_placeholder_img_tv),
                        modifier = Modifier
                            .size(width = 60.dp, height = 40.dp)
                            .clip(RoundedCornerShape(5.dp))
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 5.dp)
                    ) {
                        Text(
                            text = item.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        val status = if (item.is_completed) {
                            "已完成下载"
                        } else if (item.cid == curDownload?.id) {
                            curDownload.statusText
                        } else {
                            "暂停中"
                        }
                        Text(
                            text = status,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (!item.is_completed) {
                        if (item.cid == curDownload?.id && curDownload.status in 100 until  200) {
                            IconButton(onClick = { onPauseClick(curDownload.taskId) }) {
                                Icon(Icons.Filled.Pause, null)
                            }
                        } else {
                            IconButton(onClick = onStartClick) {
                                Icon(Icons.Filled.PlayArrow, null)
                            }
                        }
                    }
                    Box() {
                        IconButton(
                            onClick = { expandedMoreMenu = true }
                        ) {
                            Icon(Icons.Filled.MoreVert, null)
                        }
                        DropdownMenu(
                            expanded = expandedMoreMenu,
                            onDismissRequest = { expandedMoreMenu = false },
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    expandedMoreMenu = false
                                    onDeleteClick()
                                },
                                text = {
                                    Text(text = "删除下载")
                                }
                            )
                        }
                    }

                }
                if (!item.is_completed) {
                    if (item.cid == curDownload?.id) {
                        LinearProgressIndicator(
                            progress = { curDownload.rate },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (item.total_bytes != 0L) {
                        LinearProgressIndicator(
                            progress = { item.downloaded_bytes.toFloat() / item.total_bytes.toFloat() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

        }
    }
}