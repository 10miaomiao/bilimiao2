package com.a10miaomiao.bilimiao

import android.app.Application


class Bilimiao: Application() {

    companion object {
        const val APP_NAME = "bilimiao"
    }

    override fun onCreate() {
        super.onCreate()
    }
}