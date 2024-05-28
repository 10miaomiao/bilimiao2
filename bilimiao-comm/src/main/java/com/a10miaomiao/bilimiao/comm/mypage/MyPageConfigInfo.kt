package com.a10miaomiao.bilimiao.comm.mypage


data class MyPageConfigInfo(
    var title: String = "",
    @Deprecated("请使用menu")
    var menus: List<MenuItemPropInfo>? = null,
    var search: SearchConfigInfo? = null,
    var menu: MyPageMenu? = null,
) {
    fun getMenuItems(): List<MenuItemPropInfo> {
        return menus ?: menu?.items ?: listOf()
    }
}