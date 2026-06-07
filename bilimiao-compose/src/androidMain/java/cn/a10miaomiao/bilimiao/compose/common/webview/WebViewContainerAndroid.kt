package cn.a10miaomiao.bilimiao.compose.common.webview

import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun WebViewContainer(
    url: String,
    modifier: Modifier,
    config: WebViewConfig,
    callbacks: WebViewCallbacks,
    onHandleReady: (WebViewHandle) -> Unit,
) {
    val webViewState = remember {
        object : WebViewHandle {
            var webView: WebView? = null

            override fun loadUrl(url: String) { webView?.loadUrl(url) }
            override fun reload() { webView?.reload() }
            override fun evaluateJavascript(script: String, callback: ((String?) -> Unit)?) {
                webView?.evaluateJavascript(script) { callback?.invoke(it) }
            }
            override fun addJavascriptInterface(name: String, handler: (String) -> Unit) {
                webView?.addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun postMessage(message: String) { handler(message) }
                    },
                    name
                )
            }
            override fun removeJavascriptInterface(name: String) {
                webView?.removeJavascriptInterface(name)
            }
            override fun stopLoading() { webView?.stopLoading() }
            override fun canGoBack(): Boolean = webView?.canGoBack() == true
            override fun goBack() { webView?.goBack() }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewState.webView?.apply {
                stopLoading()
                destroy()
            }
            webViewState.webView = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            FrameLayout(context).apply {
                val webView = WebView(context).also { wv ->
                    webViewState.webView = wv
                    configureWebView(wv, config, callbacks, webViewState)
                    wv.loadUrl(url)
                    onHandleReady(webViewState)
                }
                addView(webView)
            }
        },
        onRelease = { it.removeAllViews() }
    )
}

private fun configureWebView(
    webView: WebView,
    config: WebViewConfig,
    callbacks: WebViewCallbacks,
    handle: WebViewHandle,
) {
    CookieManager.getInstance().apply {
        setAcceptCookie(true)
        setAcceptThirdPartyCookies(webView, config.acceptThirdPartyCookies)
    }

    webView.webViewClient = object : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            url?.let { callbacks.onPageStarted(it) }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            url?.let { callbacks.onPageFinished(it) }
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            return request?.url?.toString()?.let { callbacks.shouldOverrideUrlLoading(it) } ?: false
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            error?.description?.toString()?.let { callbacks.onReceivedError(it) }
        }
    }

    webView.webChromeClient = object : WebChromeClient() {
        override fun onReceivedTitle(view: WebView?, title: String?) {
            callbacks.onTitleReceived(title)
        }

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            callbacks.onProgressChanged(newProgress)
        }
    }

    webView.settings.apply {
        javaScriptEnabled = config.javaScriptEnabled
        domStorageEnabled = config.domStorageEnabled
        databaseEnabled = config.databaseEnabled
        allowFileAccess = config.allowFileAccess
        allowContentAccess = config.allowContentAccess
        setSupportZoom(config.builtInZoomControls)
        builtInZoomControls = config.builtInZoomControls
        displayZoomControls = config.displayZoomControls
        loadsImagesAutomatically = config.loadsImagesAutomatically
        cacheMode = WebSettings.LOAD_DEFAULT
        config.userAgent?.let { userAgentString = it }
    }
}
