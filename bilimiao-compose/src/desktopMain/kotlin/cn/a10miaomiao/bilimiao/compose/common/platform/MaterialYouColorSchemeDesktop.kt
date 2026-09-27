package cn.a10miaomiao.bilimiao.compose.common.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun rememberMaterialYouColorScheme(isDarkTheme: Boolean): ColorScheme {
    // Desktop: 没有系统动态配色，回退 Material 3 默认配色（主题设置中已隐藏 Material You 选项）
    return if (isDarkTheme) darkColorScheme() else lightColorScheme()
}
