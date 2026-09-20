package com.a10miaomiao.bilimiao.activity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cn.a10miaomiao.bilimiao.compose.BilimiaoTheme
import cn.a10miaomiao.bilimiao.compose.common.preference.rememberPreferenceFlow
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.appDataStore
import com.a10miaomiao.bilimiao.comm.platform.getMaterialYouColor
import com.a10miaomiao.bilimiao.comm.store.AppStore

/**
 * 独立 Activity（不在 MainComposeHost 组合内的页面）的 Compose 主题。
 *
 * 这类页面拿不到主界面的 AppStore 状态，因此直接读取 DataStore 中的主题配置，
 * 复用 [BilimiaoTheme] 生成与主界面一致的配色（主题色、明暗模式）。
 */
@Composable
fun BilimiaoActivityTheme(
    content: @Composable () -> Unit,
) {
    val preferences by rememberPreferenceFlow(appDataStore).collectAsState()
    val themeType = preferences.get<Int>(SettingPreferences.ThemeType.name)
        ?: SettingConstants.THEME_TYPE_DEFAULT
    val themeColor = if (themeType == SettingConstants.THEME_TYPE_DYNAMIC_COLOR) {
        getMaterialYouColor()
    } else {
        (preferences.get<Long>(SettingPreferences.ThemeColor.name) ?: DEFAULT_THEME_COLOR).toInt()
    }
    BilimiaoTheme(
        appState = AppStore.State(
            theme = AppStore.ThemeSettingState(
                color = themeColor,
                type = themeType,
                darkMode = preferences.get<Int>(SettingPreferences.ThemeDarkMode.name) ?: 0,
            )
        ),
        content = content,
    )
}

private const val DEFAULT_THEME_COLOR = 0xFFFB7299L
