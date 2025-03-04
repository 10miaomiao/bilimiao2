package cn.a10miaomiao.bilimiao.compose.pages.video.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.assets.BilimiaoIcons
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.Common
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Bilicoin
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Bilifavourite
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Bililike
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Bilishare
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun VideoStatBox(
    stat: bilibili.app.archive.v1.Stat,
    reqUser: bilibili.app.view.v1.ReqUser,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        VideoStatButton(
            modifier = Modifier.weight(1f),
            icon = BilimiaoIcons.Common.Bililike,
            text = NumberUtil.converString(stat.like),
            description = "点赞",
            color = if (reqUser.like == 0) {
                MaterialTheme.colorScheme.outline
            } else {
                MaterialTheme.colorScheme.primary
            },
            onClick = {

            },
        )
        VideoStatButton(
            modifier = Modifier.weight(1f),
            icon = BilimiaoIcons.Common.Bilicoin,
            text = NumberUtil.converString(stat.coin),
            description = "投币",
            color = if (reqUser.coin == 0) {
                MaterialTheme.colorScheme.outline
            } else {
                MaterialTheme.colorScheme.primary
            },
            onClick = {

            },
        )
        VideoStatButton(
            modifier = Modifier.weight(1f),
            icon = BilimiaoIcons.Common.Bilifavourite,
            text = NumberUtil.converString(stat.fav),
            description = "收藏",
            color = if (reqUser.favorite == 0) {
                MaterialTheme.colorScheme.outline
            } else {
                MaterialTheme.colorScheme.primary
            },
            onClick = {

            },
        )
        VideoStatButton(
            modifier = Modifier.weight(1f),
            icon = BilimiaoIcons.Common.Bilishare,
            text = NumberUtil.converString(stat.share),
            description = "分享",
            color = MaterialTheme.colorScheme.outline,
            onClick = {

            },
        )
    }
}

@Composable
fun VideoStatButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    description: String,
    color: Color,
    onClick: () -> Unit,
) {
    TextButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                modifier = Modifier.size(24.dp),
                tint = color,
            )
            Text(
                text = text,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}