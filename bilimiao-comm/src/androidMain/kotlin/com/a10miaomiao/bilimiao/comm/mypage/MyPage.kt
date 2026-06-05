package com.a10miaomiao.bilimiao.comm.mypage


interface MyPage {
    val pageConfig: MyPageConfig

    fun onMenuItemClick(menuItem: MenuItemPropInfo) {
    }

    fun onSearchSelfPage(keyword: String) {}
}
