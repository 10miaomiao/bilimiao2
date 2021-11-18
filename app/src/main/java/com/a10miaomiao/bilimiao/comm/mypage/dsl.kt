package com.a10miaomiao.bilimiao.comm.mypage

import androidx.fragment.app.Fragment

fun Fragment.myPageConfig (init: MyPageConfigInfo.() -> Unit): MyPageConfig {
    return MyPageConfig (lifecycle) {
        val configInfo = MyPageConfigInfo()
        init.invoke(configInfo)
        configInfo
    }
}
