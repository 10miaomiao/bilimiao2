package com.a10miaomiao.bilimiao.comm.store

import com.a10miaomiao.bilimiao.comm.entity.user.UserInfo

interface UserStateProvider {
    val info: UserInfo?
    fun isLogin(): Boolean
    fun isVip(): Boolean
}
