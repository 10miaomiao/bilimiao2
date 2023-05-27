package com.a10miaomiao.bilimiao.comm.mypage


data class MyPageConfigInfo(
    var title: String = "",
    var menus: List<MenuItemPropInfo>? = null,
    var search: SearchConfigInfo? = null,
)