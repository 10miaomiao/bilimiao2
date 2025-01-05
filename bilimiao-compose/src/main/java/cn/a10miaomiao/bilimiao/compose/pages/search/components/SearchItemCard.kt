package cn.a10miaomiao.bilimiao.compose.pages.search.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bilibili.polymer.app.search.v1.Item.CardItem
import cn.a10miaomiao.bilimiao.compose.components.bangumi.BangumiItemBox
import cn.a10miaomiao.bilimiao.compose.components.video.VideoItemBox
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil

@Composable
internal fun SearchItemCard(
    cardItem: CardItem<*>,
    onClick: () -> Unit
) {
    when (cardItem) {
        is CardItem.Av -> {
            val avItem = cardItem.value
            VideoItemBox(
                modifier = Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 5.dp
                ),
                title = avItem.title,
                pic = avItem.cover,
                upperName = avItem.author + " " + avItem.showCardDesc2,
                playNum = NumberUtil.converString(avItem.play),
                damukuNum = NumberUtil.converString(avItem.danmaku),
                duration = avItem.duration,
                isHtml = true,
                onClick = onClick
            )
        }
        is CardItem.Bangumi -> {
            val bangumiItem = cardItem.value
            BangumiItemBox(
                modifier = Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 5.dp
                ),
                title = bangumiItem.title,
                cover = bangumiItem.cover,
                statusText = bangumiItem.styles,
                desc = bangumiItem.label,
                isHtml = true,
                onClick = onClick
            )
        }
        is CardItem.Author -> {
            val authorItem = cardItem.value
            AuthorItemBox(
                modifier = Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 5.dp
                ),
                name = authorItem.title,
                face = authorItem.cover,
                sign = authorItem.sign,
                fans = authorItem.fans,
                archives = authorItem.archives,
                level = authorItem.level,
                onClick = onClick
            )
        }
        else -> Unit
    }
}