package cn.a10miaomiao.bilimiao.compose.common

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavHostController
import cn.a10miaomiao.bilimiao.compose.common.emitter.SharedFlowEmitter
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation


private fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}

internal val LocalContainerView = staticCompositionLocalOf<ViewGroup?> {
    noLocalProvidedFor("LocalFragment")
}

@Composable
fun localContainerView() = LocalContainerView.current

internal val LocalPageNavigation = staticCompositionLocalOf<PageNavigation> {
    noLocalProvidedFor("PageNavigation")
}

@Composable
fun localPageNavigation() = LocalPageNavigation.current

internal val LocalEmitter = staticCompositionLocalOf<SharedFlowEmitter> {
    noLocalProvidedFor("SharedFlowEmitter")
}

@Composable
fun localEmitter() = LocalEmitter.current
