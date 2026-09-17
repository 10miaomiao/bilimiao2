@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package cn.a10miaomiao.bilimiao.compose.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 播放异常覆盖层
 *
 * 对齐旧版 `include_error_message_box.xml` + `ErrorMessageBoxController`：
 * 出错时整屏黑底覆盖，居中展示错误文案，下方提供「重试」与「关闭播放」。
 *
 * @param message 错误文案
 * @param onRetry 重试回调（重新加载当前播放源）
 * @param onClose 关闭播放回调
 * @param modifier 布局修饰符
 */
@Composable
fun PlayerErrorOverlay(
    message: String,
    onRetry: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlayerStatusOverlay(
        title = message,
        primaryText = "重试",
        onPrimaryClick = onRetry,
        onCloseClick = onClose,
        modifier = modifier,
    )
}

/**
 * 播放完成覆盖层
 *
 * 对齐旧版 `include_completion_box.xml` + `CompletionBoxController`：
 * 播放结束且无连播/循环可走时整屏黑底覆盖，居中展示「播放完成」，
 * 下方提供「重新播放」与「关闭播放」。
 *
 * @param onReplay 重新播放回调
 * @param onClose 关闭播放回调
 * @param modifier 布局修饰符
 */
@Composable
fun PlayerCompletionOverlay(
    onReplay: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlayerStatusOverlay(
        title = "播放完成",
        primaryText = "重新播放",
        onPrimaryClick = onReplay,
        onCloseClick = onClose,
        modifier = modifier,
    )
}

/**
 * 播放状态覆盖层（播放异常 / 播放完成共用的版式）
 *
 * 整屏黑底，居中展示主文案与操作按钮。按钮使用 [FlowRow] 排列，
 * 悬浮小窗（可缩至 200dp 宽）等窄幅场景下会自动换行而不被裁切。
 *
 * @param title 居中的主文案
 * @param primaryText 主操作按钮文案
 * @param onPrimaryClick 主操作按钮回调
 * @param onCloseClick 关闭播放回调
 */
@Composable
private fun PlayerStatusOverlay(
    title: String,
    primaryText: String,
    onPrimaryClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            // 拦截点击，避免穿透到覆盖层下方的控制器与播放手势
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TextButton(
                    onClick = onCloseClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.White.copy(alpha = 0.85f),
                    ),
                ) {
                    Text("关闭播放")
                }
                Button(onClick = onPrimaryClick) {
                    Text(primaryText)
                }
            }
        }
    }
}
