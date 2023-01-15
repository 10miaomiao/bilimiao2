package com.a10miaomiao.bilimiao.page.auth

import android.app.ProgressDialog
import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigation.findNavController
import com.a10miaomiao.bilimiao.Bilimiao
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.apis.AuthApi
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.auth.LoginInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserInfo
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.comm.store.UserStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.lang.Exception

class H5LoginViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val userStore: UserStore by instance()

    var loading = false

    fun updateLoading(loading: Boolean) {
        ui.setState {
            this.loading = loading
        }
    }

    fun resolveUrl(view: View, url: String) {
        try {
            updateLoading(true)
            val accessKey = "access_key=(.*?)&".toRegex().find(url)!!.groupValues[1]
            val mid = "mid=(.*?)&".toRegex().find(url)!!.groupValues[1]
            val loginInfo = LoginInfo(
                token_info = LoginInfo.TokenInfo(
                    access_token = accessKey,
                    mid = mid.toLong(),
                    expires_in = 7200,
                    refresh_token = ""
                ),
                status = 0,
                message =  null,
                url = null,
                sso = null,
                cookie_info = null
            )
            BilimiaoCommApp.commApp.saveAuthInfo(loginInfo)
            authInfo(view)
        } catch (e: Exception) {
            DebugMiao.log("获取登录参数时，发生错误")
            e.printStackTrace()

        } finally {
            updateLoading(false)
        }
    }

    fun authInfo(view: View) = viewModelScope.launch(Dispatchers.Main) {
        val progressDialog = ProgressDialog(context).apply {
            isIndeterminate = true
            setTitle("获取信息中")
            show()
        }
        try {
            val res = withContext(Dispatchers.IO) {
                AuthApi().account().call().gson<ResultInfo<UserInfo>>()
            }
            if (res.code == 0) {
                withContext(Dispatchers.Main) {
                    userStore.setUserInfo(res.data)
                    val nav = findNavController(view)
                    nav.popBackStack(MainNavGraph.dest.home, true)
                }
            } else {
                alert(res.message)
            }
        } catch (e: Exception) {
            alert("网络错误")
        } finally {
            progressDialog.hide()
        }
    }

    private fun alert(title: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setNegativeButton("确定", null)
        builder.show()
    }
}