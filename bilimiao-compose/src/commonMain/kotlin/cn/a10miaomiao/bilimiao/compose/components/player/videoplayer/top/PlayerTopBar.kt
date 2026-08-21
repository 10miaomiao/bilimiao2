@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.top

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import cn.a10miaomiao.bilimiao.compose.components.layout.PlayerDisplayMode

/**
 * 播放器顶部导航栏.
 *
 * @param modifier
 * @param title 标题内容
 * @param isFullscreen 是否全屏
 * @param onExitFullscreen 退出全屏回调
 * @param onClose 关闭播放回调
 * @param actions 顶栏右侧操作区（如画中画、更多菜单）
 */
@Composable
fun PlayerTopBar(
    modifier: Modifier = Modifier,
    title: String = "",
    isFullscreen: Boolean = false,
    onExitFullscreen: () -> Unit = {},
    onClose: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    val contentColor = LocalContentColor.current
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isFullscreen) {
            IconButton(onClick = onExitFullscreen) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    "退出全屏",
                    tint = contentColor,
                )
            }
        } else {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Rounded.Close,
                    "关闭播放",
                    tint = contentColor,
                )
            }
        }
        Text(
            text = title,
            color = contentColor,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        actions()
    }
}
