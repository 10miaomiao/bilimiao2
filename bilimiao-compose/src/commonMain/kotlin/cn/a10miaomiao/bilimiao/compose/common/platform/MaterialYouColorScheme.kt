package cn.a10miaomiao.bilimiao.compose.common.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * Material You 动态配色（跟随系统主题色取色）。
 *
 * Android 12+ 使用系统原生动态配色，其余平台回退 Material 3 默认配色。
 * 平台是否支持可通过 [com.a10miaomiao.bilimiao.comm.platform.isMaterialYouSupported] 判断。
 */
@Composable
expect fun rememberMaterialYouColorScheme(isDarkTheme: Boolean): ColorScheme
