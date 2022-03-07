package com.a10miaomiao.bilimiao.comm.mypage

import com.a10miaomiao.bilimiao.widget.comm.MenuItemView


interface MyPage {
    val pageConfig: MyPageConfig

    fun onMenuItemClick(view: MenuItemView) {
    }
}