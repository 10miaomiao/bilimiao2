package cn.a10miaomiao.bilimiao.compose.common.platform

import android.os.Build

actual val platformInfo: PlatformInfo = object : PlatformInfo {
    override val model: String = Build.MODEL
    override val osVersion: String = Build.VERSION.RELEASE
    override val sdkInt: Int = Build.VERSION.SDK_INT
    override val platform: Platform = Platform.ANDROID
}
