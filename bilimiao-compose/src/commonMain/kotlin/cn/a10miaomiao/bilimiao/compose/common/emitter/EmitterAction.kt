package cn.a10miaomiao.bilimiao.compose.common.emitter

sealed class EmitterAction {
    data class DoubleClickTab(
        val tab: String,
    ): EmitterAction()
}