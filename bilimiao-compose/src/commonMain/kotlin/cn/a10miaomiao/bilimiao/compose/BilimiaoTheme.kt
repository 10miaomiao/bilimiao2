package cn.a10miaomiao.bilimiao.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import cn.a10miaomiao.bilimiao.compose.common.platform.rememberMaterialYouColorScheme
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.store.AppStore
import com.materialkolor.rememberDynamicColorScheme

/** 当前应用是否处于深色主题（由 [BilimiaoTheme] 依据 darkMode 设置与系统主题提供） */
val LocalAppDarkTheme = staticCompositionLocalOf { false }

@Composable
fun BilimiaoTheme(
    appState: AppStore.State,
    content: @Composable () -> Unit
) {
    val themeState = appState.theme ?: return
    val isDarkTheme = isAppDarkTheme(themeState)
    CompositionLocalProvider(LocalAppDarkTheme provides isDarkTheme) {
        MaterialTheme(
            colorScheme = appColorScheme(themeState),
            content = content,
        )
    }
}

@Composable
fun isAppDarkTheme(themeState: AppStore.ThemeSettingState): Boolean {
    return when (themeState.darkMode) {
        0 -> isSystemInDarkTheme()
        1 -> false
        else -> true
    }
}

@Composable
fun appColorScheme(
    themeState: AppStore.ThemeSettingState
): ColorScheme {
    val isDarkTheme = isAppDarkTheme(themeState)
    if (themeState.type == SettingConstants.THEME_TYPE_DYNAMIC_COLOR) {
        // Material You：直接使用系统动态配色
        val colorScheme = rememberMaterialYouColorScheme(isDarkTheme)
        // 深色模式沿用 AMOLED 纯黑背景，与下面的 isAmoled 保持一致
        return if (isDarkTheme) {
            colorScheme.copy(
                background = Color.Black,
                onBackground = Color.White,
                surface = Color.Black,
                onSurface = Color.White,
            )
        } else {
            colorScheme
        }
    }
    val themeColor = Color(themeState.color)
    val colorScheme = rememberDynamicColorScheme(
        themeColor,
        isDarkTheme,
        isAmoled = true
    )
    return colorScheme
}
