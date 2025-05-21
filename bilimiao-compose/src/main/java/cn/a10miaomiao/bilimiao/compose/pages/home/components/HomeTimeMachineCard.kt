package cn.a10miaomiao.bilimiao.compose.pages.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.R
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoCard
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import java.util.Random

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
internal fun HomeTimeMachineCard(
    iconModel: Any?,
    cardName: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    MiaoCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (iconModel != null) {
                GlideImage(
                    model = iconModel,
                    contentDescription = null,
                    modifier = Modifier
                        .size(30.dp)
                        .padding(end = 4.dp)
                )
            }
            Text(
                text = cardName,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 5.dp, start = 5.dp, end = 5.dp),
        ) {
            content()
        }
    }
}

@Composable
internal fun HomeTimeMachineTimeCard(
    timeText: String,
    timeSeason: Int,
    onClick: () -> Unit,
) {
    val seasonIcon = remember(timeSeason) {
        when(timeSeason) {
            0 -> R.drawable.ic_season_0
            1 -> R.drawable.ic_season_1
            2 -> R.drawable.ic_season_2
            3 -> R.drawable.ic_season_3
            else -> R.drawable.ic_season_0
        }
    }
    val emoticons = remember {
        val random = Random()
        val emoticonsArr = arrayOf("ε=ε=ε=┏(゜ロ゜;)┛", "(　o=^•ェ•)o　┏━┓", "(/▽＼)", "ヽ(✿ﾟ▽ﾟ)ノ")
        emoticonsArr[random.nextInt(emoticonsArr.size)]
    }
    HomeTimeMachineCard(
        iconModel = R.drawable.ic_time,
        cardName = "当前时间线",
        onClick = onClick
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                AssistChip(
                    onClick = onClick,
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = seasonIcon),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = timeText,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                )
            }
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = emoticons,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun HomeTimeMachineRegionCard(
    region: RegionInfo,
    onClick: (RegionInfo, Int) -> Unit
) {
    HomeTimeMachineCard(
        iconModel = if (region.icon != null) {
            region.icon
        } else if (!region.logo.isNullOrBlank()) {
            UrlUtil.autoHttps(region.logo!!)
        } else null,
        cardName = region.name,
        onClick = {
            onClick(region, 0)
        }
    ) {
        FlowRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            region.children?.forEachIndexed { index, child ->
                AssistChip(
                    onClick = { onClick(region, index) },
                    label = {
                        Text(
                            text = child.name,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                )
            }
        }
    }
}