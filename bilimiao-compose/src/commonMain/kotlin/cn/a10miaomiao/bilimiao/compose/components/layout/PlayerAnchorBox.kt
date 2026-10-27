package cn.a10miaomiao.bilimiao.compose.components.layout

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import cn.a10miaomiao.bilimiao.compose.common.localPlayerState
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.kodein.di.compose.rememberInstance

/**
 * 播放器锚点容器组件。
 *
 * 两件事分别上报给 PlayerState：
 * - 是否启用：落点所属视频是不是当前播放视频，由本组件自己判断，组合后立即上报；
 * - 落点矩形：布局后上报，与是否正在播放无关，页面一进入就持续跟随位置变化。
 *
 * 落点是否在屏幕内由显示模式判定（calculatePlayerDisplayMode）结合宿主视口判断，
 * 这里只负责上报真实位置，不做可见性裁剪。
 *
 * 封面淡出只用透明度，不用 AnimatedContent（后者会为进出内容做尺寸/裁剪过渡，
 * 导致落点矩形抖动）。
 *
 * @param aid 关联的视频 aid，用于判断播放器是否应覆盖到此位置
 * @param modifier 修饰符
 * @param content 封面内容，当播放器覆盖时淡出
 */
@Composable
fun PlayerAnchorBox(
    aid: String,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val playerState = localPlayerState()
    val playerStore by rememberInstance<PlayerStore>()
    // 只订阅当前播放 aid：播放信息（标题、分P 等）变化不必让锚点容器重组
    val playingAidFlow = remember(playerStore) {
        playerStore.stateFlow.map { it.aid }.distinctUntilChanged()
    }
    val playingAid by playingAidFlow.collectAsState(initial = playerStore.state.aid)

    val isAnchorEnabled = playingAid == aid
    // 组合后立即上报启用状态：此时宿主布局还没进入测量，因此「播放与页面同帧出现」
    // 也能直接按锚点布局，不需要先显示一帧悬浮窗
    SideEffect {
        playerState.setAnchorEnabled(isAnchorEnabled)
    }

    // 播放器覆盖到此位置时淡出封面：只动画透明度，不改变布局
    val contentAlpha by animateFloatAsState(
        targetValue = if (isAnchorEnabled && playerState.showPlayer) 0f else 1f,
        label = "playerAnchorContentAlpha",
    )

    Box(
        modifier = modifier
            .alpha(contentAlpha)
            .onGloballyPositioned { coordinates ->
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
    ) {
        content()
    }

    // 登记/注销落点容器：只有最后一个容器离开时才会清理落点，
    // 避免转场期间旧页面退出把新页面刚上报的落点清掉
    DisposableEffect(Unit) {
        playerState.attachAnchorHost()
        onDispose {
            playerState.detachAnchorHost()
        }
    }
}
