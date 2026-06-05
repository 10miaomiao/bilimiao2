package com.a10miaomiao.bilimiao.comm.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.comm.BilimiaoCommCore
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.user.UserInfo
import com.a10miaomiao.bilimiao.comm.miao.MiaoJson
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.platform.PlatformProviders
import com.a10miaomiao.bilimiao.comm.store.base.BaseStore
import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import java.io.File

class UserStore(override val di: DI) :
    ViewModel(), BaseStore<UserStore.State>, UserStateProvider {

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

    override val info: UserInfo? get() = state.info

    private val messageStore: MessageStore by instance()

    override fun init() {
        super.init()
        if (BilimiaoCommCore.instance.loginInfo != null)  {
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
        BilimiaoCommCore.instance.deleteAuth()
        setUserInfo(null)
    }

    private fun seveUserInfo(userInfo: UserInfo?) {
        val file = File(PlatformProviders.context.filesDir, "user.data")
        if (userInfo != null) {
            val jsonStr = MiaoJson.toJson(userInfo)
            file.writeText(jsonStr)
        } else {
            file.delete()
        }
    }

    private fun readUserInfo() {
        try {
            val file = File(PlatformProviders.context.filesDir, "user.data")
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
                .awaitCall()
                .json<ResponseData<UserInfo>>()
            if (res.code == 0 && res.data?.mid != 0L) {
                setState {
                    info = res.data
                }
                seveUserInfo(res.data)
            } else {
                setState {
                    info = null
                }
                GlobalToaster.show("登录失效，请重新登录")
            }
        } catch (e: Exception) {
            GlobalToaster.show("无法连接到御坂网络")
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

    override fun isLogin() = state.info != null

    override fun isVip() = (state.info?.vip_type ?: 0) > 0

}
