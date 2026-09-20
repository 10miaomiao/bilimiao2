package cn.a10miaomiao.bilimiao.compose.common.platform

/**
 * 应用内 DPI（显示缩放）设置页入口。
 *
 * 该设置页是 Android 端专属的 Activity，因此由各平台入口点决定是否提供实现：
 * Android 端在 MainActivity 中绑定实现，桌面端不绑定（设置页不展示该入口）。
 */
interface DensitySettingLauncher {
    fun openDensitySetting()
}
