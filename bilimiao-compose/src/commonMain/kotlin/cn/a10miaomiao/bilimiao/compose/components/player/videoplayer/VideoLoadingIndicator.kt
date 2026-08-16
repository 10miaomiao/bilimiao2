@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * 视频加载指示器.
 *
 * 可选地展示一个圆形进度指示器, 以及一段文本内容.
 *
 * @param showProgress 是否显示圆形进度指示器
 * @param text 加载状态文本
 * @param textStyle 文本样式
 */
@Composable
fun VideoLoadingIndicator(
    showProgress: Boolean,
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (showProgress) {
            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 3.dp)
        }

        Row(Modifier.padding(top = 8.dp)) {
            // 替换自 animeko 的 ProvideTextStyleContentColor, 使用标准 Compose API
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                ProvideTextStyle(textStyle) {
                    text()
                }
            }
        }
    }
}
