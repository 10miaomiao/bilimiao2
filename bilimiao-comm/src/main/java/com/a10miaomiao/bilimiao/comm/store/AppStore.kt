package com.a10miaomiao.bilimiao.comm.store

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences.dataStore
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.message.UnreadMessageInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.store.base.BaseStore
import com.kongzue.dialogx.DialogX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

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

    private val context: Context by instance()

    override fun init(context: Context) {
        super.init(context)
        SettingPreferences.launch(viewModelScope) {
            context.dataStore.data.collect {
                val themeType = it[ThemeType] ?: SettingConstants.THEME_TYPE_DEFAULT
                val themeColor = if (themeType == SettingConstants.THEME_TYPE_DYNAMIC_COLOR) {
                    materialYouColor
                } else {
                    (it[ThemeColor] ?: 0xFFFB7299).toInt()
                }
                setState {
                    home = HomeSettingState(
                        showPopular = it[HomePopularShow] ?: true,
                        showRecommend = it[HomeRecommendShow] ?: true,
                        entryView = it[HomeEntryView] ?: SettingConstants.HOME_ENTRY_VIEW_DEFAULT
                    )
                    theme = ThemeSettingState(
                        color = themeColor,
                        type = themeType,
                        darkMode = it[ThemeDarkMode] ?: 0,
                        appBarType = it[ThemeAppBarType] ?: 0,
                    )
                    isLockScreenOrientationPortrait = it[IsLockScreenOrientationPortrait] ?: false
                }
            }
        }
    }

    val materialYouColor get() = ContextCompat.getColor(
        context,
        android.R.color.system_primary_light
    )

    fun setDarkMode(mode: Int) {
        viewModelScope.launch {
            SettingPreferences.edit(context) {
                it[ThemeDarkMode] = mode
            }
        }
        if (mode == 0) {
            DialogX.globalTheme = DialogX.THEME.AUTO
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        } else if (mode == 1) {
            DialogX.globalTheme = DialogX.THEME.LIGHT
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else if (mode == 2) {
            DialogX.globalTheme = DialogX.THEME.DARK
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    fun setThemeColor(color: Long, type: Int) {
        viewModelScope.launch {
            SettingPreferences.edit(context) {
                it[ThemeColor] = color
                it[ThemeType] = type
            }
        }
    }

    fun setAppBarType(type: Int) {
        viewModelScope.launch {
            SettingPreferences.edit(context) {
                it[ThemeAppBarType] = type
            }
        }
    }


}