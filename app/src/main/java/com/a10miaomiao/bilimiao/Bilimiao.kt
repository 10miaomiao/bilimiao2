package com.a10miaomiao.bilimiao

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import com.kongzue.dialogx.DialogX
import net.mikaelzero.mojito.Mojito
import net.mikaelzero.mojito.loader.glide.GlideImageLoader
import net.mikaelzero.mojito.view.sketch.SketchImageLoadFactory


class Bilimiao: Application() {

    companion object {
        const val APP_NAME = "bilimiao"
        lateinit var app: Bilimiao
        lateinit var commApp: BilimiaoCommApp
    }

    init {
        app = this
        commApp = BilimiaoCommApp(this)
    }

    override fun onCreate() {
        super.onCreate()
        AppCrashHandler.getInstance(this)
        setDefaultNightMode()
        Mojito.initialize(
            GlideImageLoader.with(this),
            SketchImageLoadFactory()
        )
        commApp.onCreate()
    }

    private fun setDefaultNightMode() {
        val mode = ThemeDelegate.getNightMode(this)
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
}