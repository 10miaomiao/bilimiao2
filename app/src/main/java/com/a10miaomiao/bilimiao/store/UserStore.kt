package com.a10miaomiao.bilimiao.store

import android.content.Context
import com.a10miaomiao.bilimiao.entity.UserInfo
import com.a10miaomiao.miaoandriod.MiaoLiveData
import com.a10miaomiao.miaoandriod.MiaoObserver
import com.google.gson.Gson
import java.io.File

class UserStore(
        val context: Context
) {

    private val _user = MiaoLiveData<UserInfo?>(null)
    val user get() = _user.value

    init {
        val userInfo = readUserInfo()
        if (userInfo != null)
            setUserInfo(userInfo)
    }

    val observer = _user.observe()
    val observeNotNull = _user.observeNotNull() as MiaoObserver<UserInfo>

    fun setUserInfo(userInfo: UserInfo?) {
        _user set userInfo
        seveUserInfo(userInfo)
    }

    private fun seveUserInfo(userInfo: UserInfo?) {
        val file = File(context.filesDir.path + "/user.data")
        if (userInfo != null) {
            val jsonStr = Gson().toJson(userInfo)
            file.writeText(jsonStr)
        } else {
            file.delete()
        }
    }

    private fun readUserInfo(): UserInfo? {
        return try {
            val file = File(context.filesDir.path + "/user.data")
            val jsonStr = file.readText()
            Gson().fromJson(jsonStr, UserInfo::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun isSelf(mid: Long) = user != null  && user!!.mid == mid

}