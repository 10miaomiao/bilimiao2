package com.a10miaomiao.bilimiao.comm.store

import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.appDataStore
import com.a10miaomiao.bilimiao.comm.platform.getMaterialYouColor
import com.a10miaomiao.bilimiao.comm.platform.setDarkMode
import com.a10miaomiao.bilimiao.comm.store.base.BaseStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.kodein.di.DI

class AppStore(override val di: DI) :
    ViewModel(), BaseStore<AppStore.State> {

    data class ThemeSettingState (
        val color: Int,
        val type: Int = SettingConstants.THEME_TYPE_DEFAULT,
        val darkMode: Int = 0,
        val appBarType: Int = 0,
    )

    data class HomeSettingState (
        val showPopular: Boolean = true,
        val showRecommend: Boolean = true,
        val entryView: Int = SettingConstants.HOME_ENTRY_VIEW_DEFAULT,
    )

    data class State (
        var theme: ThemeSettingState? = null,
        var home: HomeSettingState = HomeSettingState(),
        var isLockScreenOrientationPortrait: Boolean = false,
    )

    override val stateFlow = MutableStateFlow(State())
    override fun copyState() = state.copy()

    override fun init() {
        super.init()
        viewModelScope.launch {
            val prefs = appDataStore.data.first()
            val themeType = prefs[SettingPreferences.ThemeType] ?: SettingConstants.THEME_TYPE_DEFAULT
            val themeColor = if (themeType == SettingConstants.THEME_TYPE_DYNAMIC_COLOR) {
                getMaterialYouColor()
            } else {
                (prefs[SettingPreferences.ThemeColor] ?: 0xFFFB7299).toInt()
            }
            setState {
                home = HomeSettingState(
                    showPopular = prefs[SettingPreferences.HomePopularShow] ?: true,
                    showRecommend = prefs[SettingPreferences.HomeRecommendShow] ?: true,
                    entryView = prefs[SettingPreferences.HomeEntryView] ?: SettingConstants.HOME_ENTRY_VIEW_DEFAULT
                )
                theme = ThemeSettingState(
                    color = themeColor,
                    type = themeType,
                    darkMode = prefs[SettingPreferences.ThemeDarkMode] ?: 0,
                    appBarType = prefs[SettingPreferences.ThemeAppBarType] ?: 0,
                )
                isLockScreenOrientationPortrait = prefs[SettingPreferences.IsLockScreenOrientationPortrait] ?: false
            }
        }
    }

    fun setDarkMode(mode: Int) {
        setState { theme = (theme ?: ThemeSettingState(color = 0xFFFB7299.toInt())).copy(darkMode = mode) }
        viewModelScope.launch {
            appDataStore.edit {
                it[SettingPreferences.ThemeDarkMode] = mode
            }
        }
        setDarkMode(mode)
    }
    fun setThemeColor(color: Long, type: Int) {
        setState { theme = (theme ?: ThemeSettingState(color = color.toInt())).copy(color = color.toInt(), type = type) }
        viewModelScope.launch {
            appDataStore.edit {
                it[SettingPreferences.ThemeColor] = color
                it[SettingPreferences.ThemeType] = type
            }
        }
    }

    fun setAppBarType(type: Int) {
        setState { theme = (theme ?: ThemeSettingState(color = 0xFFFB7299.toInt())).copy(appBarType = type) }
        viewModelScope.launch {
            appDataStore.edit {
                it[SettingPreferences.ThemeAppBarType] = type
            }
        }
    }

}
