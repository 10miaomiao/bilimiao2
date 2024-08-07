package cn.a10miaomiao.bilimiao.compose.pages.playlist.commponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListItemInfo
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.skydoves.landscapist.glide.GlideImage

@Composable
internal fun PlayListItemCard(
    index: Int,
    item: PlayListItemInfo,
    currentPlayCid: String,
    onPlayClick: () -> Unit,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
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
                    imageModel = UrlUtil.autoHttps(item.cover) + "@672w_378h_1c_",
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
                Text(
                    text = "UP:" + item.ownerName,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.outline,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                if (currentPlayCid == item.cid) {
                    Box(
                        modifier = Modifier
                            .sizeIn(
                                minWidth = 50.dp,
                                minHeight = 30.dp
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            color = MaterialTheme.colorScheme.primary,
                            text = "播放中",
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Button(
                        onClick = onPlayClick,
                        shape = MaterialTheme.shapes.small,
                        contentPadding = PaddingValues(
                            vertical = 4.dp,
                            horizontal = 12.dp,
                        ),
                        modifier = Modifier
                            .sizeIn(
                                minWidth = 40.dp,
                                minHeight = 30.dp
                            )
                            .padding(0.dp)
                    ) {
                        Text(
                            text = "播放",
                            fontSize = 12.sp
                        )
                    }
                }

            }
        }
    }
}