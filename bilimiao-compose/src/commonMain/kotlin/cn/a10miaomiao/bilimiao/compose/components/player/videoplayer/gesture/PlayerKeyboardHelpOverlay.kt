package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

const val TAG_PLAYER_KEYBOARD_HELP = "PlayerKeyboardHelp"

/** 可用宽度小于该值时改用单列排布, 避免键位标签被挤压. */
private val HELP_SINGLE_COLUMN_MAX_WIDTH = 520.dp

/** 键位标签列的最小宽度, 让不同行的说明文字尽量对齐. */
private val HELP_KEY_LABEL_MIN_WIDTH = 104.dp

/**
 * 播放器快捷键帮助浮层.
 *
 * 居中显示半透明黑底面板, 内容来自 [playerKeyboardShortcutGroups];
 * 宽屏两列、窄屏单列, 最多占用画面 80% 宽.
 *
 * @param visible 是否显示
 * @param onDismiss 自动关闭或外部请求关闭时调用
 * @param autoDismissMillis 显示多久后自动关闭, 避免长时间遮挡画面
 */
@Composable
fun PlayerKeyboardHelpOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    autoDismissMillis: Long = 5000L,
) {
    // 可见时开始倒计时, 到点自动关闭; 重新显示会重启计时
    LaunchedEffect(visible, autoDismissMillis) {
        if (!visible) return@LaunchedEffect
        delay(autoDismissMillis)
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier.testTag(TAG_PLAYER_KEYBOARD_HELP),
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        // 浮层与播放器手势层是兄弟节点: 不拦截指针事件的话, 点击面板会穿透到下方手势层
        // (暂停 / 切换控制器 / 滚轮调音量), 面板本身也点不掉.
        // 这里在最外层按 Initial 阶段消费全部事件, 并在按下时直接关闭浮层.
        val onDismissState by rememberUpdatedState(onDismiss)
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            event.changes.forEach { it.consume() }
                            // 面板任意位置按下即视为「点击面板关闭」
                            if (event.type == PointerEventType.Press) {
                                onDismissState()
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            val maxContentWidth = maxWidth * 0.8f
            val singleColumn = maxWidth < HELP_SINGLE_COLUMN_MAX_WIDTH
            // 分组做左右交替分配, 两列的行数尽量接近
            val (leftGroups, rightGroups) = remember {
                playerKeyboardShortcutGroups.filterIndexed { index, _ -> index % 2 == 0 } to
                    playerKeyboardShortcutGroups.filterIndexed { index, _ -> index % 2 == 1 }
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.78f),
            ) {
                Column(
                    Modifier
                        .widthIn(max = maxContentWidth)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = "键盘快捷键",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    if (singleColumn) {
                        ShortcutGroupColumn(playerKeyboardShortcutGroups)
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                            ShortcutGroupColumn(leftGroups)
                            ShortcutGroupColumn(rightGroups)
                        }
                    }
                }
            }
        }
    }
}

/** 一列分组, 分组之间留出稍大的间距. */
@Composable
private fun ShortcutGroupColumn(
    groups: List<PlayerShortcutGroup>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        groups.forEach { group ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = group.title,
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 12.sp,
                )
                group.items.forEach { item ->
                    ShortcutItemRow(item)
                }
            }
        }
    }
}

/** 一行键位说明: 左侧等宽字体小标签, 右侧动作文案. */
@Composable
private fun ShortcutItemRow(item: PlayerShortcutItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.widthIn(min = HELP_KEY_LABEL_MIN_WIDTH)) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                // 半透明白底胶囊, 在亮色画面上也能看清
                color = Color.White.copy(alpha = 0.16f),
            ) {
                Text(
                    text = item.keys,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
        Text(
            text = item.label,
            color = Color.White.copy(alpha = 0.92f),
            fontSize = 13.sp,
        )
    }
}
