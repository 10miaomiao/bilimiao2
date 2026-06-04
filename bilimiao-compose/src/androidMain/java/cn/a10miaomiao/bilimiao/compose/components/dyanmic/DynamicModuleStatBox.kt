package cn.a10miaomiao.bilimiao.compose.components.dyanmic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.assets.BilimiaoIcons
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.Common
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Like
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Likefill
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Reply
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Share
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil


@Composable
fun DynamicModuleStatBox(
    stat: bilibili.app.dynamic.v2.ModuleStat,
) {
    DynamicModuleStatBox(
        share = stat.repost,
        reply = stat.reply,
        like = stat.like,
        isLike = stat.likeInfo?.isLike == true,
    )
}

@Composable
fun DynamicModuleStatBox(
    share: Long,
    reply: Long,
    like: Long,
    isLike: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(
            onClick = { /*TODO*/ },
            contentPadding = PaddingValues(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    BilimiaoIcons.Common.Share,
                    contentDescription = "share",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 4.dp)
                        .size(16.dp)
                )
                Text(
                    text = NumberUtil.converString(share),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        TextButton(
            onClick = { /*TODO*/ },
            contentPadding = PaddingValues(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    BilimiaoIcons.Common.Reply,
                    contentDescription = "reply",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 4.dp)
                        .size(16.dp)
                )
                Text(
                    text = NumberUtil.converString(reply),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        TextButton(
            onClick = { /*TODO*/ },
            contentPadding = PaddingValues(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isLike) {
                    Icon(
                        BilimiaoIcons.Common.Likefill,
                        contentDescription = "like",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 4.dp)
                            .size(16.dp)
                    )
                } else {
                    Icon(
                        BilimiaoIcons.Common.Like,
                        contentDescription = "like",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(end = 4.dp)
                            .size(16.dp)
                    )
                }
                Text(
                    text = NumberUtil.converString(like),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}