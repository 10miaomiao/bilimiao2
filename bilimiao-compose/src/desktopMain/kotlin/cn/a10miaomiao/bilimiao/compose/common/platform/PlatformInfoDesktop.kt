package cn.a10miaomiao.bilimiao.compose.common.platform

actual val platformInfo: PlatformInfo = object : PlatformInfo {
    override val model: String = System.getProperty("os.name") ?: "Desktop"
    override val osVersion: String = System.getProperty("os.version") ?: "0"
    override val sdkInt: Int = 0
    override val platform: Platform = Platform.DESKTOP
}
