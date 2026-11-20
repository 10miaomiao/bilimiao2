@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.gesture

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.ControllerVisibility
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.GestureFamily
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.PlayerControllerState
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.progress.PlayerProgressSliderState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

const val TAG_GESTURE_LOCK = "GestureLock"

/**
 * 手势锁屏按钮.
 *
 * @param isLocked 是否已锁定
 * @param onClick 点击回调, 通常切换锁定状态
 */
@Composable
fun GestureLock(
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier.testTag(TAG_GESTURE_LOCK),
        shape = RoundedCornerShape(16.dp),
        // 半透明黑色底, 保证在亮色画面上也清晰可见
        color = Color.Black.copy(alpha = 0.3f),
        // 替代原 animeko 的 outline.slightlyWeaken()
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    ) {
        IconButton(onClick) {
            val color = if (isLocked) {
                MaterialTheme.colorScheme.primary
            } else {
                Color.White
            }
            CompositionLocalProvider(LocalContentColor provides color) {
                if (isLocked) {
                    Icon(Icons.Outlined.Lock, contentDescription = "解锁屏幕")
                } else {
                    Icon(Icons.Outlined.LockOpen, contentDescription = "锁定屏幕")
                }
            }
        }
    }
}

/**
 * 处理点击事件并自动隐藏控制器.
 *
 * @see LockableVideoGestureHost
 */
@Composable
fun LockedScreenGestureHost(
    controllerVisibility: () -> ControllerVisibility,
    setFullVisible: (visible: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .clickable(
                remember { MutableInteractionSource() },
                indication = null,
                onClick = { setFullVisible(true) },
            ).fillMaxSize(),
    )

    if (controllerVisibility() == ControllerVisibility.Visible) {
        LaunchedEffect(true) {
            delay(2.seconds)
            setFullVisible(false)
        }
    }
    return
}

/**
 * 可锁定的视频手势宿主.
 *
 * 当 [locked] 为 `true` 时显示 [LockedScreenGestureHost], 仅响应点击以显示/隐藏控制器;
 * 否则委托给 [PlayerGestureHost] 处理完整手势.
 *
 * 简化自 animeko: 移除了 `playerState: MediampPlayer` 参数及基于 `PlaybackSpeed` feature
 * 的 `rememberPlayerFastSkipState` 默认值, `fastSkipState` 改由调用方直接传入.
 *
 * @param keyboardShortcuts 键盘快捷键回调集合, 原样透传给 [PlayerGestureHost]; `null` 时不安装快捷键
 */
@Composable
fun LockableVideoGestureHost(
    controllerState: PlayerControllerState,
    seekerState: SwipeSeekerState,
    progressSliderState: PlayerProgressSliderState,
    locked: Boolean,
    enableSwipeToSeek: Boolean,
    audioController: LevelController,
    brightnessController: LevelController,
    modifier: Modifier = Modifier,
    onTogglePauseResume: () -> Unit = {},
    onToggleFullscreen: () -> Unit = {},
    onExitFullscreen: () -> Unit = {},
    family: GestureFamily = GestureFamily.TOUCH,
    gestureIndicatorState: GestureIndicatorState = rememberGestureIndicatorState(),
    fastSkipState: FastSkipState? = null,
    fastSkipEnabled: Boolean = true,
    keyboardShortcuts: PlayerKeyboardShortcutCallbacks? = null,
) {
    if (locked) {
        LockedScreenGestureHost(
            { controllerState.visibility },
            controllerState.setFullVisible,
            modifier.testTag("LockedScreenGestureHost"),
        )
    } else {
        // 全部使用命名参数, 避免 PlayerGestureHost 参数顺序调整后位置参数错位
        PlayerGestureHost(
            controllerState = controllerState,
            seekerState = seekerState,
            progressSliderState = progressSliderState,
            indicatorState = gestureIndicatorState,
            fastSkipState = fastSkipState,
            enableSwipeToSeek = enableSwipeToSeek,
            audioController = audioController,
            brightnessController = brightnessController,
            modifier = modifier,
            onTogglePauseResume = onTogglePauseResume,
            onToggleFullscreen = onToggleFullscreen,
            onExitFullscreen = onExitFullscreen,
            family = family,
            fastSkipEnabled = fastSkipEnabled,
            keyboardShortcuts = keyboardShortcuts,
        )
    }
}
