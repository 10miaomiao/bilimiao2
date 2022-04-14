package com.a10miaomiao.bilimiao

import android.app.Application
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate

class Bilimiao: Application() {

    companion object {
        const val APP_NAME = "bilimiao"
        lateinit var commApp: BilimiaoCommApp
    }

    init {
        commApp = BilimiaoCommApp(this)
    }

    override fun onCreate() {
        super.onCreate()
        ThemeDelegate.setNightMode(this)
        commApp.onCreate()
    }
}