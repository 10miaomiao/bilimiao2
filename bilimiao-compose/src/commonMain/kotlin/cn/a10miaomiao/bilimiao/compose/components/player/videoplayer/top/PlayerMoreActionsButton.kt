@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.top

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * 「更多」菜单中的二级菜单
 *
 * 一级菜单只显示 [title]（右侧附带当前值 [value]），点击后在同一个 Popup 内原地切换到
 * [content]，由 [content] 渲染具体的选项列表，并可通过参数 `dismiss` 关闭整个菜单。
 *
 * 之所以在原地切换而不是再弹一个 Popup：嵌套 Popup 会让外层菜单失焦而被提前关闭，
 * 且点击外部时两个 Popup 的关闭顺序不可控。
 */
class PlayerMenuSubMenu(
    /** 一级菜单标题，如「清晰度」 */
    val title: String,
    /** 一级菜单右侧的当前值文案，如「1080P」 */
    val value: String,
    /** 二级菜单内容，`dismiss` 用于选中后关闭整个菜单 */
    val content: @Composable (dismiss: () -> Unit) -> Unit,
)

/**
 * 播放器顶栏"更多"按钮（三个点）
 *
 * 点击弹出菜单，包含调用方传入的二级菜单入口（如清晰度、倍速、字幕）以及播放设置、弹幕设置。
 *
 * @param onVideoSetting 点击"播放设置"回调
 * @param onDanmakuSetting 点击"弹幕设置"回调
 * @param modifier 布局修饰符
 * @param subMenus 二级菜单列表，为空时菜单只有播放设置 / 弹幕设置
 * @param onExpandedChanged 菜单展开状态变化回调（供调用方在菜单展开期间保持控制器常显）
 */
@Composable
fun PlayerMoreActionsButton(
    onVideoSetting: () -> Unit,
    onDanmakuSetting: () -> Unit,
    modifier: Modifier = Modifier,
    subMenus: List<PlayerMenuSubMenu> = emptyList(),
    onExpandedChanged: (Boolean) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    // 当前展开的二级菜单下标，null 表示显示一级菜单
    var openedSubMenuIndex by remember { mutableStateOf<Int?>(null) }
    // 菜单展开期间需要保持控制器常显，否则控制器自动隐藏会把整个顶栏（连同菜单）一起撤下
    val expandedChanged by rememberUpdatedState(onExpandedChanged)
    LaunchedEffect(expanded) {
        expandedChanged(expanded)
    }
    // 组件被移出组合（控制器收起）时兜底取消，避免控制器再也无法自动隐藏
    DisposableEffect(Unit) {
        onDispose { expandedChanged(false) }
    }
    // 菜单被关闭时（选中选项、点击外部）一并退回一级菜单
    fun dismiss() {
        expanded = false
        openedSubMenuIndex = null
    }

    val openedSubMenu = openedSubMenuIndex?.let { subMenus.getOrNull(it) }
    Box(modifier) {
        IconButton(
            onClick = {
                openedSubMenuIndex = null
                expanded = true
            },
        ) {
            Icon(Icons.Rounded.MoreVert, "更多")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { dismiss() },
        ) {
            if (openedSubMenu == null) {
                subMenus.forEachIndexed { index, subMenu ->
                    DropdownMenuItem(
                        text = { Text(subMenu.title) },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    subMenu.value,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = LocalContentColor.current.copy(alpha = 0.7f),
                                )
                                Icon(
                                    Icons.Rounded.ChevronRight,
                                    "展开",
                                    Modifier.size(18.dp),
                                )
                            }
                        },
                        onClick = { openedSubMenuIndex = index },
                    )
                }
                DropdownMenuItem(
                    text = { Text("播放设置") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Settings, null)
                    },
                    onClick = {
                        dismiss()
                        onVideoSetting()
                    },
                )
                DropdownMenuItem(
                    text = { Text("弹幕设置") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Subtitles, null)
                    },
                    onClick = {
                        dismiss()
                        onDanmakuSetting()
                    },
                )
            } else {
                // 二级菜单：顶部返回项兼作标题
                DropdownMenuItem(
                    text = { Text(openedSubMenu.title) },
                    leadingIcon = {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "返回")
                    },
                    onClick = { openedSubMenuIndex = null },
                )
                HorizontalDivider()
                openedSubMenu.content { dismiss() }
            }
        }
    }
}
