package com.a10miaomiao.bilimiao.store

import android.arch.lifecycle.ViewModel
import android.content.Context
import com.a10miaomiao.bilimiao.Bilimiao
import com.a10miaomiao.bilimiao.entity.UserInfo
import com.a10miaomiao.bilimiao.netword.ApiHelper
import com.a10miaomiao.bilimiao.netword.LoginHelper
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.miaoandriod.MiaoLiveData
import com.a10miaomiao.miaoandriod.MiaoObserver
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.toast
import java.io.File

class UserStore(
        val context: Context
) : ViewModel() {

    private val _user = MiaoLiveData<UserInfo?>(null)
    val user get() = _user.value

    init {
        val userInfo = readUserInfo()
        if (userInfo != null)
            setUserInfo(userInfo)
    }

    val observer = _user.observe()
    val observeNotNull = _user.observeNotNull() as MiaoObserver<UserInfo>

    private var loadInfoDisposable: Disposable? = null

    fun setUserInfo(userInfo: UserInfo?) {
        _user set userInfo
        seveUserInfo(userInfo)
        loadInfo()
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

    private fun loadInfo() {
        loadInfoDisposable = Bilimiao.app.loginInfo?.let { loginInfo ->
            val accessToken = loginInfo.token_info.access_token
            LoginHelper.authInfo(accessToken)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }?.subscribe({
            if (it.code == 0) {
                _user set it.data
                seveUserInfo(it.data)
            } else {
                context.toast("登录失效，请重新登录")
            }
        }, {
            context.toast("无法连接到御坂网络")
            it.printStackTrace()
        })
    }

    fun isSelf(mid: Long) = user != null && user!!.mid == mid

    override fun onCleared() {
        super.onCleared()
        loadInfoDisposable?.dispose()
    }

}