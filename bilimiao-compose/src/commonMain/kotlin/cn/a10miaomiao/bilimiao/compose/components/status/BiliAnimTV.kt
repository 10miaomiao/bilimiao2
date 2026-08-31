package cn.a10miaomiao.bilimiao.compose.components.status

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import bilimiao.bilimiao_compose.generated.resources.Res
import bilimiao.bilimiao_compose.generated.resources.bili_anim_tv_chan_1
import bilimiao.bilimiao_compose.generated.resources.bili_anim_tv_chan_3
import bilimiao.bilimiao_compose.generated.resources.bili_anim_tv_chan_5
import bilimiao.bilimiao_compose.generated.resources.bili_anim_tv_chan_7
import bilimiao.bilimiao_compose.generated.resources.bili_anim_tv_chan_9
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

/**
 * 哔哩哔哩小电视加载动画
 *
 * 对应旧版安卓端的 [AnimationDrawable] 帧动画（res/drawable/bili_anim_tv_chan.xml）：
 * 5 帧循环播放，每帧 100ms。
 * 原图是深灰色线稿，使用白色 [ColorFilter.tint] 染白，适配深色播放器背景。
 *
 * @param size 动画尺寸
 */
@Composable
fun BiliAnimTV(
    modifier: Modifier = Modifier,
    size: Dp = 60.dp,
) {
    var frame by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            frame = (frame + 1) % 5
        }
    }
    val painter = when (frame) {
        1 -> painterResource(Res.drawable.bili_anim_tv_chan_3)
        2 -> painterResource(Res.drawable.bili_anim_tv_chan_5)
        3 -> painterResource(Res.drawable.bili_anim_tv_chan_7)
        4 -> painterResource(Res.drawable.bili_anim_tv_chan_9)
        else -> painterResource(Res.drawable.bili_anim_tv_chan_1)
    }
    Image(
        painter = painter,
        contentDescription = "loading",
        modifier = modifier.size(size),
        colorFilter = ColorFilter.tint(Color.White),
    )
}
