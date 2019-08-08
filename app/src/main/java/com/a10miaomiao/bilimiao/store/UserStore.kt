package com.a10miaomiao.bilimiao.store

import android.content.Context
import com.a10miaomiao.bilimiao.entity.UserInfo
import com.a10miaomiao.bilimiao.netword.LoginHelper
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.miaoandriod.MiaoLiveData
import com.a10miaomiao.miaoandriod.MiaoObserver

class UserStore(
        val context: Context
) {

    private val _user = MiaoLiveData<UserInfo?>(null)
    val user get() = _user.value

    init {
        val userInfo = LoginHelper.readUserInfo(context)
        if (userInfo != null)
            setUserInfo(userInfo)
    }

    val observer = _user.observe()
    val observeNotNull = _user.observeNotNull() as MiaoObserver<UserInfo>

    fun setUserInfo(userInfo: UserInfo?){
        _user set userInfo
    }

}