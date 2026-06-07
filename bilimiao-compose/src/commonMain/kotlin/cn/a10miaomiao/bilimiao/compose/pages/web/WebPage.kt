package cn.a10miaomiao.bilimiao.compose.pages.web

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.BiliJsBridge
import cn.a10miaomiao.bilimiao.compose.common.navigation.BilibiliNavigation
import cn.a10miaomiao.bilimiao.compose.common.addPaddingValues
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContentInsets
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.platform.platformInfo
import cn.a10miaomiao.bilimiao.compose.common.webview.WebViewCallbacks
import cn.a10miaomiao.bilimiao.compose.common.webview.WebViewConfig
import cn.a10miaomiao.bilimiao.compose.common.webview.WebViewContainer
import cn.a10miaomiao.bilimiao.compose.common.webview.WebViewHandle
import cn.a10miaomiao.bilimiao.compose.platform.LocalPlatformContext
import com.a10miaomiao.bilimiao.comm.network.ApiHelper
import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
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
        val platformContext = LocalPlatformContext.current
        val scope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            viewModel.runOnUiThread = { action ->
                scope.launch(Dispatchers.Main) { action() }
            }
            viewModel.shareText = { platformContext.shareText(it) }
        }
        WebPageContent(viewModel)
    }
}

private class WebPageViewModel(
    override val di: DI,
    val startUrl: String,
) : ViewModel(), DIAware {

    private val userAgent = """
            |os/android
            |model/${platformInfo.model}
            |build/${ApiHelper.BUILD_VERSION}
            |osVer/${platformInfo.osVersion}
            |sdkInt/${platformInfo.sdkInt}
            |network/2
            |BiliApp/${ApiHelper.BUILD_VERSION}
            |mobi_app/android_hd
            |channel/bili
            |c_locale/zh_CN
            |s_locale/zh_CN
            |disable_rcmd/0
        """.trimMargin().replace("\n", "")

    private val pageNavigation by instance<PageNavigation>()

    var runOnUiThread: (() -> Unit) -> Unit = {}
    var shareText: (String) -> Unit = {}

    val loading = mutableStateOf(false)
    val pageTitle = mutableStateOf("")
    val hideNavbar = mutableStateOf(false)

    var webViewHandle: WebViewHandle? = null

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
    }

    val webViewCallbacks = object : WebViewCallbacks {
        override fun shouldOverrideUrlLoading(url: String): Boolean {
            val re = BilibiliNavigation.navigationTo(pageNavigation, url)
            if (re) return true
            if (url.indexOf("bilibili://") == 0) {
                GlobalToaster.show("不支持打开的链接：$url")
                return true
            }
            return false
        }

        override fun onPageStarted(url: String) {
            loading.value = true
            hideNavbar.value = url.indexOf("navhide=1") != -1
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

        override fun onPageFinished(url: String) {
            loading.value = false
        }

        override fun onTitleReceived(title: String?) {
            title?.let { pageTitle.value = it }
        }
    }

    val webViewConfig = WebViewConfig(
        userAgent = run {
            val defaultUA = "Mozilla/5.0 (Linux; Android) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
            "$defaultUA $userAgent"
        }
    )
}

@Composable
private fun WebPageContent(
    viewModel: WebPageViewModel
) {
    PageConfig(
        title = viewModel.pageTitle.value,
    )
    val windowInsets = localContentInsets()

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        WebViewContainer(
            url = viewModel.startUrl,
            modifier = Modifier.fillMaxSize()
                .padding(
                    windowInsets.addPaddingValues(
                        addTop = if (viewModel.hideNavbar.value) -windowInsets.topDp.dp else 0.dp,
                        addBottom = 0.dp
                    )
                ),
            config = viewModel.webViewConfig,
            callbacks = viewModel.webViewCallbacks,
            onHandleReady = viewModel::onHandleReady,
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
