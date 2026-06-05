package com.a10miaomiao.bilimiao.comm.platform

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat

actual fun setDarkMode(mode: Int) {
    when (mode) {
        0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}

actual fun getMaterialYouColor(): Int {
    val context = PlatformProviders.context.platformContext as Context
    return ContextCompat.getColor(context, android.R.color.system_primary_light)
}
