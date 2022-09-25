package com.a10miaomiao.bilimiao.comm.mypage

import android.view.View


interface MyPage {
    val pageConfig: MyPageConfig

    fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {
    }
}