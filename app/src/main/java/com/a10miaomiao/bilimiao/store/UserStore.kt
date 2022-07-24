package com.a10miaomiao.bilimiao.store

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.Bilimiao
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.store.base.BaseStore
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.instance
import splitties.toast.toast
import java.io.File

class UserStore(override val di: DI) :
    ViewModel(), BaseStore<UserStore.State> {

    data class State (
        var info: UserInfo? = null
    )

    override val stateFlow = MutableStateFlow(State())
    override fun copyState() = state.copy()

    private val activity: AppCompatActivity by instance()

    override fun init(context: Context) {
        super.init(context)
        if (BilimiaoCommApp.commApp.loginInfo != null)  {
            readUserInfo()
            loadInfo()
        }
    }

    fun setUserInfo(userInfo: UserInfo?) {
        setState {
            info = userInfo
        }
        seveUserInfo(userInfo)
    }

    fun logout () {
        Bilimiao.commApp.deleteAuth()
        setUserInfo(null)
    }

    private fun seveUserInfo(userInfo: UserInfo?) {
        val file = File(activity.filesDir.path + "/user.data")
        if (userInfo != null) {
            val jsonStr = Gson().toJson(userInfo)
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
                val localInfo = Gson().fromJson(jsonStr, UserInfo::class.java)
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
                .gson<ResultInfo<UserInfo>>()
            if (res.code == 0) {
                setState {
                    info = res.data
                }
                seveUserInfo(res.data)
            } else {
                withContext(Dispatchers.Main) {
                    activity.toast("登录失效，请重新登录")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                activity.toast("无法连接到御坂网络")
            }
            e.printStackTrace()
        }
    }

    fun isSelf(mid: String) = state.info?.mid == mid.toLong()

    fun isLogin() = state.info != null

}