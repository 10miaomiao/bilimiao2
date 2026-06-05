package com.a10miaomiao.bilimiao.comm.mypage

class MyPageMenu {

    var checkable: Boolean = false
    var checkedKey: Int = 0

    private val _items: MutableList<MenuItemPropInfo> = mutableListOf()
    val items get() = _items.toList()

    fun myItem (init: MenuItemPropInfo.() -> Unit): MenuItemPropInfo {
        return MenuItemPropInfo().also {
            _items.add(it)
        }.apply(init)
    }

}