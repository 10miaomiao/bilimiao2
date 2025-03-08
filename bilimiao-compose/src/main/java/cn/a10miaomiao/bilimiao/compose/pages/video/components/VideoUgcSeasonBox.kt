package cn.a10miaomiao.bilimiao.compose.pages.video.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.pages.video.VideoDetailViewModel

@Composable
fun VideoUgcSeasonBox(
    modifier: Modifier = Modifier,
    viewModel: VideoDetailViewModel,
    arc: bilibili.app.archive.v1.Arc,
    ugcSeason: bilibili.app.view.v1.UgcSeason,
) {
    Box(modifier) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            viewModel.toUgcSeasonPage(
                                seasonId = ugcSeason.id.toString(),
                                seasonTitle = ugcSeason.title,
                            )
                        }
                        .padding(5.dp),
                ) {
                    Text(
                        text = ugcSeason.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${ugcSeason.epCount}个视频>",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = "自动连播",
                    style = MaterialTheme.typography.labelMedium,
                )
                Switch(
                    modifier = Modifier.scale(0.75f),
                    checked = viewModel.isAutoPlaySeason,
                    onCheckedChange = viewModel::updateIsAutoPlaySeason,
                )
            }
        }
    }
}