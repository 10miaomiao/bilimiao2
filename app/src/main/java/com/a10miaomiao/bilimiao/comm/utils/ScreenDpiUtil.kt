package com.a10miaomiao.bilimiao.comm.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import com.a10miaomiao.bilimiao.Bilimiao


object ScreenDpiUtil {

    const val APP_DPI = "app_dpi"
    const val APP_FONT_SCALE = "app_font_scale"

    fun getDefaultDpi(): Int {
        val configuration = Bilimiao.app.resources.configuration
        return configuration.densityDpi
    }

    fun getDefaultFontScale(): Float {
        val configuration = Bilimiao.app.resources.configuration
        return configuration.fontScale
    }

    fun saveCustomConfiguration(dpi: Int, fontScale: Float) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(Bilimiao.app)
        prefs.edit().also {
            it.putInt(APP_DPI, dpi)
            it.putFloat(APP_FONT_SCALE, fontScale)
        }.apply()
    }

    fun readCustomConfiguration(configuration: Configuration): Configuration {
        val prefs = PreferenceManager.getDefaultSharedPreferences(Bilimiao.app)
        val dpi = prefs.getInt(APP_DPI, 0)
        if (dpi > 0) {
            configuration.densityDpi = dpi
        }
        val fontScale = prefs.getFloat(APP_FONT_SCALE, 0f)
        if (fontScale > 0f) {
            configuration.fontScale = fontScale
        }
        return configuration
    }

}