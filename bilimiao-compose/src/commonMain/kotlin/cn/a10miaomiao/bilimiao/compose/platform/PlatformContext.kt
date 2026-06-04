package cn.a10miaomiao.bilimiao.compose.platform

import androidx.compose.runtime.staticCompositionLocalOf

interface PlatformContext {
    fun openUrl(url: String)
    fun copyToClipboard(text: String)
    fun shareText(text: String)
}

val LocalPlatformContext = staticCompositionLocalOf<PlatformContext> {
    error("PlatformContext not provided")
}
