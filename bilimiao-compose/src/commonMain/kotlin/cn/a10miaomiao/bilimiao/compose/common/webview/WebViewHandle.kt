package cn.a10miaomiao.bilimiao.compose.common.webview

/**
 * 跨平台 WebView 操作接口，供 ViewModel 使用
 */
interface WebViewHandle {
    fun loadUrl(url: String)
    fun reload()
    fun evaluateJavascript(script: String, callback: ((String?) -> Unit)? = null)
    fun addJavascriptInterface(name: String, handler: (String) -> Unit)
    fun removeJavascriptInterface(name: String)
    fun stopLoading()
    fun canGoBack(): Boolean
    fun goBack()
}

/**
 * WebView 配置
 */
data class WebViewConfig(
    val javaScriptEnabled: Boolean = true,
    val domStorageEnabled: Boolean = true,
    val databaseEnabled: Boolean = true,
    val allowFileAccess: Boolean = true,
    val allowContentAccess: Boolean = true,
    val builtInZoomControls: Boolean = true,
    val displayZoomControls: Boolean = false,
    val loadsImagesAutomatically: Boolean = true,
    val userAgent: String? = null,
    val acceptThirdPartyCookies: Boolean = true,
)

/**
 * WebView 回调
 */
interface WebViewCallbacks {
    fun onPageStarted(url: String) {}
    fun onPageFinished(url: String) {}
    fun onTitleReceived(title: String?) {}
    fun onProgressChanged(progress: Int) {}
    fun shouldOverrideUrlLoading(url: String): Boolean = false
    fun onReceivedError(error: String) {}
}
