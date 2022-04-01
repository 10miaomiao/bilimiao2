package com.a10miaomiao.bilimiao.comm.mypage

import androidx.fragment.app.Fragment
import com.a10miaomiao.bilimiao.widget.comm.MenuItemView

fun Fragment.myPageConfig (init: MyPageConfigInfo.() -> Unit): MyPageConfig {
    return MyPageConfig (this) {
        val configInfo = MyPageConfigInfo()
        init.invoke(configInfo)
        configInfo
    }
}


fun myMenuItem (init: MenuItemView.MenuItemPropInfo.() -> Unit): MenuItemView.MenuItemPropInfo {
    return MenuItemView.MenuItemPropInfo().apply(init)
}
