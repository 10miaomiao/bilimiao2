package cn.a10miaomiao.bilimiao.compose.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.DpSize
import cn.a10miaomiao.bilimiao.compose.PlayerState
import cn.a10miaomiao.bilimiao.compose.common.emitter.SharedFlowEmitter
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigator
import cn.a10miaomiao.bilimiao.compose.components.layout.PlayerDisplayMode


private fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}

internal val LocalPageNavigation = staticCompositionLocalOf<PageNavigator> {
    noLocalProvidedFor("PageNavigation")
}

@Composable
fun localPageNavigation() = LocalPageNavigation.current

internal val LocalEmitter = staticCompositionLocalOf<SharedFlowEmitter> {
    noLocalProvidedFor("SharedFlowEmitter")
}

@Composable
fun localEmitter() = LocalEmitter.current

internal val LocalPlayerState = staticCompositionLocalOf<PlayerState> {
    noLocalProvidedFor("PlayerState")
}

@Composable
fun localPlayerState() = LocalPlayerState.current

/**
 * 当前播放器显示模式，由 PlayerLayer 提供。
 *
 * 显示模式只在 ComposeScaffold 里判定一次（含落点是否在屏幕内的判断），播放器 UI 直接读取，
 * 不再自己重算，避免两处判定不一致（例如宿主已按悬浮窗布局，播放器 UI 仍以为在锚点模式）。
 */
internal val LocalPlayerDisplayMode = staticCompositionLocalOf { PlayerDisplayMode.Hidden }

@Composable
fun localPlayerDisplayMode() = LocalPlayerDisplayMode.current

/**
 * 内容区可视尺寸（已扣除窗口 insets 与 appbar 占位），由 ComposeScaffold 提供。
 *
 * 供需要「不超过一屏」的内容使用：例如视频详情页封面按 16:9 铺满宽度时，
 * 在宽而矮的窗口里算出来的高度会超出可视范围。
 */
internal val LocalContentVisibleSize = staticCompositionLocalOf { DpSize.Zero }

@Composable
fun localContentVisibleSize() = LocalContentVisibleSize.current
