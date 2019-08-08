package com.a10miaomiao.bilimiao

import android.app.Application

class Bilimiao : Application() {

    companion object {
        lateinit var app: Bilimiao
    }

    override fun onCreate() {
        super.onCreate()
        app = this
    }
}