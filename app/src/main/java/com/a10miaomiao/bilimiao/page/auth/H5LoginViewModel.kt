package com.a10miaomiao.bilimiao.page.auth

import android.app.ProgressDialog
import android.content.Context
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.bilimiao.compose.comm.navigation.tryPopBackStack
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.BiliJsBridge
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.auth.LoginInfo
import com.a10miaomiao.bilimiao.comm.entity.auth.QRLoginInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserInfo
import com.a10miaomiao.bilimiao.comm.network.ApiHelper
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.page.web.WebFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import kotlin.Exception

class H5LoginViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val fragment: Fragment by instance()
    val ui: MiaoBindingUi by instance()
    val userStore: UserStore by instance()

    private var _authUrl = ""
    private var _authCode = ""
    private val loginSessionId = ApiHelper.getUUID()
    var loading = false

    fun updateLoading(loading: Boolean) {
        ui.setState {
            this.loading = loading
        }
    }

    private fun setBiliAppWebView(webView: WebView) {
        var defaultUserAgentString = webView.settings.userAgentString
        if ("Mobile" !in defaultUserAgentString) {
            defaultUserAgentString += " Mobile"
        }
        val userAgent = defaultUserAgentString + " " + WebFragment.userAgent
        webView.settings.userAgentString = userAgent
        val biliJsBridge = BiliJsBridge(fragment, webView)
        webView.addJavascriptInterface(biliJsBridge, "_BiliJsBridge")
    }

    fun getQrCodeUrl(webView: WebView) = viewModelScope.launch(Dispatchers.IO) {
        try {
            updateLoading(true)
            val res = BiliApiService.authApi
                .qrCode(loginSessionId)
                .awaitCall()
                .gson<ResultInfo<QRLoginInfo>>()
            if (res.isSuccess) {
                _authUrl = res.data.url
                _authCode = res.data.auth_code
                delay((200 * Math.random()).toLong() + 200L)
                if (!confirmQRCode(_authCode)) {
                    withContext(Dispatchers.Main) {
                        setBiliAppWebView(webView)
                        webView.loadUrl(_authUrl)
                        alert("为获取BiliAPP登录凭证(token)，需模拟一次扫码登录，请在接下来的页面中点击确认登录。( *・ω・)")
                    }
                }
                launch(Dispatchers.Main) { checkQRCode(_authCode) }
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

    suspend fun confirmQRCode(authCode: String): Boolean {
        try {
            val res = BiliApiService.authApi
                .confirmQRCode(authCode)
                .awaitCall()
                .gson<MessageInfo>()
            return res.isSuccess
        } catch (e: Exception) {
            e.printStackTrace()
            return false
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
                withContext(Dispatchers.Main) {
                    userStore.setUserInfo(res.data)
                    fragment.findNavController()
                        .popBackStack(MainNavGraph.dest.main, false)
                }
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

    private fun alert(message: String) {
        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle("提示")
        builder.setMessage(message)
        builder.setNegativeButton("确定", null)
        builder.show()
    }
}