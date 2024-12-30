package cn.a10miaomiao.bilimiao.compose.pages.web

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.BiliJsBridge
import cn.a10miaomiao.bilimiao.compose.common.navigation.BilibiliNavigation
import cn.a10miaomiao.bilimiao.compose.common.addPaddingValues
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import com.a10miaomiao.bilimiao.comm.network.ApiHelper
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

@Serializable
class WebPage(
    val url: String,
) : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: WebPageViewModel = diViewModel {
            WebPageViewModel(it, url)
        }
        WebPageContent(viewModel)
    }
}

private class WebPageViewModel(
    override val di: DI,
    private val startUrl: String,
) : ViewModel(), DIAware {

    private val userAgent = """
            |os/android 
            |model/${Build.MODEL} 
            |build/${ApiHelper.BUILD_VERSION} 
            |osVer/${Build.VERSION.RELEASE} 
            |sdkInt/${Build.VERSION.SDK_INT}  
            |network/2 
            |BiliApp/${ApiHelper.BUILD_VERSION} 
            |mobi_app/android_hd 
            |channel/bili 
            |c_locale/zh_CN 
            |s_locale/zh_CN 
            |disable_rcmd/0
        """.trimMargin().replace("\n", "")

    private val fragment by instance<Fragment>()
    private val pageNavigation by instance<PageNavigation>()

    var webView: WebView? = null

    val loading = mutableStateOf(false)
    val pageTitle = mutableStateOf("")
    val hideNavbar = mutableStateOf(false)

    init {
//        initWebView(webView)
    }

    fun initWebView(view: WebView) {
        val biliJsBridge = BiliJsBridge(fragment, pageNavigation, view)
        CookieManager.getInstance().setAcceptThirdPartyCookies(view, true)
        view.webViewClient = mWebViewClient
        view.webChromeClient = mWebChromeClient
        view.settings.apply {
            javaScriptEnabled = true
            var defaultUserAgentString = userAgentString
            if ("Mobile" !in defaultUserAgentString) {
                defaultUserAgentString += " Mobile"
            }
            userAgentString = "$defaultUserAgentString $userAgent"
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
        view.addJavascriptInterface(biliJsBridge, "_BiliJsBridge")
        view.loadUrl(startUrl)
    }

    private val mWebViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val url = request.url.toString()
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
            hideNavbar.value = url.indexOf("navhide=1") != -1
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

}

@Composable
private fun WebPageContent(
    viewModel: WebPageViewModel
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