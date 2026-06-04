package cn.a10miaomiao.bilimiao.compose

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
fun appColorScheme(
    themeState: AppStore.ThemeSettingState
): ColorScheme {
//    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val themeColor = Color(themeState.color)
    val isDarkTheme = when(themeState.darkMode) {
        0 -> isSystemInDarkTheme()
        1 -> false
        else -> true
    }
//    if (dynamicColor) {
//        return if (isDarkTheme) {
//            dynamicDarkColorScheme(LocalContext.current)
//        } else {
//            dynamicLightColorScheme(LocalContext.current)
//        }
//    }
    val colorScheme = rememberDynamicColorScheme(
        themeColor,
        isDarkTheme,
        isAmoled = true
    )
    return colorScheme
}