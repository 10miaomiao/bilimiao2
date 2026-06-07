package cn.a10miaomiao.bilimiao.compose.common.platform

expect val platformInfo: PlatformInfo

interface PlatformInfo {
    val model: String
    val osVersion: String
    val sdkInt: Int
}
