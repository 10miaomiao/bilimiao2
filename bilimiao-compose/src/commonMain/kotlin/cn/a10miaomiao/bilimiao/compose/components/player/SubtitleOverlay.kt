package cn.a10miaomiao.bilimiao.compose.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.SubtitleItem

/**
 * CC 字幕覆盖层
 *
 * B 站字幕为私有 JSON 格式（非标准 srt/vtt），mediamp/ExoPlayer 无法直接解析，
 * 因此采用自绘方案（参考旧安卓版 `DanmakuVideoPlayer` 的字幕实现）：
 * 根据当前播放位置在视频画面底部绘制字幕文本。
 *
 * 数据来源：[com.a10miaomiao.bilimiao.comm.delegate.player.PlayerDelegateImpl] 下载解析
 * 后放入 [PlayerSourceState][com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceState] 的
 * subtitleItems（毫秒时间轴，按开始时间升序）。
 *
 * 字幕行的匹配采用推进式查找（对齐旧版 `subtitleTask` 的实现）：记住当前字幕行的
 * 起止时间与索引，播放位置更新时先判断是否仍位于当前行内，再决定向前/向后移动，
 * 顺序播放时每次仅 O(1) 判断，仅 seek 跳转时才会线性推进到目标行。
 *
 * 样式对齐旧版：白色文字 + 半透明黑色圆角底，字号固定 16sp。
 * 字幕距底部的间距由 [VideoScaffold][cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.VideoScaffold]
 * 字幕槽位按控制栏可见状态提供：底栏显示时贴底栏上缘，隐藏时画面底部留白。
 *
 * @param subtitleItems 字幕行（毫秒时间轴，按开始时间升序）
 * @param currentPosition 当前播放位置（毫秒）
 * @param modifier 布局修饰符
 */
@Composable
fun SubtitleOverlay(
    subtitleItems: List<SubtitleItem>,
    currentPosition: Long,
    modifier: Modifier = Modifier,
) {
    // 当前字幕行的索引（推进式查找状态）；字幕列表切换（换视频/换字幕源）时重置
    var subtitleIndex by remember(subtitleItems) { mutableIntStateOf(-1) }

    // 播放位置更新时推进字幕索引：以上一帧索引为起点，用当前行的起止时间
    // 判断播放位置是否在其中，决定停留/向前/向后切换
    LaunchedEffect(subtitleItems, currentPosition) {
        subtitleIndex = advanceSubtitleIndex(
            items = subtitleItems,
            position = currentPosition,
            lastIndex = subtitleIndex,
        )
    }

    // 索引可能停留在字幕间隙（当前行尚未开始）或所有字幕之外，
    // 需要额外校验当前位置是否真的落在该行的 from..to 范围内
    val item = subtitleItems.getOrNull(subtitleIndex)
    if (item == null || currentPosition !in item.from..item.to) return

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Text(
            text = item.content,
            color = Color.White,
            fontSize = SUBTITLE_FONT_SIZE,
            lineHeight = SUBTITLE_FONT_SIZE * 1.35f,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x66000000))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

/**
 * 字幕字号（对齐旧版 layout_danmaku_palyer.xml 中 bottom_subtitle 的 16sp）
 */
private val SUBTITLE_FONT_SIZE = 16.sp

/**
 * 推进式查找当前播放位置对应的字幕行索引
 *
 * 从上一帧的索引 [lastIndex] 出发，比较当前行的起止时间与播放位置 [position]：
 * - 位置仍落在当前行内：保持索引不变；
 * - 位置早于当前行开始时间：向前（索引 -1）移动；
 * - 位置晚于当前行结束时间：向后（索引 +1）移动。
 *
 * 字幕行之间可能存在空隙（当前行尚未开始、上一行已结束），此时返回空隙后的行
 * （未开始的当前行），由调用方依据 from/to 决定是否显示，避免空隙期间反复回扫。
 * 位于所有字幕之前/之后时同样返回一个"不匹配"的停留索引交给调用方隐藏。
 * 顺序播放时每次仅 O(1)；仅首次定位或 seek 大跳转时线性推进。
 *
 * @return 停留的字幕行索引（-1 仅表示列表为空）
 */
private fun advanceSubtitleIndex(
    items: List<SubtitleItem>,
    position: Long,
    lastIndex: Int,
): Int {
    if (items.isEmpty()) return -1
    var index = lastIndex.coerceIn(0, items.size - 1)
    while (index in items.indices) {
        val item = items[index]
        when {
            // 播放位置早于当前行开始时间：需要往前找
            position < item.from -> {
                if (index > 0 && position > items[index - 1].to) {
                    // 位于上一行结束与当前行开始之间的空隙：停在当前行，等播放进入后再显示
                    return index
                }
                index--
            }
            // 播放位置晚于当前行结束时间：需要往后找
            position > item.to -> {
                if (index == items.size - 1) return index
                index++
            }
            // 播放位置落在当前行的起止时间范围内
            else -> return index
        }
    }
    return index.coerceIn(0, items.size - 1)
}
