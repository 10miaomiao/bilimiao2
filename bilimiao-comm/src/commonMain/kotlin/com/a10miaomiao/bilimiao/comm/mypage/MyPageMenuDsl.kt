package com.a10miaomiao.bilimiao.comm.mypage

inline fun myMenu(init: MyPageMenu.() -> Unit): MyPageMenu {
    return MyPageMenu().apply(init)
}
