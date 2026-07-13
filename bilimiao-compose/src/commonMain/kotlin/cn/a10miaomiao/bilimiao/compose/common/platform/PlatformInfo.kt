package cn.a10miaomiao.bilimiao.compose.common.platform

expect val platformInfo: PlatformInfo

enum class Platform { ANDROID, DESKTOP, IOS, WEB }

interface PlatformInfo {
    val model: String
    val osVersion: String
    val sdkInt: Int
    val platform: Platform
}
