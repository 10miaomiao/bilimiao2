@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.a10miaomiao.bilimiao.compose.components.player.FastForwardTrianglesIcon

/**
 * 长按倍速播放指示浮窗
 *
 * 还原旧版（bilimiao 2.x）speed_tips：半透明黑圆角底 + 快进动态图标 + 白色文字。
 * 展示位置由调用方通过 [modifier] 控制（如居中偏上偏移）。
 *
 * @param modifier 位置/尺寸修饰符
 * @param text 提示文字
 * @param tint 图标与文字颜色
 */
@Composable
fun FastForwardIndicator(
    modifier: Modifier = Modifier,
    text: String = "倍速播放中",
    tint: Color = Color.White,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.Black.copy(alpha = 0.6f),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FastForwardTrianglesIcon(
                tint = tint,
                modifier = Modifier.size(width = 24.dp, height = 16.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text,
                color = tint,
                lineHeight = LocalTextStyle.current.lineHeight,
            )
        }
    }
}
