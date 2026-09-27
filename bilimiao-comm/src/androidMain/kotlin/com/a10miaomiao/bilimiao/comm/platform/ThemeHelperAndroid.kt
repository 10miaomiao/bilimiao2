package com.a10miaomiao.bilimiao.comm.platform

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat

/** 应用默认主题色（少女粉） */
private val DEFAULT_THEME_COLOR = 0xFFFB7299.toInt()

actual fun setDarkMode(mode: Int) {
    when (mode) {
        0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}

actual val isMaterialYouSupported: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

actual fun getMaterialYouColor(): Int {
    // 系统主色资源是 Android 12 新增的，低版本取不到，回退默认主题色
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return DEFAULT_THEME_COLOR
    }
    val context = PlatformProviders.context.platformContext as Context
    return ContextCompat.getColor(context, android.R.color.system_primary_light)
}
