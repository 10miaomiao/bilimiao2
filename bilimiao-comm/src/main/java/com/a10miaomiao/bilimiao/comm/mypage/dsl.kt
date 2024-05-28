package com.a10miaomiao.bilimiao.comm.mypage

import androidx.fragment.app.Fragment

fun Fragment.myPageConfig (init: MyPageConfigInfo.() -> Unit): MyPageConfig {
    return MyPageConfig (this) {
        val configInfo = MyPageConfigInfo()
        init.invoke(configInfo)
        configInfo
    }
}


fun myMenuItem (init: MenuItemPropInfo.() -> Unit): MenuItemPropInfo {
    return MenuItemPropInfo().apply(init)
}

fun myMenu (init: MyPageMenu.() -> Unit): MyPageMenu {
    return MyPageMenu().apply(init)
}
