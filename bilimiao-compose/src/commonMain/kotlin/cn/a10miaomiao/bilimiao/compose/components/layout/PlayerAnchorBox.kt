package cn.a10miaomiao.bilimiao.compose.components.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import cn.a10miaomiao.bilimiao.compose.common.localPlayerState
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import org.kodein.di.compose.rememberInstance

/**
 * 播放器锚点容器组件。
 *
 * 当播放器正在播放 [aid] 对应的视频时，播放器窗口会覆盖到此组件的位置上，
 * 同时 [content]（通常是封面）会被隐藏。
 *
 * 当播放器未播放此视频时，[content] 正常显示。
 *
 * @param aid 关联的视频 aid，用于判断播放器是否应覆盖到此位置
 * @param modifier 修饰符
 * @param content 封面内容，当播放器覆盖时隐藏
 */
@Composable
fun PlayerAnchorBox(
    aid: String,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val playerState = localPlayerState()
    val playerStore by rememberInstance<PlayerStore>()
    val playerStoreState by playerStore.stateFlow.collectAsState()

    val isPlayingThisVideo = playerStoreState.aid == aid && playerState.showPlayer

    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            if (isPlayingThisVideo) {
                val position = coordinates.positionInRoot()
                val size = coordinates.size
                playerState.setAnchorBounds(
                    Rect(
                        left = position.x,
                        top = position.y,
                        right = position.x + size.width,
                        bottom = position.y + size.height,
                    )
                )
            }
        }
    ) {
        // 当播放器覆盖到此位置时，隐藏封面内容
        AnimatedVisibility(
            visible = !isPlayingThisVideo,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            content()
        }
    }

    // 当组件离开组合或不再播放此视频时，清除 anchorBounds
    DisposableEffect(aid) {
        onDispose {
            if (playerState.anchorBounds != null) {
                playerState.setAnchorBounds(null)
            }
        }
    }

    // 当 isPlayingThisVideo 变为 false 时清除 anchorBounds
    LaunchedEffect(isPlayingThisVideo) {
        if (!isPlayingThisVideo && playerState.anchorBounds != null) {
            playerState.setAnchorBounds(null)
        }
    }
}
