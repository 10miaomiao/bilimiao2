package cn.a10miaomiao.bilimiao.compose.pages.playlist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.a10miaomiao.bilimiao.compose.assets.BilimiaoIcons
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.Common
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Upper
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoCard
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListItemInfo
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
internal fun PlayListItemCard(
    modifier: Modifier = Modifier,
    index: Int,
    item: PlayListItemInfo,
    onClick: () -> Unit,
    action: @Composable () -> Unit,
) {
    MiaoCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp, 60.dp)
                    .clip(RoundedCornerShape(10.dp)),
            ) {
                GlideImage(
                    model = UrlUtil.autoHttps(item.cover) + "@672w_378h_1c_",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(bottomEnd = 10.dp)
                        )
                        .padding(
                            vertical = 2.dp,
                            horizontal = 4.dp,
                        ),
                ) {
                    Text(
                        color = Color.White,
                        text = "${index + 1}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 5.dp),
            ) {
                Text(
                    text = item.title,
                    maxLines = 2,
                    modifier = Modifier.padding(bottom = 5.dp),
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleSmall,
                )
                Row {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        imageVector = BilimiaoIcons.Common.Upper,
                        contentDescription = null,
                    )
                    val pagesSize = item.videoPages.size
                    Text(
                        modifier = Modifier.padding(start = 2.dp),
                        text = if (pagesSize < 2)
                            item.ownerName
                        else
                            "${item.ownerName} · ${pagesSize}个分P",
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.outline,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                action()
            }
        }
    }
}