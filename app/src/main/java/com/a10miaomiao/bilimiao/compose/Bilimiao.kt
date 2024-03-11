package com.a10miaomiao.bilimiao.compose

import android.app.Application
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp


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
        commApp.onCreate()
    }
}