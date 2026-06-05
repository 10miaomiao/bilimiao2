package cn.a10miaomiao.bilimiao.compose.pages.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoCard
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import coil3.compose.AsyncImage
import bilimiao.bilimiao_compose.generated.resources.Res
import bilimiao.bilimiao_compose.generated.resources.ic_region_ad
import bilimiao.bilimiao_compose.generated.resources.ic_region_dh
import bilimiao.bilimiao_compose.generated.resources.ic_region_dsj
import bilimiao.bilimiao_compose.generated.resources.ic_region_dy
import bilimiao.bilimiao_compose.generated.resources.ic_region_fj
import bilimiao.bilimiao_compose.generated.resources.ic_region_fj_domestic
import bilimiao.bilimiao_compose.generated.resources.ic_region_gc
import bilimiao.bilimiao_compose.generated.resources.ic_region_kj
import bilimiao.bilimiao_compose.generated.resources.ic_region_sh
import bilimiao.bilimiao_compose.generated.resources.ic_region_ss
import bilimiao.bilimiao_compose.generated.resources.ic_region_wd
import bilimiao.bilimiao_compose.generated.resources.ic_region_yl
import bilimiao.bilimiao_compose.generated.resources.ic_region_ys
import bilimiao.bilimiao_compose.generated.resources.ic_region_yx
import bilimiao.bilimiao_compose.generated.resources.ic_region_yy
import bilimiao.bilimiao_compose.generated.resources.ic_season_0
import bilimiao.bilimiao_compose.generated.resources.ic_season_1
import bilimiao.bilimiao_compose.generated.resources.ic_season_2
import bilimiao.bilimiao_compose.generated.resources.ic_season_3
import bilimiao.bilimiao_compose.generated.resources.ic_time
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.random.Random

private val regionIconMap by lazy {
    mapOf(
        "ic_region_fj" to Res.drawable.ic_region_fj,
        "ic_region_fj_domestic" to Res.drawable.ic_region_fj_domestic,
        "ic_region_dh" to Res.drawable.ic_region_dh,
        "ic_region_yy" to Res.drawable.ic_region_yy,
        "ic_region_wd" to Res.drawable.ic_region_wd,
        "ic_region_yx" to Res.drawable.ic_region_yx,
        "ic_region_kj" to Res.drawable.ic_region_kj,
        "ic_region_sh" to Res.drawable.ic_region_sh,
        "ic_region_gc" to Res.drawable.ic_region_gc,
        "ic_region_ss" to Res.drawable.ic_region_ss,
        "ic_region_ad" to Res.drawable.ic_region_ad,
        "ic_region_yl" to Res.drawable.ic_region_yl,
        "ic_region_ys" to Res.drawable.ic_region_ys,
        "ic_region_dy" to Res.drawable.ic_region_dy,
        "ic_region_dsj" to Res.drawable.ic_region_dsj,
    )
}

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
                when (iconModel) {
                    is DrawableResource -> {
                        Image(
                            painter = painterResource(iconModel),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    else -> {
                        AsyncImage(
                            model = iconModel,
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .padding(end = 4.dp)
                        )
                    }
                }
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
            0 -> Res.drawable.ic_season_0
            1 -> Res.drawable.ic_season_1
            2 -> Res.drawable.ic_season_2
            3 -> Res.drawable.ic_season_3
            else -> Res.drawable.ic_season_0
        }
    }
    val emoticons = remember {
        val random = Random
        val emoticonsArr = arrayOf("ε=ε=ε=┏(゜ロ゜;)┛", "(　o=^•ェ•)o　┏━┓", "(/▽＼)", "ヽ(✿ﾟ▽ﾟ)ノ")
        emoticonsArr[random.nextInt(emoticonsArr.size)]
    }
    HomeTimeMachineCard(
        iconModel = Res.drawable.ic_time,
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
                            painter = painterResource(seasonIcon),
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
    val iconModel: Any? = if (region.icon != null) {
        regionIconMap[region.icon]
    } else if (!region.logo.isNullOrBlank()) {
        UrlUtil.autoHttps(region.logo!!)
    } else null

    HomeTimeMachineCard(
        iconModel = iconModel,
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
