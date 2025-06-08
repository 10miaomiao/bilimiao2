package cn.a10miaomiao.bilimiao.compose.pages.auth

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.os.Build
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.BiliJsBridge
import cn.a10miaomiao.bilimiao.compose.common.addPaddingValues
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.BilibiliNavigation
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.components.dialogs.MessageDialogState
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.auth.LoginInfo
import com.a10miaomiao.bilimiao.comm.entity.auth.QRLoginInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserInfo
import com.a10miaomiao.bilimiao.comm.network.ApiHelper
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
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
            |model/${Build.MODEL} 
            |build/7082000 
            |osVer/${Build.VERSION.RELEASE} 
            |sdkInt/${Build.VERSION.SDK_INT}  
            |network/2 
            |BiliApp/7082000 
            |mobi_app/android_hd 
            |channel/bili 
            |c_locale/zh_CN 
            |s_locale/zh_CN 
            |disable_rcmd/0
        """.trimMargin().replace("\n", "")

    private val fragment by instance<Fragment>()
    private val userStore by instance<UserStore>()
    private val pageNavigation by instance<PageNavigation>()
    private val messageDialog by instance<MessageDialogState>()

    private var _authUrl = ""
    private var _authCode = ""
    private val loginSessionId = ApiHelper.getUUID()

    var webView: WebView? = null

    val loading = mutableStateOf(false)
    val pageTitle = mutableStateOf("")
    val hideNavbar = mutableStateOf(false)

    init {
//        initWebView(webView)
    }

    fun initWebView(view: WebView) {
        CookieManager.getInstance().setAcceptThirdPartyCookies(view, true)
        view.webViewClient = mWebViewClient
        view.webChromeClient = mWebChromeClient
        view.settings.apply {
            javaScriptEnabled = true
            var defaultUserAgentString = userAgentString
            if ("Mobile" !in defaultUserAgentString) {
                defaultUserAgentString += " Mobile"
            }
            userAgentString = defaultUserAgentString
            allowContentAccess = true
            allowFileAccess = true
            cacheMode = WebSettings.LOAD_DEFAULT
            databaseEnabled = true
            domStorageEnabled = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            loadsImagesAutomatically = true
        }
        view.loadUrl(startUrl)
    }

    private fun setBiliAppWebView(view: WebView) {
        var defaultUserAgentString = view.settings.userAgentString
        if ("Mobile" !in defaultUserAgentString) {
            defaultUserAgentString += " Mobile"
        }
        val userAgent = "$defaultUserAgentString $userAgent"
        view.settings.userAgentString = userAgent
        val biliJsBridge = BiliJsBridge(fragment, pageNavigation, view)
        view.addJavascriptInterface(biliJsBridge, "_BiliJsBridge")
    }

    private val mWebViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val url = request.url.toString()
            if (
                url.startsWith("https://www.bilibili.com/")
                || url.startsWith("https://m.bilibili.com/")
                || url.startsWith("https://bilibili.com/")
            ){
                getQrCodeUrl(view)
                return true
            }
            val re = BilibiliNavigation.navigationTo(pageNavigation, url)
            if (re) {
                return true
            }
            if (url.indexOf("bilibili://") == 0) {
                PopTip.show("不支持打开的链接：$url")
                return true
            }
            return false
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            loading.value = true
            hideNavbar.value = true
            view.evaluateJavascript("""
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
            """.trimIndent()) {
//                DebugMiao.log("callback", it)
            }
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            loading.value = false
//            val js = """javascript:(function() {
//                        var parent = document.getElementsByTagName('head').item(0);
//                        var style = document.createElement('style');
//                        style.type = 'text/css';
//                        style.innerHTML = '#dynamic-openapp, #dynamic-openapp-mask,.mini-header-container,.fixed-header-container,.v-navbar__body,#internationalHeader,.international-footer,.bili-footer,#cannot-check{display: none !important;} #app{padding-bottom: 0;}';
//                        parent.appendChild(style);
//                        window.java_obj.showDescription(
//                               'theme-color',
//                               document.querySelector('meta[name="theme-color"]').getAttribute('content')
//                        );
//                        window.java_obj.test.hello('from js');
//                    })()
//                """
//            view.loadUrl(js)
        }
    }

    private val mWebChromeClient = object : WebChromeClient() {
        override fun onReceivedTitle(view: WebView, title: String) {
            super.onReceivedTitle(view, title)
            pageTitle.value = title
        }
    }

    fun getQrCodeUrl(view: WebView) = viewModelScope.launch(Dispatchers.IO) {
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
                        setBiliAppWebView(view)
                        view.loadUrl(_authUrl)
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
                    BilimiaoCommApp.commApp.saveAuthInfo(loginInfo)
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
    PageConfig(
        title = viewModel.pageTitle.value,
    )
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize()
                .padding(
                    windowInsets.addPaddingValues(
                        addTop = if (viewModel.hideNavbar.value) -windowInsets.topDp.dp else 0.dp,
                        addBottom = windowStore.bottomAppBarHeightDp.dp
                    )
                ),
            factory = {
                FrameLayout(it).apply {
                    val webView = viewModel.webView ?: WebView(it).also {
                        viewModel.initWebView(it)
                        viewModel.webView = it
                    }
                    addView(webView)
                }
            },
            onRelease = {
                it.removeAllViews()
            }
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