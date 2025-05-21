package com.a10miaomiao.bilimiao.comm.store

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserInfo
import com.a10miaomiao.bilimiao.comm.miao.MiaoJson
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.store.base.BaseStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import org.kodein.di.DI
import org.kodein.di.instance
import java.io.File

class UserStore(override val di: DI) :
    ViewModel(), BaseStore<UserStore.State> {

    data class State (
        var info: UserInfo? = null
    ) {
        fun isSelf(mid: String) = info?.mid == mid.toLong()

        fun isSelf(mid: Long) = info?.mid == mid

        fun isLogin() = info != null

        fun isVip() = (info?.vip_type ?: 0) > 0
    }

    override val stateFlow = MutableStateFlow(State())
    override fun copyState() = state.copy()

    private val activity: AppCompatActivity by instance()

    private val messageStore: MessageStore by instance()

    override fun init(context: Context) {
        super.init(context)
        if (BilimiaoCommApp.commApp.loginInfo != null)  {
            readUserInfo()
            loadInfo()
            messageStore.getUnreadMessage()
        }
    }

    fun setUserInfo(userInfo: UserInfo?) {
        setState {
            info = userInfo
        }
        seveUserInfo(userInfo)
        if (userInfo != null) {
            messageStore.getUnreadMessage()
        }
    }

    fun logout () {
        BilimiaoCommApp.commApp.deleteAuth()
        setUserInfo(null)
    }

    private fun seveUserInfo(userInfo: UserInfo?) {
        val file = File(activity.filesDir.path + "/user.data")
        if (userInfo != null) {
            val jsonStr = MiaoJson.toJson(userInfo)
            file.writeText(jsonStr)
        } else {
            file.delete()
        }
    }

    private fun readUserInfo() {
        try {
            val file = File(activity.filesDir.path + "/user.data")
            if (file.exists()) {
                val jsonStr = file.readText()
                val localInfo = MiaoJson.fromJson<UserInfo>(jsonStr)
                setState {
                    info = localInfo
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun loadInfo() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val res = BiliApiService.authApi
                .account()
                .call()
                .json<ResultInfo<UserInfo>>()
            if (res.code == 0) {
                setState {
                    info = res.data
                }
                seveUserInfo(res.data)
            } else {
                withContext(Dispatchers.Main) {
                    PopTip.show("登录失效，请重新登录")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                PopTip.show("无法连接到御坂网络")
            }
            e.printStackTrace()
        }
    }

    fun sso() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val res = BiliApiService.authApi
                .sso()
                .awaitCall()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun isSelf(mid: String) = state.info?.mid == mid.toLong()

    fun isLogin() = state.info != null

    fun isVip() = (state.info?.vip_type ?: 0) > 0

}