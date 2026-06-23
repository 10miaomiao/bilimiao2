package cn.a10miaomiao.bilimiao.compose.common.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter

/**
 * 应用信息接口
 * 抽象 PackageManager 获取版本信息和应用图标的平台相关操作
 */
interface AppInfo {
    val versionName: String
    val versionCode: Long
    val appId: Int

    @Composable
    fun AppIcon(modifier: Modifier = Modifier)
}
