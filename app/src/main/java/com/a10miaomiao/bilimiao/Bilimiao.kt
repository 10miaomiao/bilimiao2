package com.a10miaomiao.bilimiao

import android.app.Application
import com.a10miaomiao.miaoandriod.MiaoAndroid

class Bilimiao : Application() {

    companion object {
        lateinit var app: Bilimiao
    }

    override fun onCreate() {
        super.onCreate()
        MiaoAndroid.init()
    }
}