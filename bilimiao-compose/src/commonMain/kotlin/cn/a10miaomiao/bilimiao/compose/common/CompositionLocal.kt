package cn.a10miaomiao.bilimiao.compose.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import cn.a10miaomiao.bilimiao.compose.PlayerState
import cn.a10miaomiao.bilimiao.compose.common.emitter.SharedFlowEmitter
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigator


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
