@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.top

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * 播放器顶栏"更多"按钮（三个点）
 *
 * 点击弹出菜单，包含播放设置、弹幕设置入口。
 *
 * @param onVideoSetting 点击"播放设置"回调
 * @param onDanmakuSetting 点击"弹幕设置"回调
 * @param modifier 布局修饰符
 */
@Composable
fun PlayerMoreActionsButton(
    onVideoSetting: () -> Unit,
    onDanmakuSetting: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Rounded.MoreVert, "更多")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("播放设置") },
                leadingIcon = {
                    Icon(Icons.Rounded.Settings, null)
                },
                onClick = {
                    expanded = false
                    onVideoSetting()
                },
            )
            DropdownMenuItem(
                text = { Text("弹幕设置") },
                leadingIcon = {
                    Icon(Icons.Rounded.Subtitles, null)
                },
                onClick = {
                    expanded = false
                    onDanmakuSetting()
                },
            )
        }
    }
}
