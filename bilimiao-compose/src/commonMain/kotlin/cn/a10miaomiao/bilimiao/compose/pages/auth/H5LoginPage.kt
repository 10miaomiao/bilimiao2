package cn.a10miaomiao.bilimiao.compose.pages.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.BiliJsBridge
import cn.a10miaomiao.bilimiao.compose.common.addPaddingValues
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContentInsets
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.BilibiliNavigation
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.platform.platformInfo
import cn.a10miaomiao.bilimiao.compose.components.dialogs.MessageDialogState
import cn.a10miaomiao.bilimiao.compose.common.webview.WebViewCallbacks
import cn.a10miaomiao.bilimiao.compose.common.webview.WebViewConfig
import cn.a10miaomiao.bilimiao.compose.common.webview.WebViewContainer
import cn.a10miaomiao.bilimiao.compose.common.webview.WebViewHandle
import com.a10miaomiao.bilimiao.comm.BilimiaoCommCore
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.auth.LoginInfo
import com.a10miaomiao.bilimiao.comm.entity.auth.QRLoginInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserInfo
import com.a10miaomiao.bilimiao.comm.network.ApiHelper
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance


@Serializable
class H5LoginPage(
    val url: String = "https://passport.bilibili.com/h5-app/passport/login",
) : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: H5LoginPageViewModel = diViewModel {
            H5LoginPageViewModel(it, url)
        }
        H5LoginPageContent(viewModel)
    }
}

private class H5LoginPageViewModel(
    override val di: DI,
    private val startUrl: String,
) : ViewModel(), DIAware {

    private val userAgent = """
            |os/android
            |model/${platformInfo.model}
            |build/7082000
            |osVer/${platformInfo.osVersion}
            |sdkInt/${platformInfo.sdkInt}
            |network/2
            |BiliApp/7082000
            |mobi_app/android_hd
            |channel/bili
            |c_locale/zh_CN
            |s_locale/zh_CN
            |disable_rcmd/0
        """.trimMargin().replace("\n", "")

    private val userStore by instance<UserStore>()
    private val pageNavigation by instance<PageNavigation>()
    private val messageDialog by instance<MessageDialogState>()

    private var _authUrl = ""
    private var _authCode = ""
    private val loginSessionId = ApiHelper.getUUID()

    var webViewHandle: WebViewHandle? = null

    val loading = mutableStateOf(false)
    val pageTitle = mutableStateOf("")
    val hideNavbar = mutableStateOf(false)

    var runOnUiThread: (() -> Unit) -> Unit = {}
    var shareText: (String) -> Unit = {}

    fun onHandleReady(handle: WebViewHandle) {
        webViewHandle = handle
        val biliJsBridge = BiliJsBridge(
            pageNavigation = pageNavigation,
            webViewHandle = handle,
            runOnUiThread = { action -> runOnUiThread { action() } },
            shareText = shareText,
        )
        handle.addJavascriptInterface("_BiliJsBridge") { message ->
            biliJsBridge.postMessage(message)
        }
        handle.loadUrl(startUrl)
    }

    fun onPageStarted(url: String) {
        loading.value = true
        hideNavbar.value = true
        webViewHandle?.evaluateJavascript("""
            (function(){
                window.BiliJsBridge = {
                    sendTasks: [],
                    callbacks: [],
                    selfCallbackId: 0,
                    newVersion: true,
                    inited: true,
                };
                window.BiliJsBridge.biliInject = {
                    postMessage: function(e) {
                        window._BiliJsBridge.postMessage(e);
                    },
                    biliCallbackReceived: function(t, e, n) {
                        var r = window.BiliJsBridge.callbacks.map((function(t) {
                            return t.callbackId
                        })).indexOf(Number(t));
                        r >= 0 && window.BiliJsBridge.callbacks[r].callback && window.BiliJsBridge.callbacks[r].callback(n || e)
                    }
                }
            })()
        """.trimIndent())
    }

    fun onPageFinished(url: String) {
        loading.value = false
    }

    fun onTitleReceived(title: String?) {
        pageTitle.value = title ?: ""
    }

    fun shouldOverrideUrlLoading(url: String): Boolean {
        if (
            url.startsWith("https://www.bilibili.com/")
            || url.startsWith("https://m.bilibili.com/")
            || url.startsWith("https://bilibili.com/")
        ) {
            getQrCodeUrl()
            return true
        }
        val re = BilibiliNavigation.navigationTo(pageNavigation, url)
        if (re) {
            return true
        }
        if (url.indexOf("bilibili://") == 0) {
            GlobalToaster.show("不支持打开的链接：$url")
            return true
        }
        return false
    }

    fun getQrCodeUrl() = viewModelScope.launch(Dispatchers.IO) {
        try {
            loading.value = true
            val res = BiliApiService.authApi
                .qrCode(loginSessionId)
                .awaitCall()
                .json<ResponseData<QRLoginInfo>>()
            if (res.isSuccess) {
                _authUrl = res.requireData().url
                _authCode = res.requireData().auth_code
                delay((200 * Math.random()).toLong() + 200L)
                if (!confirmQRCode(_authCode)) {
                    withContext(Dispatchers.Main) {
                        webViewHandle?.loadUrl(_authUrl)
                        messageDialog.alert(
                            text = "为获取BiliAPP登录凭证(token)，需模拟一次扫码登录，请在接下来的页面中点击确认登录。( *・ω・)"
                        )
                    }
                }
                launch(Dispatchers.Main) { checkQRCode(_authCode) }
            } else {
                messageDialog.alert(text = res.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            messageDialog.alert(
                text = "发生错误，请稍后重试：" + (e.message ?: e.toString())
            )
        } finally {
            loading.value = false
        }
    }

    suspend fun confirmQRCode(authCode: String): Boolean {
        try {
            val res = BiliApiService.authApi
                .confirmQRCode(authCode)
                .awaitCall()
                .json<MessageInfo>(isLog = true)
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
                    .json<ResponseData<LoginInfo.QrLoginInfo>>()
            }
            when (res.code) {
                86039, 86090 -> {
                    delay(3000)
                    if (_authCode == authCode && isPolling) {
                        checkQRCode(authCode)
                    }
                }
                86038, -3 -> {
                    messageDialog.alert(
                        text = "二维码已过期，请刷新"
                    )
                }
                0 -> {
                    // 成功
                    val loginInfo = res.requireData().toLoginInfo()
                    BilimiaoCommCore.instance.saveAuthInfo(loginInfo)
                    authInfo()
                }
                else -> {
                    // 发生错误
                    messageDialog.alert(
                        text = "登录失败，请稍后重试：" + res.message
                    )
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException){
        } catch (e: Exception) {
            e.printStackTrace()
            messageDialog.alert(
                text = "登录失败，请稍后重试：" + (e.message ?: e.toString())
            )
        }
    }


    private suspend fun authInfo() {
        messageDialog.loading("正在获取用户信息")
        try {
            val res = withContext(Dispatchers.IO) {
                BiliApiService.authApi
                    .account()
                    .awaitCall()
                    .json<ResponseData<UserInfo>>()
            }
            if (res.isSuccess) {
                withContext(Dispatchers.Main) {
                    userStore.setUserInfo(res.requireData())
                    pageNavigation.popBackStack()
                }
                messageDialog.close()
            } else {
                throw Exception(res.message)
            }
        } catch (e: kotlinx.coroutines.CancellationException){
            messageDialog.close()
        } catch (e: Exception) {
            messageDialog.alert(
                text = "获取用户信息失败，请稍后重试：" + (e.message ?: e.toString())
            )
        }
    }

}

@Composable
private fun H5LoginPageContent(
    viewModel: H5LoginPageViewModel
) {
    val platformContext = cn.a10miaomiao.bilimiao.compose.platform.LocalPlatformContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.runOnUiThread = { action ->
            scope.launch(kotlinx.coroutines.Dispatchers.Main) { action() }
        }
        viewModel.shareText = { text -> platformContext.shareText(text) }
    }

    PageConfig(
        title = viewModel.pageTitle.value,
    )
    val windowInsets = localContentInsets()

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        WebViewContainer(
            url = "",
            modifier = Modifier.fillMaxSize()
                .padding(
                    windowInsets.addPaddingValues(
                        addTop = if (viewModel.hideNavbar.value) -windowInsets.topDp.dp else 0.dp,
                        addBottom = 0.dp
                    )
                ),
            config = WebViewConfig(
                javaScriptEnabled = true,
                domStorageEnabled = true,
                databaseEnabled = true,
                allowFileAccess = true,
                allowContentAccess = true,
                builtInZoomControls = true,
                displayZoomControls = false,
                loadsImagesAutomatically = true,
                acceptThirdPartyCookies = true,
            ),
            callbacks = object : WebViewCallbacks {
                override fun onPageStarted(url: String) {
                    viewModel.onPageStarted(url)
                }

                override fun onPageFinished(url: String) {
                    viewModel.onPageFinished(url)
                }

                override fun onTitleReceived(title: String?) {
                    viewModel.onTitleReceived(title)
                }

                override fun shouldOverrideUrlLoading(url: String): Boolean {
                    return viewModel.shouldOverrideUrlLoading(url)
                }
            },
            onHandleReady = { handle ->
                viewModel.onHandleReady(handle)
            },
        )
        if (viewModel.loading.value) {
            CircularProgressIndicator(
                modifier = Modifier.size(60.dp)
                    .align(Alignment.Center),
            )
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
