package cn.a10miaomiao.bilimiao.compose.pages.video.components

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.FilterChip
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
import cn.a10miaomiao.bilimiao.compose.pages.community.MainReplyViewModel
import cn.a10miaomiao.bilimiao.compose.pages.video.VideoDetailViewModel
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun VideoStatBox(
    modifier: Modifier = Modifier,
    viewModel: VideoDetailViewModel,
    arc: bilibili.app.archive.v1.Arc,
    stat: bilibili.app.archive.v1.Stat,
    reqUser: bilibili.app.view.v1.ReqUser,
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .then(modifier),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        VideoStatButton(
            icon = BilimiaoIcons.Common.Bililike,
            text = NumberUtil.converString(stat.like),
            description = "点赞",
            color = if (reqUser.like == 0) {
                MaterialTheme.colorScheme.outline
            } else {
                MaterialTheme.colorScheme.primary
            },
            onClick = {
                viewModel.requestLike(arc, reqUser)
            },
        )
        VideoStatButton(
            icon = BilimiaoIcons.Common.Bilicoin,
            text = NumberUtil.converString(stat.coin),
            description = "投币",
            color = if (reqUser.coin == 0) {
                MaterialTheme.colorScheme.outline
            } else {
                MaterialTheme.colorScheme.primary
            },
            onClick = {
                viewModel.openCoinDialog(
                    arc.aid.toString(),
                    arc.copyright,
                )
            },
        )
        VideoStatButton(
            icon = BilimiaoIcons.Common.Bilifavourite,
            text = NumberUtil.converString(stat.fav),
            description = "收藏",
            color = if (reqUser.favorite == 0) {
                MaterialTheme.colorScheme.outline
            } else {
                MaterialTheme.colorScheme.primary
            },
            onClick = {
                viewModel.openAddFavoriteDialog(arc.aid.toString())
            },
        )
        VideoStatButton(
            icon = BilimiaoIcons.Common.Bilishare,
            text = NumberUtil.converString(stat.share),
            description = "分享",
            color = MaterialTheme.colorScheme.outline,
            onClick = {
                viewModel.openShare("av${arc.aid}", arc.title)
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
    ElevatedAssistChip(
        onClick = onClick,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = description,
                modifier = Modifier.size(24.dp),
                tint = color,
            )
        },
        label = {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    )
//    TextButton(
//        modifier = modifier,
//        onClick = onClick,
//    ) {
//        Row(
//            verticalAlignment = Alignment.Bottom,
//        ) {
//
//            Text(
//                text = text,
//                color = MaterialTheme.colorScheme.outline,
//                modifier = Modifier.padding(start = 4.dp),
//            )
//        }
//    }
}