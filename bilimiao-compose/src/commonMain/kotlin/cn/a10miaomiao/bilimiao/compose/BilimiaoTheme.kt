package cn.a10miaomiao.bilimiao.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.a10miaomiao.bilimiao.comm.store.AppStore
import com.materialkolor.rememberDynamicColorScheme

@Composable
fun BilimiaoTheme(
    appState: AppStore.State,
    content: @Composable () -> Unit
) {
    val themeState = appState.theme ?: return
    MaterialTheme(
        colorScheme = appColorScheme(themeState),
        content = content,
    )
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
    val themeColor = Color(themeState.color)
    val isDarkTheme = isAppDarkTheme(themeState)
    val colorScheme = rememberDynamicColorScheme(
        themeColor,
        isDarkTheme,
        isAmoled = true
    )
    return colorScheme
}
