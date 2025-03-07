package com.a10miaomiao.bilimiao.comm.delegate.theme

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.Bilimiao
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences.dataStore
import com.a10miaomiao.bilimiao.comm.store.AppStore.HomeSettingState
import com.a10miaomiao.bilimiao.comm.store.AppStore.ThemeSettingState
import com.a10miaomiao.bilimiao.config.config
import com.google.android.material.color.DynamicColors
import com.kongzue.dialogx.DialogX
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.DIAware


class ThemeDelegate(
    private var activity: AppCompatActivity,
    override val di: DI,
) : DIAware {

    companion object {
        var defaultThemeColor = 0xFFFB7299
        fun getNightMode(context: Context): Int {
            return runBlocking {
                SettingPreferences.mapData(context) {
                    it[ThemeColor]?.let { c -> defaultThemeColor = c }
                    it[ThemeDarkMode] ?: 0
                }
            }
        }
    }

    private val _themeColor = MutableLiveData<Long>()
    val themeColor get() = _themeColor.value ?: defaultThemeColor

    fun onCreate(savedInstanceState: Bundle?) {
        collectData()
    }

    private fun collectData() {
        SettingPreferences.launch(activity.lifecycleScope) {
            activity.dataStore.data.collect {
                it[ThemeColor]?.let { c -> defaultThemeColor = c }
                _themeColor.value = it[ThemeColor] ?: 0xFFFB7299
            }
        }
    }

    fun observeTheme(owner: LifecycleOwner, observer: Observer<Long>) = _themeColor.observe(owner, observer)

    fun isSystemInDark(): Boolean {
        val uiMode = activity.resources.configuration.uiMode
        return (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

}