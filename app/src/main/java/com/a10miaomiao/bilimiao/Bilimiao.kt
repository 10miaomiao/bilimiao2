package com.a10miaomiao.bilimiao

import android.app.Application
import com.a10miaomiao.miaoandriod.MiaoAndroid

class Bilimiao : Application() {
    override fun onCreate() {
        super.onCreate()
        MiaoAndroid.init()
    }
}