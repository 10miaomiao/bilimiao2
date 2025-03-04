package cn.a10miaomiao.bilimiao.compose.pages.video.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoTitleBar
import cn.a10miaomiao.bilimiao.compose.pages.community.MainReplyViewModel

@Composable
fun VideoReplyTitleBar(
    modifier: Modifier = Modifier,
    viewModel: MainReplyViewModel,
    count: Int = -1,
) {
    val sortOrder by viewModel.sortOrder.collectAsState()
    val expanded = remember {
        mutableStateOf(false)
    }
    MiaoTitleBar(
        modifier = modifier,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "视频评论",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if (count > 0) {
                    Text(
                        text = "($count)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        },
        action = {
            IconButton(
                onClick = viewModel::openReplyDialog
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Comment,
                    contentDescription = "发布评论",
                )
            }
            Box {
                IconButton(
                    onClick = {
                        expanded.value = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "列表排序",
                    )
                }
                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = {
                        expanded.value = false
                    },
                ) {
                    viewModel.sortOrderList.forEach {
                        DropdownMenuItem(
                            text = {
                                Text(text = it.second)
                            },
                            onClick = {
                                viewModel.setSortOrder(it.first)
                                expanded.value = false
                            },
                            trailingIcon = {
                                if (it.first == sortOrder) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    )
}