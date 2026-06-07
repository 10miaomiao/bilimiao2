package cn.a10miaomiao.bilimiao.compose.common.webview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 跨平台 WebView 容器
 *
 * @param url 初始 URL
 * @param config WebView 配置
 * @param callbacks WebView 生命周期回调
 * @param onHandleReady WebView 操作句柄就绪回调
 * @param modifier Compose Modifier
 */
@Composable
expect fun WebViewContainer(
    url: String,
    modifier: Modifier = Modifier,
    config: WebViewConfig = WebViewConfig(),
    callbacks: WebViewCallbacks = object : WebViewCallbacks {},
    onHandleReady: (WebViewHandle) -> Unit = {},
)
