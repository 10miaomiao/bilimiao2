package com.a10miaomiao.bilimiao.page.auth

import android.app.ProgressDialog
import android.content.Context
import android.view.View
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.a10miaomiao.bilimiao.Bilimiao
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.apis.AuthApi
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.auth.LoginInfo
import com.a10miaomiao.bilimiao.comm.entity.auth.QRLoginInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    val fragment: Fragment by instance()
    val ui: MiaoBindingUi by instance()
    val userStore: UserStore by instance()

    private var _authUrl = ""
    private var _authCode = ""
    var loading = false

    fun updateLoading(loading: Boolean) {
        ui.setState {
            this.loading = loading
        }
    }

    fun getQrCodeUrl(webView: WebView) = viewModelScope.launch(Dispatchers.IO) {
        try {
            updateLoading(true)
            val res = BiliApiService.authApi
                .qrCode()
                .awaitCall()
                .gson<ResultInfo<QRLoginInfo>>(isDebug = true)
            if (res.isSuccess) {
                _authUrl = res.data.url
                _authCode = res.data.auth_code
                launch(Dispatchers.Main) { checkQRCode(res.data.auth_code) }
                withContext(Dispatchers.Main) {
                    webView.loadUrl(res.data.url)
                }
            } else {
                withContext(Dispatchers.Main) {
                    alert(res.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                alert("发生错误，请稍后重试：" + e.message ?: e.toString())
            }
        } finally {
            updateLoading(false)
        }
    }

    suspend fun checkQRCode(
        authCode: String = _authCode,
        isPolling: Boolean = true,
    ) {
        try {
            val res = withContext(Dispatchers.IO) {
                BiliApiService.authApi
                    .checkQrCode(authCode)
                    .awaitCall()
                    .gson<ResultInfo<LoginInfo.QrLoginInfo>>()
            }
            when (res.code) {
                86039, 86090 -> {
                    delay(3000)
                    if (_authCode == authCode && isPolling) {
                        checkQRCode(authCode)
                    }
                }
                86038, -3 -> {
                    // 过期、失效
//                    error.value = "二维码已过期，请刷新"
//                    alert("登录失败，请稍后重试")
                }
                0 -> {
                    // 成功
                    val loginInfo = res.data.toLoginInfo()
                    BilimiaoCommApp.commApp.saveAuthInfo(loginInfo)
                    authInfo()
                }
                else -> {
                    // 发生错误
                    alert("登录失败，请稍后重试：" + res.message)
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException){
        } catch (e: Exception) {
            e.printStackTrace()
            alert("登录失败，请稍后重试：" + e.message ?: e.toString())
        }
    }


    private suspend fun authInfo() {
        val progressDialog = ProgressDialog(context).apply {
            isIndeterminate = true
            setTitle("获取信息中")
            show()
        }
        try {
            val res = withContext(Dispatchers.IO) {
                BiliApiService.authApi
                    .account()
                    .awaitCall()
                    .gson<ResultInfo<UserInfo>>()
            }
            if (res.isSuccess) {
                userStore.setUserInfo(res.data)
                fragment.findNavController().popBackStack(MainNavGraph.dest.main, false)
            } else {
                alert("获取用户信息失败，请稍后重试：" + res.message)
                throw Exception(res.message)
            }
        } catch (e: kotlinx.coroutines.CancellationException){
        } catch (e: Exception) {
            alert("获取用户信息失败，请稍后重试：" + e.message ?: e.toString())
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