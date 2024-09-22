package cn.a10miaomiao.bilimiao.compose.commponents.dyanmic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
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

@Composable
fun DynamicModuleStatBox(
    stat: bilibili.app.dynamic.v2.ModuleStat,
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
                    modifier = Modifier.padding(end = 4.dp)
                        .size(16.dp)
                )
                Text(
                    text = stat.repost.toString(),
                    style = MaterialTheme.typography.labelMedium
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
                    modifier = Modifier.padding(end = 4.dp)
                        .size(16.dp)
                )
                Text(
                    text = stat.reply.toString(),
                    style = MaterialTheme.typography.labelMedium
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
                if (stat.likeInfo?.isLike == true) {
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
                        modifier = Modifier.padding(end = 4.dp)
                            .size(16.dp)
                    )
                }
                Text(
                    text = stat.like.toString(),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}